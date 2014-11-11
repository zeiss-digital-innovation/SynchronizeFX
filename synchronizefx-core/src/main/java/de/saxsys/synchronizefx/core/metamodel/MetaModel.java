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

package de.saxsys.synchronizefx.core.metamodel;

import java.util.List;
import java.util.concurrent.Executor;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.ModelWalkingSynchronizer.ActionType;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.SetRootElement;
import de.saxsys.synchronizefx.core.metamodel.executors.CommandLogDispatcher;
import de.saxsys.synchronizefx.core.metamodel.executors.RepairingSingleValuePropertyCommandExecutor;
import de.saxsys.synchronizefx.core.metamodel.executors.SimpleSingleValuePropertyCommandExecutor;
import de.saxsys.synchronizefx.core.metamodel.executors.SingleValuePropertyCommandExecutor;

/**
 * Generates and applies commands necessary to keep domain models synchronous.
 */
public class MetaModel {

    private Object root;
    private TopologyLayerCallback topology;

    // server / client independent initialization
    private CommandListCreator creator;
    private WeakObjectRegistry objectRegistry;
    private ValueMapper valueMapper;
    private SilentChangeExecutor silentChangeExecutor;
    private ModelWalkingSynchronizer modelWalkingSynchronizer;

    // server / client dependent initialization
    private final CommandListExecutor executor;
    private final Listeners listeners;

    /**
     * Creates a {@link MetaModel} where the root object of the domain model is received from another node.
     * 
     * @param topology
     *            used to interact with the lower layer which is probably represented by the class that called this
     *            constructor.
     * @param changeExecutor
     *            The executor to use for all changes done to JavaFX properties.
     */
    public MetaModel(final TopologyLayerCallback topology, final Executor changeExecutor) {
        initCommonObjects(topology, changeExecutor);

        // CHECKSTYLE:OFF Because of line length limit. TODO find shorter class names.
        final RepairingSingleValuePropertyCommandExecutor singleValuePropertyExecutor = new RepairingSingleValuePropertyCommandExecutor(
                objectRegistry, new SimpleSingleValuePropertyCommandExecutor(objectRegistry, silentChangeExecutor,
                        valueMapper));
        // CHECKSTYLE:ON
        final CommandLogDispatcher commandLog = new CommandLogDispatcher(singleValuePropertyExecutor);

        this.listeners = new Listeners(objectRegistry, creator, topology, modelWalkingSynchronizer, commandLog);
        silentChangeExecutor.registerListenersToSilence(listeners);
        this.executor = new CommandListExecutor(this, objectRegistry, listeners, silentChangeExecutor, valueMapper,
                singleValuePropertyExecutor);
    }

    /**
     * Creates a {@link MetaModel} which serves a new domain model.
     * 
     * @see MetaModel#MetaModel(TopologyLayerCallback, Executor)
     * @param topology
     *            see {@link MetaModel#MetaModel(TopologyLayerCallback, Executor)}
     * @param root
     *            The root object of the domain model that should be served.
     * @param changeExecutor
     *            The executor to use for all changes done to JavaFX properties.
     */
    public MetaModel(final TopologyLayerCallback topology, final Object root, final Executor changeExecutor) {
        initCommonObjects(topology, changeExecutor);
        this.root = root;

        // CHECKSTYLE:OFF Because of line length limit. TODO find shorter class names.
        final SingleValuePropertyCommandExecutor singleValuePropertyExecutor = new SimpleSingleValuePropertyCommandExecutor(
                objectRegistry, silentChangeExecutor, valueMapper);
        // CHECKSTYLE:ON
        final CommandLogDispatcher commandLog = new CommandLogDispatcher();

        this.listeners = new Listeners(objectRegistry, creator, topology, modelWalkingSynchronizer, commandLog);
        silentChangeExecutor.registerListenersToSilence(listeners);
        this.executor = new CommandListExecutor(this, objectRegistry, listeners, silentChangeExecutor, valueMapper,
                singleValuePropertyExecutor);

        registerListenersOnModel();
    }

    private void initCommonObjects(final TopologyLayerCallback topology, final Executor changeExecutor) {
        this.topology = topology;

        this.objectRegistry = new WeakObjectRegistry();
        this.valueMapper = new ValueMapper(objectRegistry);

        this.modelWalkingSynchronizer = new ModelWalkingSynchronizer();

        this.creator = new CommandListCreator(objectRegistry, valueMapper, topology);
        this.silentChangeExecutor = new SilentChangeExecutor(changeExecutor);
    }

    /**
     * Executes commands to change the domain model of the user.
     * 
     * <p>
     * These commands have usually been created by an other instance of {@link MetaModel} in an other JVM which send
     * them via {@link TopologyLayerCallback#sendCommands(List)} or produced them through
     * {@link MetaModel#commandsForDomainModel(CommandsForDomainModelCallback)}.
     * </p>
     * 
     * <p>
     * If you need to send these commands to other peers e.g. when you are the server in an client/server environment,
     * please call this method first an than redistribute the commands. This call blocks when one of your threads called
     * {@link MetaModel#commandsForDomainModel(CommandsForDomainModelCallback)} and hasn't finished yet. When this
     * happens, the commands you've passed as argument to this method will not be incorporated into the command list
     * returned by your {@link MetaModel#commandsForDomainModel(CommandsForDomainModelCallback)} and so you may want to
     * also redistribute them to the new client that's the reason you've called
     * {@link MetaModel#commandsForDomainModel(CommandsForDomainModelCallback)}.
     * </p>
     * 
     * @param commands
     *            The commands that should be executed.
     */
    public void execute(final List<Command> commands) {
        try {
            modelWalkingSynchronizer.doWhenModelWalkerFinished(ActionType.INCOMMING_COMMANDS, new Runnable() {
                @Override
                public void run() {
                    for (Object command : commands) {
                        execute(command);
                    }
                }
            });
        } catch (final SynchronizeFXException e) {
            topology.onError(e);
        }
    }

    /**
     * This method creates the commands necessary to reproduce the entire domain model.
     * 
     * <p>
     * The API of this method may looks a bit odd as the commands produced are returned via a callback instead of return
     * but this is necessary to ensure that no updated are lost for newly connecting peers.
     * </p>
     * 
     * <p>
     * Make sure that commands you receive via {@link TopologyLayerCallback#sendCommands(List)} are not send to the peer
     * you've requested this initial set of commands for before your callback is called. Make also sure that future
     * calls of {@link TopologyLayerCallback#sendCommands(List)} will send the changes to this new peer before your
     * callback returns.
     * </p>
     * 
     * <p>
     * It is guaranteed that your callback is only called once and that this call happens before
     * {@link MetaModel#commandsForDomainModel(CommandsForDomainModelCallback)} returns.
     * </p>
     * 
     * @param callback
     *            The callback that takes the commands.
     */
    public void commandsForDomainModel(final CommandsForDomainModelCallback callback) {
        if (this.root == null) {
            topology.onError(new SynchronizeFXException(
                    "Request to create necessary commands to reproduce the domain model "
                            + " but the root object of the domain model is not set."));
            return;
        }
        try {
            modelWalkingSynchronizer.startModelWalking();
            creator.commandsForDomainModel(this.root, callback);
            modelWalkingSynchronizer.finishedModelWalking();
        } catch (final SynchronizeFXException e) {
            topology.onError(e);
        }
    }

    /**
     * Set's a new object as the root object for the domain model.
     * 
     * This is usably called on an {@link SetRootElement} command.
     * 
     * @param root
     *            the new root object.
     */
    void setRoot(final Object root) {
        this.root = root;
        topology.domainModelChanged(root);
    }

    /**
     * The synchronizer used to synchronize model walking processes with other tasks.
     * 
     * <p>
     * This method is intended to be used by tests only.
     * </p>
     * 
     * @return The synchronizer
     */
    ModelWalkingSynchronizer getModelWalkingSynchronizer() {
        return modelWalkingSynchronizer;
    }

    /**
     * Execute a single command to change the domain model of the user.
     * 
     * @see MetaModel#execute(List)
     * @param command
     *            The command that should be executed.
     */
    private void execute(final Object command) {
        executor.execute(command);
    }

    private void registerListenersOnModel() {
        try {
            // to register all objects in the id map
            commandsForDomainModel(new CommandsForDomainModelCallback() {
                @Override
                public void commandsReady(final List<Command> commands) {
                    // the commands are not needed.
                }
            });
            // assign UUIDs for all observable objects that don't have them until now.
            listeners.registerListenersOnEverything(root);
        } catch (final SynchronizeFXException e) {
            topology.onError(e);
        }
    }
}
