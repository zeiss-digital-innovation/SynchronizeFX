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

package de.saxsys.synchronizefx.core.clientserver;

import java.util.concurrent.Executor;

import javafx.beans.property.Property;

/**
 * This class implements a server that makes a JavaFX model available over the network. All fields of the model that
 * implement the {@link Property} interface will be synchronized between all clients.
 * 
 * @author raik.bieniek
 * 
 */
public class SynchronizeFxServer {

    private DomainModelServer impl;

    /**
     * Sets up everything that is needed to serve a domain model.
     * 
     * <p>
     * This method doesn't start the server. Use {@link SynchronizeFxServer#start()} for that. Using this
     * constructor, all changes done to JavaFX properties will be executed directly in the current thread.
     * </p>
     * 
     * @param model The root object of the domain model to serve.
     * @param networkLayer An object that does the network transfer and optionally the serialization of the data
     *            generated to keep models synchron.
     * @param callback Used to inform the user of this class on errors. The methods in the callback are not called
     *            before you call {@link SynchronizeFxServer#start()}.
     */
    public SynchronizeFxServer(final Object model, final CommandTransferServer networkLayer,
            final ServerCallback callback) {
        impl = new DomainModelServer(model, networkLayer, callback);
    }

    /**
     * Sets up everything that is needed to serve a domain model.
     * 
     * <p>
     * This method doesn't start the server. Use {@link SynchronizeFxServer#start()} for that. Using this
     * constructor, all changes done to JavaFX properties will be executed by an {@link Executor} passed by the user.
     * </p>
     * 
     * @param model The root object of the domain model to serve.
     * @param networkLayer An object that does the network transfer and optionally the serialization of the data
     *            generated to keep models synchron.
     * @param callback Used to inform the user of this class on errors. The methods in the callback are not called
     *            before you call {@link SynchronizeFxServer#start()}.
     * @param changeExecutor An executor used to execute all changes done to JavaFX properties in the model. This
     *            executor must garuantee that only one runnable passed to it is executed at the same time (e.g. like
     *            a single thread executor). <i>IMPORTANT</i> This executor must be used by other code too to execute
     *            all changes on the model in order to guarantee synchronity between the server and all connected
     *            clients.
     */
    public SynchronizeFxServer(final Object model, final CommandTransferServer networkLayer,
            final ServerCallback callback, final Executor changeExecutor) {
        impl = new DomainModelServer(model, networkLayer, callback, changeExecutor);
    }

    /**
     * Starts the server and accepts incoming client connections.
     */
    public void start() {
        impl.start();
    }

    /**
     * The {@link Executor} that should be used for all changes on JavaFX properties in the model in order to
     * guarantee synchronity to the connected clients.
     * 
     * <p>
     * This executor is much like platform thread for JavaFX clients. Like with the platform thread, runnables passed
     * should execute quickly to not block the server.
     * </p>
     * 
     * <p>
     * If an {@link Executor} was passed in the constructor this method will returned it. In this case it is also
     * possible to use this passed {@link Executor} directly instead of using this method. If the {@link Executor}
     * was not passed in the constructor this method will return an automatically created one.
     * </p>
     * 
     * @return The {@link Executor} for model changes.
     */
    public Executor getModelChangeExecutor() {
        return impl.getModelChangeExecutor();
    }

    /**
     * Shuts down the server
     * 
     * <p>
     * Before the shutdown, all clients which are still connected are disconnected. If the {@link Executor} for model
     * changes was not passed in the constructor it will be shutdown by this method too. This means
     * {@link #getModelChangeExecutor()} will not accept any further tasks. If the {@link Executor} for model changes
     * was passed in the constructor it will not be closed. It is the responsibility of the user to do so.
     * </p>
     */
    public void shutdown() {
        impl.shutdown();
    }
}
