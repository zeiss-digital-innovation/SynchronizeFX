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

package de.saxsys.synchronizefx.core.metamodel;

import java.util.List;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.ModelWalkingSynchronizer.ActionType;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.SetRootElement;
import de.saxsys.synchronizefx.core.metamodel.executors.CommandLogDispatcher;
import de.saxsys.synchronizefx.core.metamodel.executors.RepairingSingleValuePropertyCommandExecutor;
import de.saxsys.synchronizefx.core.metamodel.executors.SimpleSingleValuePropertyCommandExecutor;
import de.saxsys.synchronizefx.core.metamodel.executors.SingleValuePropertyCommandExecutor;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.AddToListRepairer;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.ListCommandIndexRepairer;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.ListCommandVersionRepairer;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.ListPropertyCommandExecutor;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.ListPropertyCommandFilter;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.RemoveFromListRepairer;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.ReparingListPropertyCommandExecutor;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.ReplaceInListRepairer;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.SimpleListPropertyCommandExecutor;

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
    private ListPropertyMetaDataStore listMetaData;
    private SimpleListPropertyCommandExecutor simpleListCommandExecutor;

    // server / client dependent initialization
    private final CommandListExecutor executor;
    private final Listeners listeners;

    /**
     * Creates a {@link MetaModel} where the root object of the domain model is received from another node.
     * 
     * @param topology
     *            used to interact with the lower layer which is probably represented by the class that called this
     *            constructor.
     */
    public MetaModel(final TopologyLayerCallback topology) {
        initCommonObjects(topology);

        // CHECKSTYLE:OFF Because of line length limit. TODO find shorter class names.
        final RepairingSingleValuePropertyCommandExecutor singleValuePropertyExecutor = new RepairingSingleValuePropertyCommandExecutor(
                objectRegistry, new SimpleSingleValuePropertyCommandExecutor(objectRegistry, silentChangeExecutor,
                        valueMapper));
        // CHECKSTYLE:ON
        final ReparingListPropertyCommandExecutor repairingListExecutor = new ReparingListPropertyCommandExecutor(
                listMetaData, new ListCommandIndexRepairer(new AddToListRepairer(), new RemoveFromListRepairer(),
                        new ReplaceInListRepairer()), new ListCommandVersionRepairer(), simpleListCommandExecutor,
                topology);
        final CommandLogDispatcher commandLog = new CommandLogDispatcher(singleValuePropertyExecutor,
                repairingListExecutor);

        this.listeners = new Listeners(objectRegistry, creator, topology, modelWalkingSynchronizer, commandLog);
        silentChangeExecutor.registerListenersToSilence(listeners);
        final ListPropertyCommandExecutor filteringListExecutor = new ListPropertyCommandFilter(repairingListExecutor,
                new TemporaryReferenceKeeper(), listMetaData, objectRegistry);
        this.executor = new CommandListExecutor(this, objectRegistry, listeners, silentChangeExecutor, valueMapper,
                listMetaData, singleValuePropertyExecutor, filteringListExecutor);
    }

    /**
     * Creates a {@link MetaModel} which serves a new domain model.
     * 
     * @see MetaModel#MetaModel(TopologyLayerCallback)
     * @param topology
     *            see {@link MetaModel#MetaModel(TopologyLayerCallback)}
     * @param root
     *            The root object of the domain model that should be served.
     */
    public MetaModel(final TopologyLayerCallback topology, final Object root) {
        initCommonObjects(topology);
        this.root = root;

        // CHECKSTYLE:OFF Because of line length limit. TODO find shorter class names.
        final SingleValuePropertyCommandExecutor singleValuePropertyExecutor = new SimpleSingleValuePropertyCommandExecutor(
                objectRegistry, silentChangeExecutor, valueMapper);
        // CHECKSTYLE:ON
        final CommandLogDispatcher commandLog = new CommandLogDispatcher();

        this.listeners = new Listeners(objectRegistry, creator, topology, modelWalkingSynchronizer, commandLog);
        silentChangeExecutor.registerListenersToSilence(listeners);
        final ListPropertyCommandExecutor filteringListExecutor = new ListPropertyCommandFilter(
                simpleListCommandExecutor, new TemporaryReferenceKeeper(), listMetaData, objectRegistry);
        this.executor = new CommandListExecutor(this, objectRegistry, listeners, silentChangeExecutor, valueMapper,
                listMetaData, singleValuePropertyExecutor, filteringListExecutor);

        registerListenersOnModel();
    }

    private void initCommonObjects(final TopologyLayerCallback topology) {
        this.topology = topology;

        this.objectRegistry = new WeakObjectRegistry();
        this.valueMapper = new ValueMapper(objectRegistry);

        this.modelWalkingSynchronizer = new ModelWalkingSynchronizer();

        this.listMetaData = new ListPropertyMetaDataStore(objectRegistry);

        this.creator = new CommandListCreator(objectRegistry, valueMapper, topology, listMetaData);
        this.silentChangeExecutor = new SilentChangeExecutor();
        this.simpleListCommandExecutor = new SimpleListPropertyCommandExecutor(objectRegistry, silentChangeExecutor,
                valueMapper, listMetaData);
    }

    /**
     * Executes commands to change the domain model of the user.
     * 
     * <p>
     * This method is <em>not</em> Thread-safe. All callers must make sure that this method is called sequentially e.g
     * by using a single-thread executor. The thread in which this method is called will also be used to execute changes
     * on JavaFX properties so clients that have bound the GUI to properties of the domain model should make sure that
     * this method is called in the JavaFX GUI thread.
     * </p>
     * 
     * <p>
     * These commands have usually been created by an other instance of {@link MetaModel} in an other JVM which send
     * them via {@link TopologyLayerCallback#sendCommands(List)} or produced them through
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
