/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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

package de.saxsys.synchronizefx.nettywebsocket;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.saxsys.synchronizefx.core.clientserver.MessageTransferClient;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

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
 * A client side transmitter implementation for SynchronizeFX that uses Netty and transfers messages over WebSockets.
 * 
 * <p>
 * Both, encrypted and unencrypted web socket connections are supported with this transmitter. The transmitter does
 * however not validate the server certificate for encrypetd connections.
 * </p>
 * 
 */
public class NettyWebsocketClient implements MessageTransferClient {

    private static final Logger LOG = LoggerFactory.getLogger(NettyWebsocketClient.class);
    /**
     * The timeout for connection attempts in milliseconds.
     */
    private static final int TIMEOUT = 10000;

    private Serializer serializer;
    private URI serverUri;
    private Map<String, Object> httpHeaders;
    private NetworkToTopologyCallbackClient callback;

    private EventLoopGroup eventLoopGroup;

    private Channel channel;

    /**
     * Initializes the transmitter.
     * 
     * @param serverUri The URI for the server to connect to. The scheme must be <code>ws</code> for a HTTP based
     *            websocket connection and <code>wss</code> for a HTTPS based connection.
     * @param serializer The serializer to use to serialize SynchronizeFX messages.
     */
    public NettyWebsocketClient(final URI serverUri, final Serializer serializer) {
        this.serverUri = serverUri;
        this.serializer = serializer;
    }

    /**
     * Initializes the transmitter.
     * 
     * @param serverUri The URI for the server to connect to. The scheme must be <code>ws</code> for a HTTP based
     *            websocket connection and <code>wss</code> for a HTTPS based connection.
     * @param serializer The serializer to use to serialize SynchronizeFX messages.
     * @param httpHeaders header parameter for the http connection
     */
    public NettyWebsocketClient(final URI serverUri, final Serializer serializer,
            final Map<String, Object> httpHeaders) {
        this(serverUri, serializer);
        this.httpHeaders = new HashMap<>(httpHeaders);
    }

    @Override
    public void setTopologyCallback(final NetworkToTopologyCallbackClient callback) {
        this.callback = callback;
    }

    @Override
    public void connect() throws SynchronizeFXException {
        this.eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        WebsocketChannelInitializer initializer =
                new WebsocketChannelInitializer(serverUri, httpHeaders, serializer, callback);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT).handler(initializer);

        LOG.info("Connecting to server");
        try {
            ChannelFuture future = bootstrap.connect(serverUri.getHost(), serverUri.getPort());
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
    public void send(final List<Object> messages) {
        channel.writeAndFlush(messages);
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
