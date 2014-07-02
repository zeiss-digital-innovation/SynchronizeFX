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

package de.saxsys.synchronizefx.core.inmemorypeers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.saxsys.synchronizefx.core.clientserver.MessageTransferServer;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackServer;
import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An in-memory server implementation that can do changes to its domain model in its own thread.
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 *
 * @param <T>
 *            The type of the domain model.
 */
public class InMemoryServer<T> implements MessageTransferServer {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryServer.class);
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<InMemoryClient<T>> clients = new ArrayList<>();

    private final T model;

    private NetworkToTopologyCallbackServer callback;

    /**
     * @param model
     *            The model that should be served to {@link InMemoryClient}s
     */
    public InMemoryServer(final T model) {
        this.model = model;
    }

    @Override
    public void setTopologyLayerCallback(final NetworkToTopologyCallbackServer callback) {
        this.callback = callback;
    }

    @Override
    public void sendToAll(final List<Object> messages) {
        sendToAllExcept(messages, null);
    }

    @Override
    public void sendToAllExcept(final List<Object> messages, final Object nonReciver) {
        for (final InMemoryClient<T> client : clients) {
            if (!client.equals(nonReciver)) {
                send(messages, client);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void send(final List<Object> messages, final Object client) {
        ((InMemoryClient<T>) client).recieve(messages);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onConnectFinished(final Object client) {
        // this will only happen in the server thread
        clients.add((InMemoryClient<T>) client);
    }

    @Override
    public void start() throws SynchronizeFXException {
        // nothing needs to be done
    }

    @Override
    public void shutdown() {
        executeInServerThread(new Runnable() {
            @Override
            public void run() {
                for (final InMemoryClient<T> client : clients) {
                    client.serverShutsDown();
                }
                clients.clear();
            }
        });
    }

    /**
     * Schedules a runnable to be executed in the servers thread.
     * 
     * @param runnable
     *            The execution to schedule
     */
    public void executeInServerThread(final Runnable runnable) {
        executor.execute(runnable);
    }
    
    /**
     * Starts a {@link SynchronizeFxServer} with this object as client implementation.
     *  
     *  <p>
     *  The startup is not done in Server thread but in the thread of the method caller. 
     *  </p>
     *  
     * @return The started {@link SynchronizeFxServer}
     */
    public SynchronizeFxServer startSynchronizeFxServer() {
        final SynchronizeFxServer server = new SynchronizeFxServer(model, this, new ServerCallback() {
            @Override
            public void onError(final SynchronizeFXException error) {
                LOG.error("An SynchronizeFX exception occured. ", error);
            }
        });
        server.start();
        return server;
    }

    /**
     * The model that this server serves.
     * 
     * @return the model
     */
    public T getModel() {
        return model;
    }

    /**
     * Connects a new client.
     * 
     * @param client
     *            The client that wants to connect.
     */
    void connect(final InMemoryClient<T> client) {
        executeInServerThread(new Runnable() {
            @Override
            public void run() {
                callback.onConnect(client);
            }
        });
    }

    /**
     * Disconnects a client.
     * 
     * @param client
     *            The client to disconnect.
     */
    void disconnect(final InMemoryClient<T> client) {
        executeInServerThread(new Runnable() {
            @Override
            public void run() {
                clients.remove(client);
            }
        });
    }
    
    /**
     * Receive messages from a client.
     * 
     * @param client The client that send the changes.
     * @param messages The changes that where received.
     */
    void recive(final InMemoryClient<T> client, final List<Object> messages) {
        executeInServerThread(new Runnable() {
            @Override
            public void run() {
                callback.recive(messages, client);
            }
        });
    }
}
