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

package de.saxsys.synchronizefx.netty.websockets;

import java.net.URI;
import java.util.Map;

import de.saxsys.synchronizefx.netty.base.Codec;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

/**
 * Sets up a {@link SocketChannel} for client side, web socket based SynchronizeFX communication.
 * 
 * @author Raik Bieniek
 */
class WebsocketChannelInitializer implements Codec {

    private static final String PROTOCOL = "v2.websocket.synchronizefx.saxsys.de";

    private final URI serverUri;
    private final Map<String, Object> httpHeaders;

    /**
     * Gathers all dependencies for this class.
     * 
     * @param serverUri the uri of the server to connect to.
     * @param httpHeaders optional headers that should be passed to the server when initializing the HTTP connection.
     *            If no user defined headers should be passed this parameter can be <code>null</code>
     */
    WebsocketChannelInitializer(final URI serverUri, final Map<String, Object> httpHeaders) {
        this.serverUri = serverUri;
        this.httpHeaders = httpHeaders;
    }

    @Override
    public void addToPipeline(final ChannelPipeline pipeline) {
        pipeline.addLast("http-codec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(8192));

        final WebSocketClientHandshaker handShaker = new WhiteSpaceInPathWebSocketClientHandshaker13(serverUri,
                WebSocketVersion.V13, PROTOCOL, false, createHttpHeaders(httpHeaders), Integer.MAX_VALUE);
        pipeline.addLast("websocket-protocol-handler", new WebSocketClientProtocolHandler(handShaker));

        pipeline.addLast("websocket-frame-codec", new ByteBufToWebSocketFrameCodec());
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
}
