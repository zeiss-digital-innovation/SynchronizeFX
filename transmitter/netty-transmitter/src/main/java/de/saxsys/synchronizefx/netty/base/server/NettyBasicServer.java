/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

package de.saxsys.synchronizefx.netty.base.server;

import java.util.List;

import de.saxsys.synchronizefx.core.clientserver.CommandTransferServer;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackServer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Contains the base server implementation for all Netty based {@link CommandTransferServer}s.
 */
public abstract class NettyBasicServer implements CommandTransferServer {

    private NioEventLoopGroup connectionAccptorGroup;
    private NioEventLoopGroup clientConnectionGroup;

    private ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final int port;
    private NetworkToTopologyCallbackServer callback;

    /**
     * Initializes an instance.
     * 
     * @param port The port to listen on when the server is {@link #start()}ed.
     */
    public NettyBasicServer(final int port) {
        this.port = port;
    }

    /**
     * Creates an channel pipeline implementation with data codecs specific to the concrete implementation of this
     * base server.
     * 
     * @return The created initializer
     */
    protected abstract BasicChannelInitializerServer createChannelInitializer();

    @Override
    public void start() throws SynchronizeFXException {
        this.connectionAccptorGroup = new NioEventLoopGroup();
        this.clientConnectionGroup = new NioEventLoopGroup();

        BasicChannelInitializerServer channelInitializer = createChannelInitializer();
        channelInitializer.setTopologyCallback(callback);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(connectionAccptorGroup, clientConnectionGroup).channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer).childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.bind(port).syncUninterruptibly();
    }

    @Override
    public void setTopologyLayerCallback(final NetworkToTopologyCallbackServer callback) {
        this.callback = callback;
    }

    @Override
    public void onConnectFinished(final Object client) {
        clients.add((Channel) client);
    }

    @Override
    public void sendToAll(final List<Command> commands) {
        clients.writeAndFlush(commands);
    }

    @Override
    public void send(final List<Command> commands, final Object client) {
        ((Channel) client).writeAndFlush(commands);
    }

    @Override
    public void sendToAllExcept(final List<Command> commands, final Object nonReciver) {
        clients.writeAndFlush(commands, new ChannelMatcher() {
            @Override
            public boolean matches(final Channel candidate) {
                return candidate != nonReciver;
            }
        });
    }

    @Override
    public void shutdown() {
        connectionAccptorGroup.shutdownGracefully().addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(final Future<Object> future) throws Exception {
                clientConnectionGroup.shutdownGracefully();
            }
        });
    };
}
