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

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;

import de.saxsys.synchronizefx.core.clientserver.CommandTransferServer;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackServer;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;

/**
 * A single channel that can be used by a single {@link SynchronizeFxServer}.
 * 
 * @author Raik Bieniek
 */
class SynchronizeFXWebsocketChannel implements CommandTransferServer {

    private final Serializer serializer;
    private final SychronizeFXWebsocketServer parent;

    private final List<Session> connections = new LinkedList<>();
    private final Map<Session, ExecutorService> connectionThreads = new HashMap<>();

    private NetworkToTopologyCallbackServer callback;

    /**
     * Initializes an instance with all its dependencies.
     * 
     * @param parent The server that created and manages this channel.
     * @param serializer Used to deserialize commands from other peers and serialize commands for other peers.
     */
    SynchronizeFXWebsocketChannel(final SychronizeFXWebsocketServer parent, final Serializer serializer) {
        this.parent = parent;
        this.serializer = serializer;
    }

    /**
     * Inform this channel that a new client has connected to it.
     * 
     * @param session The client that has connected.
     */
    void newClient(final Session session) {
        synchronized (connections) {
            callback.onConnect(session);
        }
    }

    /**
     * Inform this channel that one of its client has send a message.
     * 
     * @param message The message that was send.
     * @param session The client that send the message.
     */
    void newMessage(final byte[] message, final Session session) {
        callback.recive(serializer.deserialize(message), session);
    }

    /**
     * Informs this {@link CommandTransferServer} that a client connection got closed.
     * 
     * @param connection The connection that was closed
     */
    void connectionCloses(final Session connection) {
        synchronized (connections) {
            final ExecutorService executorService = connectionThreads.get(connection);
            if (executorService != null) {
                executorService.shutdownNow();
            }
            connectionThreads.remove(connection);
            connections.remove(connection);
        }
    }

    /**
     * The amount of currently connected clients.
     * 
     * @return The connected client count.
     */
    int getCurrentlyConnectedClientCount() {
        synchronized (connections) {
            return connections.size();
        }
    }

    // CommandTransferServer

    @Override
    public void setTopologyLayerCallback(final NetworkToTopologyCallbackServer callback) {
        this.callback = callback;
    }

    @Override
    public void start() throws SynchronizeFXException {
        // Nothing todo
    }

    @Override
    public void onConnectFinished(final Object client) {
        synchronized (connections) {
            final Session session = (Session) client;
            connections.add(session);

            // create the thread for sending
            connectionThreads.put(session, Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(final Runnable runnable) {
                    final Thread thread = new Thread(runnable,
                            "synchronizefx client connection thread-" + System.identityHashCode(runnable));
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(final Thread t, final Throwable e) {
                            handleClientError(session, e);
                        }
                    });
                    return thread;
                }
            }));
        }
    }

    @Override
    public void send(final List<Command> commands, final Object destination) {
        byte[] buffer;
        try {
            buffer = serializer.serialize(commands);
        } catch (final SynchronizeFXException e) {
            shutdown();
            callback.onFatalError(e);
            return;
        }
        send(buffer, (Session) destination);
    }

    @Override
    public void sendToAll(final List<Command> commands) {
        sendToAllExcept(commands, null);
    }

    @Override
    public void sendToAllExcept(final List<Command> commands, final Object nonReciver) {
        final byte[] buffer;
        try {
            buffer = serializer.serialize(commands);
        } catch (final SynchronizeFXException e) {
            shutdown();
            callback.onFatalError(e);
            return;
        }
        synchronized (connections) {
            for (final Session connection : connections) {
                if (connection != nonReciver) {
                    send(buffer, connection);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        synchronized (connections) {
            parent.channelCloses(this);
            for (final Session connection : connections) {
                final ExecutorService executorService = connectionThreads.get(connection);
                try {
                    // If there is no executor service for a client, it may has already been shut down.
                    if (executorService != null) {
                        executorService.shutdownNow();
                    }
                    connection
                            .close(new CloseReason(CloseCodes.GOING_AWAY, "This SynchronizeFX channel is closed now."));
                } catch (final IOException e) {
                    callback.onClientConnectionError(connection,
                            new SynchronizeFXException("Failed to close the connection to a connected client.", e));
                } finally {
                    connectionThreads.remove(connection);
                }
            }
            connections.clear();
        }
        callback = null;
    }

    /**
     * Sends send the result of {@link Serializer#serialize(List)} to a destination.
     * 
     * @param buffer the bytes to send.
     * @param destination The peer to send to.
     */
    private void send(final byte[] buffer, final Session destination) {
        synchronized (connections) {
            // execute asynchronously to avoid slower clients from interfering with faster clients
            final ExecutorService connectionThread = connectionThreads.get(destination);
            if (connectionThread == null) {
                // Maybe the client has disconnected in the mean time.
                return;
            }
            connectionThread.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // for the case that the runnable was committed shortly before the connection was closed.
                        if (destination.isOpen()) {
                            // FIXME replace with getAsyncRemote and removeconnectionThreads as soon as
                            // getAsyncRemote on tomcat is thread-safe
                            destination.getBasicRemote().sendBinary(ByteBuffer.wrap(buffer));
                        }
                    } catch (final IOException e) {
                        try {
                            if (destination.isOpen()) {
                                destination.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, "Failed to send data."));
                            }
                        } catch (final IOException e1) {
                            // The outer exception already indicated that something went wrong.
                            ignore(e1);
                        }
                        handleClientError(destination, e);
                        connectionCloses(destination);
                    }
                }
            });
        }
    }

    private void handleClientError(final Session destination, final Throwable e) {
        if (!destination.isOpen()) {
            // The connection may has been closed in the mean time.
            return;
        }
        callback.onClientConnectionError(destination,
                new SynchronizeFXException("An error in the communication with a client occurred.", e));
    }

    private void ignore(final IOException e) {
    }
}
