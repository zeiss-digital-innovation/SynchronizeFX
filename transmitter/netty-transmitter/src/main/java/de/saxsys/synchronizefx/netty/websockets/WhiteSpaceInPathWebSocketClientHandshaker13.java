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

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker13;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

/**
 * A Websocket handshaker that handles encoded spaces in the URI path correctly.
 * 
 * <p>
 * This is a workaround for <a href="https://github.com/netty/netty/issues/4505">Netty issue 4505</a>.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class WhiteSpaceInPathWebSocketClientHandshaker13 extends WebSocketClientHandshaker13 {

    // CHECKSTYLE:OFF constructor is too long.
    /**
     * Initializes an instance with all is dependencies.
     * 
     * @param webSocketURL see
     *            {@link WebSocketClientHandshaker13#WebSocketClientHandshaker13(URI, WebSocketVersion, String, boolean, HttpHeaders, int)}
     * @param version see
     *            {@link WebSocketClientHandshaker13#WebSocketClientHandshaker13(URI, WebSocketVersion, String, boolean, HttpHeaders, int)}
     * @param subprotocol see
     *            {@link WebSocketClientHandshaker13#WebSocketClientHandshaker13(URI, WebSocketVersion, String, boolean, HttpHeaders, int)}
     * @param allowExtensions see
     *            {@link WebSocketClientHandshaker13#WebSocketClientHandshaker13(URI, WebSocketVersion, String, boolean, HttpHeaders, int)}
     * @param customHeaders see
     *            {@link WebSocketClientHandshaker13#WebSocketClientHandshaker13(URI, WebSocketVersion, String, boolean, HttpHeaders, int)}
     * @param maxFramePayloadLength see
     *            {@link WebSocketClientHandshaker13#WebSocketClientHandshaker13(URI, WebSocketVersion, String, boolean, HttpHeaders, int)}
     */
    // CHECKSTYLE:ON constructor is too long.
    public WhiteSpaceInPathWebSocketClientHandshaker13(final URI webSocketURL, final WebSocketVersion version,
            final String subprotocol, final boolean allowExtensions, final HttpHeaders customHeaders,
            final int maxFramePayloadLength) {
        super(webSocketURL, version, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength);
    }

    @Override
    protected FullHttpRequest newHandshakeRequest() {
        FullHttpRequest request = super.newHandshakeRequest();
        request.setUri(super.uri().getRawPath());
        return request;
    }
}
