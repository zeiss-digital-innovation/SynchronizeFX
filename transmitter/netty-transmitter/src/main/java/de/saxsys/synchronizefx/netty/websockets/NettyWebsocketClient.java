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

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.netty.base.client.BasicChannelInitializerClient;
import de.saxsys.synchronizefx.netty.base.client.NettyBasicClient;

/**
 * A client side transmitter implementation for SynchronizeFX that uses Netty and transfers messages over WebSockets.
 * 
 * <p>
 * Both, encrypted and unencrypted web socket connections are supported with this transmitter. The transmitter does
 * however not validate the server certificate for encrypted connections.
 * </p>
 * 
 */
public class NettyWebsocketClient extends NettyBasicClient {

    private final URI serverUri;
    private final Serializer serializer;
    private final Map<String, Object> httpHeaders;
    private final boolean useSsl;

    /**
     * Initializes the transmitter.
     * 
     * @param serverUri The URI for the server to connect to. The scheme must be <code>ws</code> for a HTTP based
     *            websocket connection and <code>wss</code> for a HTTPS based connection.
     * @param channelName The name of the channel to connect to at the given server URI.
     * @param serializer The serializer to use to serialize SynchronizeFX messages.
     */
    public NettyWebsocketClient(final URI serverUri, final String channelName, final Serializer serializer) {
        this(serverUri, channelName, serializer, null);
    }

    /**
     * Initializes the transmitter.
     * 
     * @param serverUri The URI for the server to connect to. The scheme must be <code>ws</code> for a HTTP based
     *            websocket connection and <code>wss</code> for a HTTPS based connection.
     * @param channelName The name of the channel to connect to at the given server URI.
     * @param serializer The serializer to use to serialize SynchronizeFX messages.
     * @param httpHeaders header parameter for the http connection
     */
    public NettyWebsocketClient(final URI serverUri, final String channelName, final Serializer serializer,
            final Map<String, Object> httpHeaders) {
        super(new InetSocketAddress(serverUri.getHost(), serverUri.getPort()));
        this.serverUri = concatUri(serverUri, channelName);
        this.serializer = serializer;
        this.httpHeaders = new HashMap<>(httpHeaders);
        this.useSsl = uriRequiresSslOrFail();
    }

    @Override
    protected BasicChannelInitializerClient createChannelInitializer() {
        WebsocketChannelInitializer codec = new WebsocketChannelInitializer(serverUri, httpHeaders);
        return new BasicChannelInitializerClient(serializer, codec, useSsl);

    }

    private URI concatUri(final URI serverUri, final String channelName) {
        if (channelName == null || "".equals(channelName)) {
            return serverUri;
        }
        final String basePath = serverUri.getPath();
        final String channelPath = basePath + (basePath.endsWith("/") ? "" : "/") + channelName;
        try {
            // Use this complicated URI constructor so that the raw channelName is encoded correctly to a path.
            return new URI(serverUri.getScheme(), null, serverUri.getHost(), serverUri.getPort(), channelPath, null,
                    null);
        } catch (final URISyntaxException e) {
            throw new SynchronizeFXException("The URI to connect to the server could not be calculated.", e);
        }
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
