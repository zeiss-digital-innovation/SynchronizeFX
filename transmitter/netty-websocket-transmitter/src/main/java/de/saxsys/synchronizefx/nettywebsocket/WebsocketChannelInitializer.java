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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
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

/**
 * Sets up a {@link SocketChannel} for client side, web socket based SynchronizeFX communication.
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 */
class WebsocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * If no data was received from the server in this time (in milliseconds) a Ping Frame is send as keep alive.
     */
    private static final int KEEP_ALIVE = 20000;
    private static final String PROTOCOL = "v1.websocket.synchronizefx.saxsys.de";

    private final URI serverUri;
    private final Map<String, Object> httpHeaders;
    private IncommingWebsocketFrameHandler connection;
    private NettyWebsocketClient parent;

    /**
     * Gathers all dependencies for this class.
     * 
     * @param serverUri the uri of the server to connect to.
     * @param httpHeaders optional headers that should be passed to the server when initializing the HTTP connection.
     *            If no user defined headers should be passed this parameter can be <code>null</code>
     * @param parent 
     *            the Instance that setup this connection. It is informed on incoming messages or errors.
     */
    public WebsocketChannelInitializer(final URI serverUri, final Map<String, Object> httpHeaders,
            final NettyWebsocketClient parent) {
        this.serverUri = serverUri;
        this.httpHeaders = httpHeaders;
        this.parent = parent;
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {
        final ChannelPipeline pipeline = channel.pipeline();
        final boolean useSSL = uriRequiresSslOrFail(serverUri);

        this.connection = new IncommingWebsocketFrameHandler(parent);

        if (useSSL) {
            pipeline.addLast("ssl", new SslHandler(new NonValidatingSSLEngineFactory().createClientEngine()));
        }
        pipeline.addLast("keep-alive-activator", new IdleStateHandler(KEEP_ALIVE, 0, 0, TimeUnit.MILLISECONDS));
        pipeline.addLast("http-codec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
        pipeline.addLast("websocket-protocol-handler", new WebSocketClientProtocolHandler(serverUri,
                WebSocketVersion.V13, PROTOCOL, false, createHttpHeaders(httpHeaders), Integer.MAX_VALUE));
        pipeline.addLast("ws-handler", connection);
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
