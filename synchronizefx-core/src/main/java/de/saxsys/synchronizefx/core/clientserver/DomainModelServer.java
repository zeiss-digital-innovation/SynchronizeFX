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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.CommandsForDomainModelCallback;
import de.saxsys.synchronizefx.core.metamodel.MetaModel;
import de.saxsys.synchronizefx.core.metamodel.TopologyLayerCallback;
import de.saxsys.synchronizefx.core.metamodel.commands.ClearReferences;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The internal implementation that does all the work for {@link SynchronizeFxServer}.
 * 
 * The purpose of this class is to hide methods that are meant to be used by the framework from the user.
 * 
 * @author Raik Bieniek
 */
class DomainModelServer implements NetworkToTopologyCallbackServer, TopologyLayerCallback {
    private static final Logger LOG = LoggerFactory.getLogger(DomainModelServer.class);

    private final CommandTransferServer networkLayer;
    private final MetaModel meta;
    private final ServerCallback serverCallback;
    private final Executor changeExecutor;

    private boolean executorCreatedLocaly;

    // CHECKSTYLE:OFF The signature for the other constructor is to long to fit in 120 characters
    /**
     * @see SynchronizeFxServer#SynchronizeFxServer(Object, MessageTransferServer, Serializer, UserCallbackServer);
     * @param model see
     *            {@link SynchronizeFxServer#SynchronizeFxServer(Object, CommandTransferServer, Serializer, ServerCallback)}
     * @param networkLayer see
     *            {@link SynchronizeFxServer#SynchronizeFxServer(Object, CommandTransferServer, Serializer, ServerCallback)}
     * @param serverCallback see
     *            {@link SynchronizeFxServer#SynchronizeFxServer(Object, CommandTransferServer, Serializer, ServerCallback)}
     */
    // CHECKSTYLE:ON
    DomainModelServer(final Object model, final CommandTransferServer networkLayer,
            final ServerCallback serverCallback) {
        this(model, networkLayer, serverCallback, Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                final Thread thread = new Thread(r, "synchronizefx model change thread-" + System.identityHashCode(r));
                thread.setDaemon(true);
                return thread;
            }
        }));
        this.executorCreatedLocaly = true;
    }

    // CHECKSTYLE:OFF The signature for the other constructor is to long to fit in 120 characters
    /**
     * @see SynchronizeFxServer#SynchronizeFxServer(Object, MessageTransferServer, Serializer, UserCallbackServer);
     * @param model see
     *            {@link SynchronizeFxServer#SynchronizeFxServer(Object, CommandTransferServer, Serializer, ServerCallback)}
     * @param networkLayer see
     *            {@link SynchronizeFxServer#SynchronizeFxServer(Object, CommandTransferServer, Serializer, ServerCallback)}
     * @param serverCallback see
     *            {@link SynchronizeFxServer#SynchronizeFxServer(Object, CommandTransferServer, Serializer, ServerCallback)}
     * @param changeExecutor see
     *            {@link SynchronizeFxServer#SynchronizeFxServer(Object, CommandTransferServer, Serializer, ServerCallback)}
     */
    // CHECKSTYLE:ON
    DomainModelServer(final Object model, final CommandTransferServer networkLayer, final ServerCallback serverCallback,
            final Executor changeExecutor) {
        this.networkLayer = networkLayer;
        this.serverCallback = serverCallback;
        this.meta = new MetaModel(this, model);
        this.changeExecutor = changeExecutor;
        networkLayer.setTopologyLayerCallback(this);
        this.executorCreatedLocaly = false;
    }

    @Override
    public void recive(final List<Command> commands, final Object sender) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Server recived commands " + commands);
        }

        // FIXME Filtering the commands is a temporary hack. When the implementation is finished, clients
        // should be able to handle receiving commands they have created on there own. At the moment this is only
        // true
        // for some types of commands. Therefore these command types have to be separated from the other commands.

        final List<Command> filteredCommands = new LinkedList<>();

        for (final Command command : commands) {
            if (senderReceivingOwnCommandHandable(command)) {
                filteredCommands.add(command);
            }
        }

        changeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                meta.execute(commands);
                networkLayer.sendToAllExcept(commands, sender);
                networkLayer.send(filteredCommands, sender);
            }
        });
    }

    private boolean senderReceivingOwnCommandHandable(final Command command) {
        return command instanceof SetPropertyValue || command instanceof ClearReferences
                || command instanceof ListCommand;
    }

    @Override
    public void sendCommands(final List<Command> commands) {
        networkLayer.sendToAll(commands);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Server sent commands " + commands);
        }
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        serverCallback.onError(error);
    }

    @Override
    public void domainModelChanged(final Object root) {
        serverCallback.onError(
                new SynchronizeFXException("Domain model has changed on the server side. " + "This is not supported. "
                        + "If you want to serve a new domain model, consider creating an a new Server "
                        + "or create a meta root that holds the real root object of your domain model "
                        + "wich than can be exchanged without problems."));
    }

    /**
     * Sends the current domain model to a newly connecting client.
     * 
     * @param newClient An object that represent the new client that connected.
     * @see IncommingEventHandlerServer#onConnect(Object)
     */
    @Override
    public void onConnect(final Object newClient) {
        meta.commandsForDomainModel(new CommandsForDomainModelCallback() {
            @Override
            public void commandsReady(final List<Command> commands) {
                networkLayer.onConnectFinished(newClient);
                networkLayer.send(commands, newClient);
            }
        });
        // TODO networkLayer onConnectFinished(); javadoc that networklayer should then enable sendToAll for new
        // client.

    }

    /**
     * Logs the unexpected disconnection of an client.
     * 
     * Connection errors to single clients are usually non fatal. The server can still work correctly for the other
     * clients. Because of that this type of error is just logged here and not passed to the user.
     *
     * @param client An object that represent the client where the error occurred.
     * @param e an exception that describes the problem.
     * @see NetworkToTopologyCallbackServer#onClientConnectionError(SynchronizeFXException)
     */
    @Override
    public void onClientConnectionError(final Object client, final SynchronizeFXException e) {
        serverCallback.onClientConnectionError(client, e);
    }

    @Override
    public void onFatalError(final SynchronizeFXException e) {
        serverCallback.onError(e);
    }

    /**
     * @see SynchronizeFxServer#start();
     */
    public void start() {
        try {
            networkLayer.start();
        } catch (SynchronizeFXException e) {
            serverCallback.onError(e);
        }
    }

    /**
     * @see SynchronizeFxServer#getModelChangeExecutor()
     * @return The executor for model changes.
     */
    public Executor getModelChangeExecutor() {
        return changeExecutor;
    }

    /**
     * @see SynchronizeFxServer#shutdown()
     */
    public void shutdown() {
        if (executorCreatedLocaly) {
            // If the model change executor was created by this class, this class also has to shut it down.
            ((ExecutorService) changeExecutor).shutdown();
        }
        networkLayer.shutdown();
    }
}
