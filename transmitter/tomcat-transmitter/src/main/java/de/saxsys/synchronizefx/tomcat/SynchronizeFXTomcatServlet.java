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

package de.saxsys.synchronizefx.tomcat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;

/**
 * An server-side network layer implementation for SynchronizeFX that uses the websocket implementation of Apache
 * Tomcat for the network transfer.
 * 
 * <p>
 * Clients wishing to connect to this implementation the Websocket sub-protocol used should be
 * "v2.websocket.synchronizefx.saxsys.de". It must be ensured that this server and the client use {@link Serializer}
 * implementations that are compatible. Ideally both sides use the same implementations. Each command that is created
 * by {@link Serializer#serialize(java.util.List)} must be send as is in a single websocket binary frame. Each
 * content binary frames must be passed through {@link Serializer#deserialize(byte[])} to reproduce the SynchronizeFX
 * commands.
 * </p>
 * 
 * <p>
 * A single instance of this servlet can handle multiple {@link SynchronizeFxServer}s at the same time. Each managed
 * {@link SynchronizeFxServer} is identified by a channel name. A client wishing to connect to a specific
 * {@link SynchronizeFxServer} must open a connection to <code>&lt;servlet-base-url&gt;/&lt;channel-name&gt;</code>
 * where <code>servlet-base-url</code> is the base URL at which this servlet can be reached and
 * <code>channel-name</code> is the name of the {@link SynchronizeFxServer} the client wants to connect to.
 * </p>
 * 
 * @author Raik Bieniek
 */
public abstract class SynchronizeFXTomcatServlet extends WebSocketServlet {

    private static final long serialVersionUID = -1859780171572536501L;

    private final Map<String, SynchronizeFXTomcatChannel> channels = new HashMap<>();
    // Use "channels" for synchronized access to "servers".
    private final Map<SynchronizeFxServer, SynchronizeFXTomcatChannel> servers = new HashMap<>();
    private boolean isCurrentlyShutingDown;

    /**
     * Returns a {@link Serializer} that should be used to serialize and deserialize the commands of the
     * SynchronizeFX framework.
     * 
     * @return The serializer
     */
    protected abstract Serializer newSerializer();

    /**
     * Creates a new {@link SynchronizeFxServer} that synchronizes it's own model.
     * 
     * <p>
     * Each {@link SynchronizeFxServer} managed by this servlet must have its own channel name.
     * </p>
     * 
     * @param root The root object of the model that should be synchronized.
     * @param channelName The name of the channel at which clients can connect to the new server.
     * @param callback Used to inform the user of this class on errors. The methods in the callback are not called
     *            before you call {@link SynchronizeFxServer#start()}
     * @throws IllegalArgumentException When a {@link SynchronizeFxServer} was already started with the given channel
     *             name and has not yet been shut down.
     * @return The created server
     * @see SynchronizeFxServer#SynchronizeFxServer(Object,
     *      de.saxsys.synchronizefx.core.clientserver.CommandTransferServer, ServerCallback)
     */
    public SynchronizeFxServer newChannel(final Object root, final String channelName, final ServerCallback callback) {
        synchronized (channels) {
            if (channels.containsKey(channelName)) {
                throw new IllegalArgumentException("A new SynchronizeFX channel with the name \"" + channelName
                        + "\" should be created a channel with this name does already exist.");
            }

            final SynchronizeFXTomcatChannel channel = new SynchronizeFXTomcatChannel(this, newSerializer());
            final SynchronizeFxServer server = new SynchronizeFxServer(root, channel, callback);
            channels.put(channelName, channel);
            servers.put(server, channel);
            return server;
        }
    }

    /**
     * The amount of clients that are currently connected to the given {@link SynchronizeFxServer}.
     * 
     * @param server The server thats connection count should be checked.
     * @throws IllegalArgumentException If the {@link SynchronizeFxServer} passed as argument wasn't started by this
     *             servlet or if it has already been shut down.
     * @return The client count
     */
    public int getCurrentlyConnectedClientCount(final SynchronizeFxServer server) {
        synchronized (channels) {
            return servers.get(server).getCurrentlyConnectedClientCount();
        }
    }

    /**
     * Extracts the name of the channel the client that invoked a request wants to connect to.
     * 
     * @param request The request to connect to a channel.
     * @return The channel that the client want's to connect to. It is irrelevant if this channel does or does not
     *         exist.
     */
    protected String getChannelName(final HttpServletRequest request) {
        final String path = request.getPathInfo();
        return path == null ? "" : path.startsWith("/") ? path.substring(1) : path;
    }

    // WebSocketServlet

    /**
     * Disconnect all clients an clear the connections when the handler is destroyed by CDI.
     */
    @Override
    public void destroy() {
        synchronized (channels) {
            isCurrentlyShutingDown = true;
            for (final SynchronizeFXTomcatChannel server : channels.values()) {
                server.shutdown();
            }
            servers.clear();
            channels.clear();
            isCurrentlyShutingDown = false;

        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException {
        // Filter out requests that access an unknown channel.
        final String channelName = getChannelName(req);
        synchronized (channels) {
            if (!channels.containsKey(channelName)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "A channel with the name \"" + channelName + "\" does not exists.");
                return;
            }
        }
        super.doGet(req, resp);
    };

    @Override
    protected StreamInbound createWebSocketInbound(final String subProtocol, final HttpServletRequest request) {
        // TODO validate sub protocol and find out how to refuse connections that send an unsupported sub protocol.
        final String channelName = getChannelName(request);
        synchronized (channels) {
            return new SynchronizeFXTomcatConnection(channels.get(channelName));
        }
    }

    // Used by SynchronizeFXTomcatChannel

    /**
     * This is called when a channel is closing and should therefore no longer accept new connections.
     * 
     * @param synchronizeFXTomcatChannel The channel that closes.
     */
    void channelCloses(final SynchronizeFXTomcatChannel synchronizeFXTomcatChannel) {
        synchronized (channels) {
            if (isCurrentlyShutingDown) {
                return;
            }
        }
        synchronized (channels) {
            // Maybe a bit inefficient but the alternative would be to pass cannelName and SynchronizeFxServer to the
            // channel just to be able to close itself.
            final Iterator<Entry<String, SynchronizeFXTomcatChannel>> channelIterator = channels.entrySet().iterator();
            while (channelIterator.hasNext()) {
                if (channelIterator.next().getValue().equals(synchronizeFXTomcatChannel)) {
                    channelIterator.remove();
                }
            }

            final Iterator<Entry<SynchronizeFxServer, SynchronizeFXTomcatChannel>> serverIterator =
                    servers.entrySet().iterator();
            while (serverIterator.hasNext()) {
                if (serverIterator.next().getValue().equals(synchronizeFXTomcatChannel)) {
                    serverIterator.remove();
                }
            }
        }
    }
}
