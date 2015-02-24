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

package de.saxsys.synchronizefx.core.inmemorypeers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.saxsys.synchronizefx.core.clientserver.ClientCallback;
import de.saxsys.synchronizefx.core.clientserver.CommandTransferClient;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An in-memory client implementation that can do changes to its domain model in its own thread.
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 *
 * @param <T> The type of the domain model.
 */
public class InMemoryClient<T> implements CommandTransferClient {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryClient.class);

    /**
     * The time to wait for the domain model to get ready.
     */
    private static final long WAIT_FOR_DOMAIN_MODEL = 1000;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final InMemoryServer<T> server;
    private T model;

    private NetworkToTopologyCallbackClient callback;

    /**
     * @param server The server to connect to.
     */
    public InMemoryClient(final InMemoryServer<T> server) {
        this.server = server;
    }

    @Override
    public void setTopologyCallback(final NetworkToTopologyCallbackClient callback) {
        this.callback = callback;
    }

    @Override
    public void send(final List<Command> commands) {
        server.recive(this, commands);
    }

    @Override
    public void connect() throws SynchronizeFXException {
        server.connect(this);
    }

    @Override
    public void disconnect() {
        server.disconnect(this);
    }

    /**
     * Starts a {@link SynchronizeFxClient} with this object as client implementation.
     * 
     * <p>
     * The startup is not done in Client thread but in the thread of the method caller. All changes done to
     * properties by SynchronizeFX are done using the client thread.
     * </p>
     * 
     * @return The started {@link SynchronizeFxClient}
     */
    public SynchronizeFxClient startSynchronizeFxClient() {
        final Object waitForDomainModel = new Object();
        final SynchronizeFxClient client = new SynchronizeFxClient(this, new ClientCallback() {
            @Override
            public void onServerDisconnect() {
                // do nothing
            }

            @Override
            public void onError(final SynchronizeFXException error) {
                LOG.error("An SynchronizeFX exception occured. ", error);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void modelReady(final Object model) {
                InMemoryClient.this.model = (T) model;
                notifyDomainModelReady(waitForDomainModel);
            }
        }, executor);
        client.connect();
        waitForDomainModelReady(waitForDomainModel);
        return client;
    }

    /**
     * Schedules a runnable to be executed in the clients thread.
     * 
     * @param runnable The execution to schedule
     */
    public void executeInClientThread(final Runnable runnable) {
        executor.execute(runnable);
    }

    /**
     * The model that was retrieved from the server.
     * 
     * @return The model
     */
    public T getModel() {
        return model;
    }

    /**
     * Recives commands from the server.
     * 
     * @param commands The commands the client should receive.
     */
    void recieve(final List<Command> commands) {
        executeInClientThread(new Runnable() {
            @Override
            public void run() {
                callback.recive(commands);
            }
        });
    }

    /**
     * Informs the client that the server shuts down.
     */
    void serverShutsDown() {
        executeInClientThread(new Runnable() {
            @Override
            public void run() {
                callback.onServerDisconnect();
            }
        });
    }

    private void notifyDomainModelReady(final Object waitObject) {
        synchronized (waitObject) {
            waitObject.notify();
        }
    }

    private void waitForDomainModelReady(final Object waitObject) {
        synchronized (waitObject) {
            try {
                waitObject.wait(WAIT_FOR_DOMAIN_MODEL);
            } catch (final InterruptedException e) {
                throw new RuntimeException("Could not wait for the domain model from the server.", e);
            }
        }
    }
}
