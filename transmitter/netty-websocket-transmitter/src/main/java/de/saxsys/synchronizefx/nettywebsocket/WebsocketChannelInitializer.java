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

package de.saxsys.synchronizefx.nettywebsocket;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up a {@link SocketChannel} for client side, web socket based SynchronizeFX communication.
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 */
class WebsocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger LOG = LoggerFactory.getLogger(WebsocketChannelInitializer.class);
    /**
     * If no data was received from the server in this time (in milliseconds) a Ping Frame is send as keep alive.
     */
    private static final int KEEP_ALIVE = 20000;
    private static final String PROTOCOL = "v1.websocket.synchronizefx.saxsys.de";

    private final URI serverUri;
    private final Map<String, Object> httpHeaders;
    private final NetworkToTopologyCallbackClient callback;
    private final Serializer serializer;

    /**
     * Gathers all dependencies for this class.
     * 
     * @param serverUri the uri of the server to connect to.
     * @param httpHeaders optional headers that should be passed to the server when initializing the HTTP connection.
     *            If no user defined headers should be passed this parameter can be <code>null</code>
     * @param serializer The implementation for serializing and deserializing <code>byte[]</code> to SynchronizeFX
     *            commands.
     * @param callback The callback to the upper layer to inform it on new messages and errors.
     */
    public WebsocketChannelInitializer(final URI serverUri, final Map<String, Object> httpHeaders,
            final Serializer serializer, final NetworkToTopologyCallbackClient callback) {
        this.serverUri = serverUri;
        this.httpHeaders = httpHeaders;
        this.serializer = serializer;
        this.callback = callback;
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {
        final ChannelPipeline pipeline = channel.pipeline();
        final boolean useSSL = uriRequiresSslOrFail();

        if (useSSL) {
            final SSLEngine tlsEngine = new NonValidatingSSLEngineFactory().createClientEngine();
            final SslHandler tls = new SslHandler(tlsEngine);
            tls.handshakeFuture().addListener(new GenericFutureListener<Future<? super Channel>>() {
                @Override
                public void operationComplete(final Future<? super Channel> future) throws Exception {
                    LOG.debug("Using cipher " + tlsEngine.getSession().getCipherSuite()
                            + " for the encrypted connection to the server.");
                }
            });
            pipeline.addLast("tls", tls);
        }
        pipeline.addLast("keep-alive", new IdleStateHandler(KEEP_ALIVE, 0, 0, TimeUnit.MILLISECONDS));

        pipeline.addLast("http-codec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
        pipeline.addLast("websocket-protocol-handler", new WebSocketClientProtocolHandler(serverUri,
                WebSocketVersion.V13, PROTOCOL, false, createHttpHeaders(httpHeaders), Integer.MAX_VALUE));

        pipeline.addLast("websocket-frame-codec", new CommandToWebSocketFrameCodec(serializer));
        pipeline.addLast("command-handler", new InboundCommandHandler(callback));
        pipeline.addLast("event-handler", new NetworkEventHandler(callback));
    }

    private HttpHeaders createHttpHeaders(final Map<String, Object> headerParams) {
        if (headerParams == null || headerParams.isEmpty()) {
            return null;
        }
        HttpHeaders headers = new DefaultHttpHeaders();
        for (Map.Entry<String, Object> headerEntry : headerParams.entrySet()) {
            headers.add(headerEntry.getKey(), headerEntry.getValue());
        }
        return headers;
    }

    private boolean uriRequiresSslOrFail() throws SynchronizeFXException {
        String protocol = serverUri.getScheme();
        if ("ws".equals(protocol)) {
            return false;
        }
        if ("wss".equals(protocol)) {
            return true;
        }
        throw new SynchronizeFXException(new IllegalArgumentException("The protocol of the uri is not Websocket."));
    }
}
