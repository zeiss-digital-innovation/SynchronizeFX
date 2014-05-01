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
import java.util.Map;
import java.util.UUID;

import javafx.application.Platform;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.ModelWalkingSynchronizer.ActionType;
import de.saxsys.synchronizefx.core.metamodel.SilentChangeExecutor.ModelChangeExecutor;
import de.saxsys.synchronizefx.core.metamodel.commands.SetRootElement;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceIdentityMap;
import org.apache.commons.collections.map.ReferenceMap;

/**
 * Generates and applies commands necessary to keep domain models synchronous.
 */
public class MetaModel {

    // Apache commons collections are not generic
    @SuppressWarnings("unchecked")
    private Map<Object, UUID> objectToId = new ReferenceIdentityMap(AbstractReferenceMap.WEAK,
            AbstractReferenceMap.HARD);
    @SuppressWarnings("unchecked")
    private Map<UUID, Object> idToObject = new ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.WEAK);

    private boolean doChangesInJavaFxThread;
    private Object root;

    private final CommandListCreator creator;
    private final CommandListExecutor executor;
    private final Listeners listeners;
    private final ModelWalkingSynchronizer modelWalkingSynchronizer;

    private final TopologyLayerCallback topology;

    /**
     * Creates a {@link MetaModel} where the root object of the domain model is received from another node.
     * 
     * @param topology
     *            used to interact with the lower layer which is probably represented by the class that called this
     *            constructor.
     */
    public MetaModel(final TopologyLayerCallback topology) {
        this.doChangesInJavaFxThread = false;
        this.topology = topology;
        
        final ValueMapper valueMapper = new ValueMapper(this);

        this.modelWalkingSynchronizer = new ModelWalkingSynchronizer();
        this.creator = new CommandListCreator(this, valueMapper, topology);
        this.listeners = new Listeners(this, creator, topology, modelWalkingSynchronizer);

        final SilentChangeExecutor changeExecutor = new SilentChangeExecutor(listeners,
                new MaybeExecuteInJavaFXThread());
        this.executor = new CommandListExecutor(this, listeners, changeExecutor, valueMapper, topology);
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
        this(topology);
        this.root = root;
        // to register all objects in the id map
        commandsForDomainModel(new CommandsForDomainModelCallback() {
            @Override
            public void commandsReady(final List<Object> commands) {
                // the commands are not needed.
            }
        });
        // assign UUIDs for all observable objects that don't have them until now.
        listeners.registerListenersOnEverything(root);
    }

    /**
     * 
     * @return {@code true} when all changes to the user domain model have to be done in the JavaFX thread and false
     *         otherwise.
     */
    public boolean isDoChangesInJavaFxThread() {
        return doChangesInJavaFxThread;
    }

    /**
     * @see MetaModel#isDoChangesInJavaFxThread()
     * @param doChangesInJavaFxThread
     *            the new value
     */
    public void setDoChangesInJavaFxThread(final boolean doChangesInJavaFxThread) {
        this.doChangesInJavaFxThread = doChangesInJavaFxThread;
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
    public void execute(final List<Object> commands) {
        modelWalkingSynchronizer.doWhenModelWalkerFinished(ActionType.INCOMMING_MESSAGES, new Runnable() {
            @Override
            public void run() {
                for (Object message : commands) {
                    execute(message);
                }
            }
        });
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
        modelWalkingSynchronizer.startModelWalking();
        try {
            creator.commandsForDomainModel(this.root, callback);
        } catch (final SynchronizeFXException e) {
            topology.onError(e);
        }
        modelWalkingSynchronizer.finishedModelWalking();
    }

    /**
     * Returns a object that is identified by an id.
     * 
     * @param id
     *            The id
     * @return The object if one is registered by this id and <code>null</code> if not.
     */
    public Object getById(final UUID id) {
        return idToObject.get(id);
    }

    /**
     * Returns the id for an object.
     * 
     * @param object
     *            the object
     * @return The id.
     */
    public UUID getId(final Object object) {
        return objectToId.get(object);
    }

    /**
     * Set's a new object as the root object for the domain model.
     * 
     * This is usably called on an {@link SetRootElement} message.
     * 
     * @param root
     *            the new root object.
     */
    void setRoot(final Object root) {
        this.root = root;
        topology.domainModelChanged(root);
    }

    /**
     * Registers an object in the meta model if it is not already registered.
     * 
     * @param object
     *            The object to register.
     * @return The id of the object. It doesn't matter if the object was just registered or already known.
     */
    UUID registerIfUnknown(final Object object) {
        UUID id = getId(object);
        if (id == null) {
            id = registerObject(object);
        }
        return id;
    }

    /**
     * Registers an object in this model identified by a pre existing id.
     * 
     * @param object
     *            The object to register.
     * @param id
     *            The id by which this object is identified.
     */
    void registerObject(final Object object, final UUID id) {
        objectToId.put(object, id);
        idToObject.put(id, object);
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
        try {
            executor.execute(command);
        } catch (final SynchronizeFXException e) {
            topology.onError(e);
        }
    }

    /**
     * Registers an object in this model identified by a newly created id.
     * 
     * @param object
     *            The object to register.
     * @return The generated id.
     */
    private UUID registerObject(final Object object) {
        UUID id = generateId();
        registerObject(object, id);
        return id;
    }

    private UUID generateId() {
        return UUID.randomUUID();
    }

    /**
     * Executes changes to the users domain model in the JavaFX Thread if {@link MetaModel#doChangesInJavaFxThread} is
     * <code>true</code>. Executes them directly otherwise.
     */
    private class MaybeExecuteInJavaFXThread implements ModelChangeExecutor {
        @Override
        public void execute(final Runnable change) {
            if (doChangesInJavaFxThread) {
                Platform.runLater(change);
            } else {
                change.run();
            }
        }
    }
}
