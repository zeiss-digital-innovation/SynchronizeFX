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

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.collections.WeakMapChangeListener;
import javafx.collections.WeakSetChangeListener;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.ModelWalkingSynchronizer.ActionType;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.executors.CommandLogDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the listeners that generate the commands neccessary for reproducing the changes that where made on
 * properties the listener is registered on.
 * 
 */
class Listeners implements ChangeListener<Object>, ListChangeListener<Object>, SetChangeListener<Object>,
        MapChangeListener<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(Listeners.class);

    private final WeakObjectRegistry objectRegistry;
    private final CommandListCreator creator;
    private final TopologyLayerCallback topology;
    private final ModelWalkingSynchronizer synchronizer;
    private final CommandLogDispatcher commandLog;

    private final WeakChangeListener<Object> propertyListener = new WeakChangeListener<>(this);
    private final WeakListChangeListener<Object> listListener = new WeakListChangeListener<>(this);
    private final WeakSetChangeListener<Object> setListener = new WeakSetChangeListener<>(this);
    private final WeakMapChangeListener<Object, Object> mapListener = new WeakMapChangeListener<>(this);

    private final Map<Object, Object> disabledFor = new IdentityHashMap<>();

    /**
     * Initializes the Listeners.
     * 
     * @param objectRegistry
     *            used for object to id lookup.
     * @param creator
     *            The creator to use for command creation.
     * @param topology
     *            The user callback to use when errors occur.
     * @param synchronizer
     *            The model walking locker to block user threads as long as a model walking process is active.
     * @param commandLog
     *            To log locally produced commands that where send to the server.
     */
    public Listeners(final WeakObjectRegistry objectRegistry, final CommandListCreator creator,
            final TopologyLayerCallback topology, final ModelWalkingSynchronizer synchronizer,
            final CommandLogDispatcher commandLog) {
        this.objectRegistry = objectRegistry;
        this.creator = creator;
        this.topology = topology;
        this.synchronizer = synchronizer;
        this.commandLog = commandLog;
    }

    /**
     * Registers listeners on all {@link Property} fields of all Objects contained in {@code model}.
     * 
     * @param object
     *            The root of the object graph where to start registering listeners.
     */
    public void registerListenersOnEverything(final Object object) {
        try {
            // the removeListener() call ensures that the listener is not added more than once
            new PropertyVisitor(object) {
                @Override
                protected boolean visitSingleValueProperty(final Property<?> fieldValue) {
                    fieldValue.removeListener(propertyListener);
                    fieldValue.addListener(propertyListener);
                    return true;
                }

                @Override
                protected boolean visitCollectionProperty(final ListProperty<?> fieldValue) {
                    fieldValue.removeListener(listListener);
                    fieldValue.addListener(listListener);
                    return true;
                }

                @Override
                protected boolean visitCollectionProperty(final MapProperty<?, ?> fieldValue) {
                    fieldValue.removeListener(mapListener);
                    fieldValue.addListener(mapListener);
                    return true;
                }

                @Override
                protected boolean visitCollectionProperty(final SetProperty<?> fieldValue) {
                    fieldValue.removeListener(setListener);
                    fieldValue.addListener(setListener);
                    return false;
                }
            };
        } catch (final IllegalAccessException e) {
            topology.onError(new SynchronizeFXException(e));
        } catch (final SecurityException e) {
            topology.onError(new SynchronizeFXException(
                    "Maybe you're JVM doesn't allow reflection for this application?", e));
        }
    }

    /**
     * Registers listeners on a property so that commands are created when changes in the property occur.
     * 
     * @param prop
     *            The property to register the change listeners on.
     */
    public void registerOn(final Property<?> prop) {
        prop.addListener(propertyListener);
    }

    /**
     * Registers listeners on a property so that commands are created when changes in the property occur.
     * 
     * @param list
     *            The property to register the change listeners on.
     */
    public void registerOn(final ListProperty<?> list) {
        list.addListener(listListener);
    }

    /**
     * Registers listeners on a property so that commands are created when changes in the property occur.
     * 
     * @param map
     *            The property to register the change listeners on.
     */
    public void registerOn(final MapProperty<?, ?> map) {
        map.addListener(mapListener);
    }

    @Override
    public void changed(final ObservableValue<? extends Object> property, final Object oldValue, final Object newValue) {
        if (disabledFor.containsKey(property)) {
            return;
        }
        try {
            final List<Command> commands = creator.setPropertyValue(objectRegistry.getIdOrFail(property), newValue);
            if (newValue != null) {
                registerListenersOnEverything(newValue);
            }
            distributeCommands(commands);
        } catch (final SynchronizeFXException e) {
            topology.onError(e);
        }
    }

    @Override
    public void onChanged(final ListChangeListener.Change<? extends Object> event) {
        final ObservableList<? extends Object> list = event.getList();
        if (disabledFor.containsKey(list)) {
            return;
        }
        event.reset();
        try {
            final UUID listId = objectRegistry.getIdOrFail(list);
            while (event.next()) {
                List<Command> commands = null;
                if (event.wasPermutated()) {
                    LOG.warn("Got an ListChangeListener.Change event that permutates the list."
                            + " This case is not implemented and is not synchronized.");
                    // for (int i = event.getFrom(); i < event.getTo(); ++i) {
                    // //TODO
                    // }
                } else if (event.wasUpdated()) {
                    LOG.warn("Got an ListChangeListener.Change event that indicates that some elements in a list"
                            + " have been updated. This case is not implemented and is not synchronized.");
                    // TODO
                } else if (event.wasAdded()) {
                    commands = new LinkedList<>();
                    for (int i = event.getFrom(); i < event.getTo(); i++) {
                        final Object elem = list.get(i);
                        final Optional<UUID> id = objectRegistry.getId(elem);
                        if (event.wasRemoved()) {
                            // this is a replaced event (see ListChangeListener.Change documentation)
                            commands.addAll(creator.replaceInList(listId, i, elem));
                        } else {
                            commands.addAll(creator.addToList(listId, i, elem, list.size()));
                        }
                        // if getId() was null, then newValue is unknown to the meta model and therefore
                        // listeners need to be registered on it.
                        if (elem != null && !id.isPresent()) {
                            registerListenersOnEverything(elem);
                        }
                    }
                } else if (event.wasRemoved()) {
                    commands = creator.removeFromList(listId, event.getTo(), event.getRemovedSize());
                    // TODO don't let distributeCommands() happen
                }
                if (commands != null) {
                    distributeCommands(commands);
                }
            }
        } catch (final SynchronizeFXException e) {
            topology.onError(e);
        } finally {
            event.reset();
        }
    }

    @Override
    public void onChanged(final javafx.collections.SetChangeListener.Change<? extends Object> change) {
        final ObservableSet<?> set = change.getSet();
        if (disabledFor.containsKey(set)) {
            return;
        }
        try {
            final UUID setId = objectRegistry.getIdOrFail(set);

            List<Command> commands = null;
            if (change.wasAdded()) {
                final Object value = change.getElementAdded();
                commands = creator.addToSet(setId, value);
                registerListenersOnEverything(value);
            } else {
                final Object value = change.getElementRemoved();
                commands = creator.removeFromSet(setId, value);
            }
            distributeCommands(commands);
        } catch (final SynchronizeFXException e) {
            topology.onError(e);
        }
    }

    @Override
    public void onChanged(final MapChangeListener.Change<? extends Object, ? extends Object> change) {
        final ObservableMap<?, ?> map = change.getMap();
        if (disabledFor.containsKey(map)) {
            return;
        }
        try {
            final UUID mapId = objectRegistry.getIdOrFail(map);
            final Object key = change.getKey();
            if (change.wasAdded()) {
                final Object value = change.getValueAdded();
                final List<Command> commands = creator.putToMap(mapId, key, value);
                registerListenersOnEverything(key);
                if (value != null) {
                    registerListenersOnEverything(value);
                }
                distributeCommands(commands);
            } else {
                distributeCommands(creator.removeFromMap(mapId, key));
            }
        } catch (final SynchronizeFXException e) {
            topology.onError(e);
        }
    }

    /**
     * Prevents the listeners of this object to be executed for a specific object.
     * 
     * This can be useful if you want to apply changes from other peers to the domain model. If the listeners wouldn't
     * be disabled in this case, they would generate change commands which than would be send amongst others to the
     * client that generated the changes in the first place. The result would be an endless loop.
     * 
     * @param value
     *            The object for which the listeners should be disabled.
     */
    public void disableFor(final Object value) {
        disabledFor.put(value, null);
    }

    /**
     * Enables a previously disabled listener.
     * 
     * @see Listeners#disabledFor
     * @param value
     *            The object for which the listeners should be enabled.
     */
    public void enableFor(final Object value) {
        disabledFor.remove(value);
    }

    private void distributeCommands(final List<Command> commands) {
        synchronizer.doWhenModelWalkerFinished(ActionType.LOCAL_PROPERTY_CHANGES, new Runnable() {
            @Override
            public void run() {
                commandLog.logLocalCommands(commands);
                topology.sendCommands(commands);
            }
        });
    }
}
