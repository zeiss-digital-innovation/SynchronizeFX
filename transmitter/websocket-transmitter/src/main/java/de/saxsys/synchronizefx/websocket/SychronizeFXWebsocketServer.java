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

package de.saxsys.synchronizefx.websocket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.Endpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;

import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;

/**
 * An server-side network layer implementation for SynchronizeFX that uses the JSR 356 Websocket API.
 * 
 * <p>
 * The JSR 356 Websocket API provides multiple possibilities to create a Websocket endpoint, e.g. by implementing
 * {@link Endpoint} or using annotations like {@link OnOpen}. To support all of them this class contains the methods
 * {@link #onOpen(Session, String)} and {@link #onClose(Session, String)}. You must pass through the respective
 * events from your Websocket endpoint to this methods. The {@link OnError} methods needs to be handled by the
 * Websocket endpoint itself. Also {@link OnMessage} does not need to be passed to this class as this class registers
 * message handlers in self through the {@link #onOpen(Session, String)} method.
 * </p>
 * 
 * <p>
 * All methods of this class are Thread-safe.
 * </p>
 * 
 * <p>
 * A single instance of this class can handle multiple {@link SynchronizeFxServer}s at the same time. Each managed
 * {@link SynchronizeFxServer} is identified by a channel name. Most methods in this class need the channel at which
 * events occurred passed to them. Channels can be identified e.g by using the {@link PathParam} annotation or by
 * passing something like <code>"default"</code> to this class when multiple channels aren't needed.
 * </p>
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
 * @author Raik Bieniek
 */
public class SychronizeFXWebsocketServer {

    private final Map<String, SynchronizeFXWebsocketChannel> channels = new HashMap<>();
    // Use "channels" for synchronized access to "servers".
    private final Map<SynchronizeFxServer, SynchronizeFXWebsocketChannel> servers = new HashMap<>();
    private boolean isCurrentlyShutingDown;
    private final Serializer serializer;

    /**
     * Initializes an instance with all its dependencies.
     * 
     * @param serializer The serializer that should be used to serialize and deserialize SynchronizeFX commands.
     */
    public SychronizeFXWebsocketServer(final Serializer serializer) {
        this.serializer = serializer;

    }

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

            final SynchronizeFXWebsocketChannel channel = new SynchronizeFXWebsocketChannel(this, serializer);
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
            final SynchronizeFXWebsocketChannel channel = servers.get(server);
            if (channel == null) {
                throw new IllegalArgumentException(
                        "The SynchronizeFXServer passed as argument was not created by this instance "
                                + "or has already been shut down.");
            }
            return channel.getCurrentlyConnectedClientCount();
        }
    }

    /**
     * Pass {@link OnOpen} events of the Websocket API to this method to handle new clients.
     * 
     * @param session The client that has connected.
     * @param channelName The name of the channel this client connected too.
     * @throws IllegalArgumentException If the channel passed as argument does not exist.
     */
    public void onOpen(final Session session, final String channelName) {
        final SynchronizeFXWebsocketChannel channel = getChannelOrFail(channelName);

        channel.newClient(session);

    }

    /**
     * Pass {@link OnClose} events of the Websocket API to this method to handle the event of a disconnected client.
     * 
     * @param session The client that has disconnected.
     * @param channelName The name of the channel this client is connected too.
     * @throws IllegalArgumentException If the channel passed as argument does not exist.
     */
    public void onClose(final Session session, final String channelName) {
        final SynchronizeFXWebsocketChannel channel = getChannelOrFail(channelName);

        channel.connectionCloses(session);
    }

    /**
     * Shuts down this servers with all its channels and disconnects all remaining clients.
     */
    public void shutDown() {
        synchronized (channels) {
            isCurrentlyShutingDown = true;
            for (final SynchronizeFXWebsocketChannel server : channels.values()) {
                server.shutdown();
            }
            servers.clear();
            channels.clear();
            isCurrentlyShutingDown = false;

        }
    }

    // Used by SynchronizeFXWebsocketChannel

    /**
     * This is called when a channel is closing and should therefore no longer accept new connections.
     * 
     * @param synchronizeFXTomcatChannel The channel that closes.
     */
    void channelCloses(final SynchronizeFXWebsocketChannel synchronizeFXTomcatChannel) {
        synchronized (channels) {
            if (isCurrentlyShutingDown) {
                return;
            }
            // Maybe a bit inefficient but the alternative would be to pass cannelName and SynchronizeFxServer to the
            // channel just to be able to close itself.
            final Iterator<Entry<String, SynchronizeFXWebsocketChannel>> channelIterator =
                    channels.entrySet().iterator();
            while (channelIterator.hasNext()) {
                if (channelIterator.next().getValue().equals(synchronizeFXTomcatChannel)) {
                    channelIterator.remove();
                }
            }

            final Iterator<Entry<SynchronizeFxServer, SynchronizeFXWebsocketChannel>> serverIterator =
                    servers.entrySet().iterator();
            while (serverIterator.hasNext()) {
                if (serverIterator.next().getValue().equals(synchronizeFXTomcatChannel)) {
                    serverIterator.remove();
                }
            }
        }
    }

    private SynchronizeFXWebsocketChannel getChannelOrFail(final String channelName) {
        synchronized (channels) {
            final SynchronizeFXWebsocketChannel channel = channels.get(channelName);
            if (channel == null) {
                throw new IllegalArgumentException(
                        "A client tried to communicate with a SynchronizeFX channel with the name \"" + channelName
                                + "\" which did not exist.");
            }
            return channel;
        }
    }
}
