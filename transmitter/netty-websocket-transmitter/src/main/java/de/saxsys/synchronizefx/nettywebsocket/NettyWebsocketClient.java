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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.core.clientserver.MessageTransferClient;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

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
    /**
     * If no data was received from the server in this time (in milliseconds) a Ping Frame is send as keep alive.
     */
    private static final int KEEP_ALIVE = 20000;

    private Serializer serializer;
    private URI uri;
    private Map<String, Object> headerParams;
    private NetworkToTopologyCallbackClient callback;

    private EventLoopGroup eventLoopGroup;

    private Channel channel;

    /**
     * Initializes the transmitter.
     * 
     * @param uri The URI for the server to connect to. The scheme must be <code>ws</code> for a HTTP based websocket
     *            connection and <code>wss</code> for a HTTPS based connection.
     * @param serializer The serializer to use to serialize SynchronizeFX messages.
     */
    public NettyWebsocketClient(final URI uri, final Serializer serializer) {
        this.uri = uri;
        this.serializer = serializer;
    }

    /**
     * Initializes the transmitter.
     * 
     * @param uri The URI for the server to connect to. The scheme must be <code>ws</code> for a HTTP based websocket
     *            connection and <code>wss</code> for a HTTPS based connection.
     * @param serializer The serializer to use to serialize SynchronizeFX messages.
     * @param headerParams header parameter for the http connection
     */
    public NettyWebsocketClient(final URI uri, final Serializer serializer, final Map<String, Object> headerParams) {
        this(uri, serializer);
        this.headerParams = new HashMap<>(headerParams);
    }

    @Override
    public void setTopologyCallback(final NetworkToTopologyCallbackClient callback) {
        this.callback = callback;
    }

    @Override
    public void connect() throws SynchronizeFXException {
        final boolean useSSL = uriRequiresSslOrFail(uri);

        this.eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        final NettyWebsocketConnection connection = new NettyWebsocketConnection(uri, this, headerParams);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT).handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(final SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        if (useSSL) {
                            pipeline.addLast("ssl",
                                    new SslHandler(new NonValidatingSSLEngineFactory().createClientEngine()));
                        }
                        pipeline.addLast("keep-alive-activator", new IdleStateHandler(KEEP_ALIVE, 0, 0,
                                TimeUnit.MILLISECONDS));
                        pipeline.addLast("http-codec", new HttpClientCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
                        pipeline.addLast("ws-handler", connection);
                    }
                });

        LOG.info("Connecting to server");
        try {
            ChannelFuture future = bootstrap.connect(uri.getHost(), uri.getPort());
            if (!future.await(TIMEOUT)) {
                disconnect();
                throw new SynchronizeFXException("Timeout while trying to connect to the server.");
            }
            if (!future.isSuccess()) {
                disconnect();
                throw new SynchronizeFXException("Connection to the server failed.", future.cause());
            }
            this.channel = future.channel();
            connection.waitForHandshakeFinished();
        } catch (InterruptedException e) {
            disconnect();
            throw new SynchronizeFXException(e);
        }
    }

    @Override
    public void send(final List<Object> messages) {
        byte[] serialized;
        try {
            serialized = serializer.serialize(messages);
        } catch (SynchronizeFXException e) {
            disconnect();
            callback.onError(e);
            return;
        }
        channel.write(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(serialized)));
    }

    @Override
    public void disconnect() {
        disconnect(true);
    }

    private void disconnect(final boolean sendCloseWebsocketFrame) {
        if (sendCloseWebsocketFrame && channel != null) {
            channel.write(new CloseWebSocketFrame());
        }

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

    /**
     * Call this when messages where received from the server.
     * 
     * @param msg The messages striped of all WebSocket Overhead.
     */
    void onMessageRecived(final byte[] msg) {
        List<Object> deserialized;
        try {
            deserialized = serializer.deserialize(msg);
        } catch (SynchronizeFXException e) {
            disconnect();
            callback.onError(e);
            return;
        }
        callback.recive(deserialized);
    }

    /**
     * Call this when an error occurred.
     * 
     * @param cause The cause of the error.
     */
    void onError(final Throwable cause) {
        disconnect();
        callback.onError(new SynchronizeFXException(cause));
    }

    /**
     * Call this when the server closed the connection.
     */
    void onServerDisconnect() {
        disconnect(true);
        callback.onServerDisconnect();
    }

    private boolean uriRequiresSslOrFail(final URI uri) throws SynchronizeFXException {
        String protocol = uri.getScheme();
        if ("ws".equals(protocol)) {
            return false;
        }
        if ("wss".equals(protocol)) {
            return true;
        }
        throw new SynchronizeFXException(new IllegalArgumentException("The protocol of the uri is not Websocket."));
    }
}
