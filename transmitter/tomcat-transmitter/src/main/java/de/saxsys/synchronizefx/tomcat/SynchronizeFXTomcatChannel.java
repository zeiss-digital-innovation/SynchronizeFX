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
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import de.saxsys.synchronizefx.core.clientserver.CommandTransferServer;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackServer;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of clients that share the same synchronized model.
 * 
 * @author Raik Bieniek
 */
class SynchronizeFXTomcatChannel implements CommandTransferServer {

    private static final Logger LOG = LoggerFactory.getLogger(SynchronizeFXTomcatChannel.class);

    private final SynchronizeFXTomcatServlet parent;
    private final Serializer serializer;
    private NetworkToTopologyCallbackServer callback;

    private final List<MessageInbound> connections = new LinkedList<>();
    private final Map<MessageInbound, ExecutorService> connectionThreads = new HashMap<>();

    /**
     * Initializes an instance with all its dependencies.
     * 
     * @param parent Used to inform when this channel was closed.
     * @param serializer The serializer that should be used to send data to clients.
     */
    SynchronizeFXTomcatChannel(final SynchronizeFXTomcatServlet parent, final Serializer serializer) {
        this.parent = parent;
        this.serializer = serializer;
    }

    /**
     * Sends send the result of {@link Serializer#serialize(List)} to a destination.
     * 
     * @param buffer the bytes to send.
     * @param destination The peer to send to.
     */
    public void send(final byte[] buffer, final Object destination) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending from thread: id: " + Thread.currentThread().getName() + ", name: "
                    + Thread.currentThread().getName());
        }

        final WsOutbound outbound = ((MessageInbound) destination).getWsOutbound();

        final ExecutorService executorService = connectionThreads.get(destination);
        // execute asynchronously to avoid slower clients from interfering with faster clients
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    outbound.writeBinaryMessage(ByteBuffer.wrap(buffer));
                } catch (final IOException e) {
                    LOG.warn("Sending data to a client failed. Closing connection to this client.");
                    try {
                        outbound.close(1002, null);
                        // CHECKSTYLE:OFF
                    } catch (final IOException e1) {
                        // Maybe the connection is already closed. This is no exceptional state but rather the
                        // default in
                        // this case. So it's safe to ignore this exception.
                    }
                    // CHECKSTYLE:ON
                    connectionCloses((SynchronizeFXTomcatConnection) destination);
                }
            }
        });
    }

    // CommandTransferServer

    @Override
    public void onConnectFinished(final Object client) {
        synchronized (connections) {
            final SynchronizeFXTomcatConnection syncFxClient = (SynchronizeFXTomcatConnection) client;
            connections.add(syncFxClient);
            connectionThreads.put(syncFxClient, Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(final Runnable runnable) {
                    final Thread thread = new Thread(runnable,
                            "synchronizefx client connection thread-" + System.identityHashCode(runnable));
                    thread.setDaemon(true);
                    return thread;
                }
            }));
        }
    }

    @Override
    public void setTopologyLayerCallback(final NetworkToTopologyCallbackServer callback) {
        this.callback = callback;
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
        send(buffer, destination);
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
            // This ensures that no client is added or removed for the connection list while iterating over it.
            // This ensures also that all clients get messages in the correct order for the case that sendToAllExcept
            // as already called a second time.
            for (final MessageInbound connection : connections) {
                if (connection != nonReciver) {
                    send(buffer, connection);
                }
            }
        }
    }

    @Override
    public void start() throws SynchronizeFXException {
        // Starting is done by starting the servlet in the way servlets usualy get started. So this method does
        // nothing.
    }

    /**
     * Disconnects all clients and makes the servlet refuse new connections.
     */
    @Override
    public void shutdown() {
        synchronized (connections) {
            parent.channelCloses(this);
            for (final MessageInbound connection : connections) {
                try {
                    connection.getWsOutbound().close(0, null);
                } catch (final IOException e) {
                    LOG.error("Connection [" + connection.toString() + "] can't be closed.", e);
                } finally {
                    final ExecutorService executorService = connectionThreads.get(connection);
                    if (executorService != null) {
                        executorService.shutdown();
                    }
                    connectionThreads.remove(connection);
                }
            }
            connections.clear();
        }
        callback = null;
    }

    // Used by SynchronizeFXTomcatConnection objects

    /**
     * Informs this {@link CommandTransferServer} that a new client connection is ready.
     * 
     * @param connection The connection that just got ready.
     */
    void clientConnectionReady(final SynchronizeFXTomcatConnection connection) {
        LOG.info("Client connected.");
        callback.onConnect(connection);
    }

    /**
     * Informs this {@link CommandTransferServer} that a client received a command.
     * 
     * @param message The message containing the received command.
     * @param sender The connection that received the message.
     */
    void recivedMessage(final ByteBuffer message, final SynchronizeFXTomcatConnection sender) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Received a message in thread: id: " + Thread.currentThread().getName() + ", name: "
                    + Thread.currentThread().getName());
        }
        List<Command> commands;
        try {
            commands = serializer.deserialize(message.array());
        } catch (final SynchronizeFXException e) {
            try {
                sender.getWsOutbound().close(0, null);
            } catch (final IOException e1) {
                callback.onClientConnectionError(new SynchronizeFXException(e1));
            }
            callback.onClientConnectionError(e);
            return;
        }
        synchronized (callback) {
            callback.recive(commands, sender);
        }
    }

    /**
     * Informs this {@link CommandTransferServer} that a client connection got closed.
     * 
     * @param connection The connection that was closed
     */
    void connectionCloses(final SynchronizeFXTomcatConnection connection) {
        LOG.info("Client connection closed.");
        synchronized (connections) {
            final ExecutorService executorService = connectionThreads.get(connection);
            if (executorService != null) {
                executorService.shutdown();
            }
            connectionThreads.remove(connection);
            connections.remove(connection);
        }
    }

    /**
     * The number of clients that are currently connected to this server.
     * 
     * @return The client count.
     */
    int getCurrentlyConnectedClientCount() {
        synchronized (connections) {
            return connections.size();
        }
    }
}
