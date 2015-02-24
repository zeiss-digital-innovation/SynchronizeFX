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

import java.util.List;
import java.util.concurrent.Executor;

import javafx.application.Platform;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.MetaModel;
import de.saxsys.synchronizefx.core.metamodel.TopologyLayerCallback;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The internal implementation that does all the work for {@link SynchronizeFxClient}.
 * 
 * The purpose of this class is to hide methods that are meant to be used by the framework from the user.
 * 
 * @author Raik Bieniek
 */
class DomainModelClient implements NetworkToTopologyCallbackClient, TopologyLayerCallback {

    private static final Logger LOG = LoggerFactory.getLogger(DomainModelClient.class);

    private final ClientCallback clientCallback;
    private final MetaModel meta;
    private final CommandTransferClient networkLayer;
    private final Executor changeExecutor;

    // CHECKSTYLE:OFF The signature for the other constructor is to long to fit in 120 characters
    /**
     * @see SynchronizeFxClient#SynchronizeFxClient(CommandTransferClient, Serializer, ClientCallback)
     * @param networkLayer see
     *            {@link SynchronizeFxClient#SynchronizeFxClient(CommandTransferClient, Serializer, ClientCallback)}
     * @param clientCallback see
     *            {@link SynchronizeFxClient#SynchronizeFxClient(CommandTransferClient, Serializer, ClientCallback)}
     */
    // CHECKSTYLE:ON
    public DomainModelClient(final CommandTransferClient networkLayer, final ClientCallback clientCallback) {
        this(networkLayer, clientCallback, new ExecuteInJavaFXThread());
    }

    // CHECKSTYLE:OFF The signature for the other constructor is to long to fit in 120 characters
    /**
     * @see SynchronizeFxClient#SynchronizeFxClient(CommandTransferClient, Serializer, ClientCallback)
     * @param networkLayer see
     *            {@link SynchronizeFxClient#SynchronizeFxClient(CommandTransferClient, Serializer, ClientCallback)}
     * @param clientCallback see
     *            {@link SynchronizeFxClient#SynchronizeFxClient(CommandTransferClient, Serializer, ClientCallback)}
     * @param changeExecutor see
     *            {@link SynchronizeFxClient#SynchronizeFxClient(CommandTransferClient, ClientCallback, Executor)}
     */
    // CHECKSTYLE:ON
    public DomainModelClient(final CommandTransferClient networkLayer, final ClientCallback clientCallback,
            final Executor changeExecutor) {
        this.clientCallback = clientCallback;
        this.networkLayer = networkLayer;
        this.changeExecutor = changeExecutor;
        networkLayer.setTopologyCallback(this);

        meta = new MetaModel(this);
    }

    @Override
    public void recive(final List<Command> commands) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Client recived commands " + commands);
        }
        changeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                meta.execute(commands);
            }
        });
    }

    @Override
    public void sendCommands(final List<Command> commands) {
        networkLayer.send(commands);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Client sent commands " + commands);
        }
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        clientCallback.onError(error);
    }

    @Override
    public void onServerDisconnect() {
        clientCallback.onServerDisconnect();
    }

    @Override
    public void domainModelChanged(final Object root) {
        changeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                clientCallback.modelReady(root);
            }
        });
    }

    /**
     * @see SynchronizeFxClient#connect()
     */
    public void connect() {
        try {
            networkLayer.connect();
        } catch (SynchronizeFXException e) {
            clientCallback.onError(e);
        }
    }

    /**
     * @see SynchronizeFxClient#disconnect()
     */
    public void disconnect() {
        networkLayer.disconnect();
    }

    /**
     * Executes changes to the users domain model in the JavaFX Thread.
     */
    private static class ExecuteInJavaFXThread implements Executor {
        @Override
        public void execute(final Runnable change) {
            Platform.runLater(change);
        }
    }
}
