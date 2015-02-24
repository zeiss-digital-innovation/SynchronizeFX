/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

package de.saxsys.synchronizefx.netty.base.client;

import java.net.SocketAddress;
import java.util.List;

import de.saxsys.synchronizefx.core.clientserver.CommandTransferClient;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the base client implementation for all Netty based {@link CommandTransferClient}s.
 */
public abstract class NettyBasicClient implements CommandTransferClient {

    private static final Logger LOG = LoggerFactory.getLogger(NettyBasicClient.class);
    /**
     * The timeout for connection attempts in milliseconds.
     */
    private static final int TIMEOUT = 10000;

    private final SocketAddress address;

    private NetworkToTopologyCallbackClient callback;
    private EventLoopGroup eventLoopGroup;

    private Channel channel;

    /**
     * Initializes the client.
     * 
     * @param address The address to {@link #connect()} to.
     */
    public NettyBasicClient(final SocketAddress address) {
        this.address = address;
    }

    /**
     * Creates an channel pipeline implementation with data codecs specific to the concrete implementation of this
     * base client.
     * 
     * @return The created initializer
     */
    protected abstract BasicChannelInitializerClient createChannelInitializer();

    @Override
    public void setTopologyCallback(final NetworkToTopologyCallbackClient callback) {
        this.callback = callback;
    }

    @Override
    public void connect() throws SynchronizeFXException {
        this.eventLoopGroup = new NioEventLoopGroup();
        BasicChannelInitializerClient channelInitializer = createChannelInitializer();
        channelInitializer.setTopologyCallback(callback);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT).handler(channelInitializer);

        LOG.info("Connecting to server");
        try {
            ChannelFuture future = bootstrap.connect(address);
            if (!future.await(TIMEOUT)) {
                disconnect();
                throw new SynchronizeFXException("Timeout while trying to connect to the server.");
            }
            if (!future.isSuccess()) {
                disconnect();
                throw new SynchronizeFXException("Connection to the server failed.", future.cause());
            }
            this.channel = future.channel();
        } catch (InterruptedException e) {
            disconnect();
            throw new SynchronizeFXException(e);
        }
    }

    @Override
    public void send(final List<Command> commands) {
        channel.writeAndFlush(commands);
    }

    @Override
    public void disconnect() {
        try {
            if (channel != null) {
                channel.close();
                channel.closeFuture().sync();
            }
        } catch (InterruptedException e) {
            callback.onError(new SynchronizeFXException("Could not wait for the disconnect to finish.", e));
        }
        eventLoopGroup.shutdownGracefully();
    }
}
