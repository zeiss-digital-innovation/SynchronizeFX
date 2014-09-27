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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToSet;
import de.saxsys.synchronizefx.core.metamodel.commands.ClearReferences;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.CreateObservableObject;
import de.saxsys.synchronizefx.core.metamodel.commands.PutToMap;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromMap;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromSet;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.metamodel.commands.SetRootElement;

/**
 * Creates various types of commands that describe changes on the domain model.
 */
class CommandListCreator {

    private final WeakObjectRegistry objectRegistry;
    private final ValueMapper valueMapper;
    private final TopologyLayerCallback topology;

    /**
     * Initializes the creator.
     * 
     * @param objectRegistry
     *            used to lookup and set ids for objects.
     * @param valueMapper
     *            used to create {@link Value} messages.
     * @param topology
     *            The user callback used to report errors.
     */
    public CommandListCreator(final WeakObjectRegistry objectRegistry, final ValueMapper valueMapper,
            final TopologyLayerCallback topology) {
        this.objectRegistry = objectRegistry;
        this.valueMapper = valueMapper;
        this.topology = topology;
    }

    /**
     * @see MetaModel#commandsForDomainModel()
     * 
     * @param root
     *            The root object of the domain model.
     * @param callback
     *            The callback that takes the commands necessary to rebuild the domain model at it's current state.
     */
    public void commandsForDomainModel(final Object root, final CommandsForDomainModelCallback callback) {
        final State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                createObservableObject(root, state);
            }
        }, false);

        final SetRootElement msg = new SetRootElement();
        msg.setRootElementId(objectRegistry.getIdOrFail(root));
        // prepend it to the ClearReferences command
        state.commands.add(state.commands.size() - 1, msg);

        callback.commandsReady(state.commands);
    }

    /**
     * Creates the commands necessary to set a new value for a property.
     * 
     * @param propertyId
     *            The id of the property where the new value should be set.
     * @param value
     *            The value that should be set.
     * @throws SynchronizeFXException
     *             When creation of the commands failed.
     * @return The commands.
     */
    public List<Command> setPropertyValue(final UUID propertyId, final Object value) throws SynchronizeFXException {
        final State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                setPropertyValue(propertyId, value, state);
            }
        }, true);
        return state.commands;
    }

    /**
     * Creates the list with commands necessary for an add to list action.
     * 
     * @param listId
     *            The ID of the list where the element should be added.
     * @param position
     *            The position in the list at which the value object should be added.
     * @param value
     *            The object that should be added to the list.
     * @param newSize
     *            The new size the list has after this command has been executed on it.
     * @return a list with commands necessary to recreate this add to list command.
     */
    public List<Command> addToList(final UUID listId, final int position, final Object value, final int newSize) {
        final State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                addToList(listId, position, value, newSize, state);
            }
        }, true);
        return state.commands;
    }

    /**
     * Creates the list with commands necessary for an add to set action.
     * 
     * @param setId
     *            The ID of the set where the element should be added.
     * @param value
     *            The object that should be added to the set.
     * @return a set with commands necessary to recreate this add to set command.
     */
    public List<Command> addToSet(final UUID setId, final Object value) {
        final State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                addToSet(setId, value, state);
            }
        }, true);
        return state.commands;
    }

    /**
     * Creates the list with commands necessary to put a mapping into a map.
     * 
     * @param mapId
     *            the id of the map where the mapping should be added.
     * @param key
     *            the key of the new mapping.
     * @param value
     *            the value of the new mapping.
     * @return the list with the commands.
     */
    public List<Command> putToMap(final UUID mapId, final Object key, final Object value) {
        final State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                putToMap(mapId, key, value, state);
            }
        }, true);
        return state.commands;
    }

    /**
     * Creates the list with commands necessary to remove a object from a list.
     * 
     * @param listId
     *            The ID of the list where an element should be removed.
     * @param startPosition
     *            The index of the first element in the list which should be removed.
     * @param removeCount
     *            The element count to remove from the list, starting from <code>startPosition</code>.
     * @param newSize
     *            The size the list will have after this command has been applied.
     * @return The command list.
     */
    public List<Command> removeFromList(final UUID listId, final int startPosition, final int removeCount,
            final int newSize) {
        final RemoveFromList msg = new RemoveFromList();
        msg.setListId(listId);
        msg.setStartPosition(startPosition);
        msg.setRemoveCount(removeCount);
        msg.setNewSize(newSize);
        final List<Command> commands = new ArrayList<>(1);
        commands.add(msg);
        return commands;
    }

    /**
     * Creates the list with command necessary to remove a mapping from a map.
     * 
     * @param mapId
     *            the map where the mapping should be removed.
     * @param key
     *            the key of the mapping that should be removed.
     * @return the list with the commands.
     */
    public List<Command> removeFromMap(final UUID mapId, final Object key) {
        final State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                createObservableObject(key, state);
            }
        }, false);

        final boolean keyIsObservableObject = state.lastObjectWasObservable;

        final RemoveFromMap msg = new RemoveFromMap();
        msg.setMapId(mapId);
        msg.setKey(valueMapper.map(new ObservedValue(key, keyIsObservableObject)));

        state.commands.add(state.commands.size() - 1, msg);
        return state.commands;
    }

    /**
     * Creates the list with commands necessary to remove a object from a set.
     * 
     * @param setId
     *            The ID of the set where an element should be removed.
     * @param value
     *            The element that should be removed.
     * @return The command list.
     */
    public List<Command> removeFromSet(final UUID setId, final Object value) {
        final State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                createObservableObject(value, state);
            }
        }, false);

        final boolean keyIsObservableObject = state.lastObjectWasObservable;

        final RemoveFromSet msg = new RemoveFromSet();
        msg.setSetId(setId);
        msg.setValue(valueMapper.map(new ObservedValue(value, keyIsObservableObject)));

        state.commands.add(state.commands.size() - 1, msg);
        return state.commands;
    }

    /**
     * Creates the list of commands necessary to replace an object in a list.
     * 
     * @param listId
     *            the ID of the list where the element should be replaced
     * @param position
     *            the position of the element that should be replaced
     * @param value
     *            the new value
     * @return the command list
     */
    public List<Command> replaceInList(final UUID listId, final int position, final Object value) {
        final State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                final ReplaceInList replaceInList = new ReplaceInList();
                replaceInList.setListId(listId);
                replaceInList.setPosition(position);
                final boolean isObservableObject = createObservableObject(value, state);
                replaceInList.setValue(valueMapper.map(new ObservedValue(value, isObservableObject)));

                state.commands.add(replaceInList);
            }
        }, true);

        return state.commands;
    }

    private void setPropertyValue(final UUID propertyId, final Object value, final State state) {
        final SetPropertyValue msg = new SetPropertyValue();
        msg.setPropertyId(propertyId);

        final boolean isObservableObject = createObservableObject(value, state);
        msg.setValue(valueMapper.map(new ObservedValue(value, isObservableObject)));

        state.commands.add(msg);
    }

    private void addToList(final UUID listId, final int position, final Object value, final int newSize,
            final State state) {
        final AddToList msg = new AddToList();
        msg.setListId(listId);
        msg.setPosition(position);
        msg.setNewSize(newSize);

        final boolean isObservableObject = createObservableObject(value, state);
        msg.setValue(valueMapper.map(new ObservedValue(value, isObservableObject)));

        state.commands.add(msg);
    }

    private void addToSet(final UUID setId, final Object value, final State state) {
        final AddToSet msg = new AddToSet();
        msg.setSetId(setId);

        final boolean isObservableObject = createObservableObject(value, state);
        msg.setValue(valueMapper.map(new ObservedValue(value, isObservableObject)));

        state.commands.add(msg);
    }

    private void putToMap(final UUID mapId, final Object key, final Object value, final State state) {
        final PutToMap msg = new PutToMap();
        msg.setMapId(mapId);

        final boolean keyIsObservableObject = createObservableObject(key, state);
        final boolean valueIsObservableObject = createObservableObject(value, state);

        msg.setKey(valueMapper.map(new ObservedValue(key, keyIsObservableObject)));
        msg.setValue(valueMapper.map(new ObservedValue(value, valueIsObservableObject)));

        state.commands.add(msg);
    }

    /**
     * Adds commands to the list that are necessary to create the observable object.
     * 
     * If {@code value} isn't an observable object, then nothing is added to the commandList.
     * 
     * @param value
     *            The object for which the commands should be created.
     * @param commandList
     *            The list where the commands should be added to.
     * @param state
     *            The state of this domain model parsing.
     * @return true if value is an observable object and false otherwise.
     */
    private boolean createObservableObject(final Object value, final State state) {
        if (value == null || !PropertyVisitor.isObservableObject(value.getClass())) {
            return state.lastObjectWasObservable = false;
        }

        synchronized (state.alreadyVisited) {
            if (state.alreadyVisited.containsKey(value)) {
                return state.lastObjectWasObservable = true;
            }
            state.alreadyVisited.put(value, null);
        }

        if (state.skipKnown && objectRegistry.getId(value).isPresent()) {
            return state.lastObjectWasObservable = true;
        }

        final CreateObservableObject msg = new CreateObservableObject();
        final int currentSize = state.commands.size();

        try {
            new PropertyVisitor(value) {
                @Override
                protected boolean visitSingleValueProperty(final Property<?> fieldValue) {
                    final UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    setPropertyValue(fieldId, fieldValue.getValue(), state);
                    return false;
                }

                @Override
                protected boolean visitCollectionProperty(final ListProperty<?> fieldValue) {
                    final UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    final ListIterator<?> it = fieldValue.listIterator();
                    int index = 0;
                    while (it.hasNext()) {
                        final Object o = it.next();
                        addToList(fieldId, index, o, index + 1, state);
                        index++;
                    }
                    return false;
                }

                @Override
                protected boolean visitCollectionProperty(final MapProperty<?, ?> fieldValue) {
                    final UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    for (final Entry<?, ?> entry : fieldValue.entrySet()) {
                        putToMap(fieldId, entry.getKey(), entry.getValue(), state);
                    }
                    return false;
                }

                @Override
                protected boolean visitCollectionProperty(final SetProperty<?> fieldValue) {
                    final UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    for (final Object entry : fieldValue) {
                        addToSet(fieldId, entry, state);
                    }
                    return false;
                }

                private UUID registerPropertyAndParent(final Field field, final Property<?> fieldValue) {
                    msg.setObjectId(objectRegistry.registerIfUnknown(value));
                    final UUID fieldId = objectRegistry.registerIfUnknown(fieldValue);
                    msg.getPropertyNameToId().put(field.getName(), fieldId);
                    return fieldId;
                }
            };
        } catch (final IllegalAccessException e) {
            topology.onError(new SynchronizeFXException(e));
        } catch (final SecurityException e) {
            topology.onError(new SynchronizeFXException(
                    "Maybe you're JVM doesn't allow reflection for this application?", e));
        }

        if (msg.getObjectId() == null) {
            return state.lastObjectWasObservable = false;
        }
        msg.setClassName(value.getClass().getName());
        // create the object before it's field values are set
        state.commands.add(currentSize, msg);
        return state.lastObjectWasObservable = true;
    }

    private State createCommandList(final WithCommandType type, final boolean skipKnown) {
        final State state = new State(skipKnown);
        boolean restart = true;
        while (restart) {
            restart = false;
            state.reset();
            try {
                type.invoke(state);
            } catch (final ConcurrentModificationException e) {
                restart = true;
            }
        }
        state.commands.add(new ClearReferences());
        return state;
    }

    /**
     * The state that must be keeped for the creation of depend commands.
     */
    private static class State {
        /**
         * only {@code synchronized} access allowed.
         */
        private final Map<Object, Object> alreadyVisited = new IdentityHashMap<>();
        private final List<Command> commands = new LinkedList<>();
        private final boolean skipKnown;
        /**
         * Holds the return value of the last invocation of
         * {@link CommandListCreator#createObservableObject(Object, State)}.
         */
        private boolean lastObjectWasObservable;

        public State(final boolean skipKnown) {
            this.skipKnown = skipKnown;
        }

        /**
         * Resets all state holding fields to the state when this object was instantiated.
         */
        public void reset() {
            alreadyVisited.clear();
            commands.clear();
            lastObjectWasObservable = false;
        }
    }

    /**
     * Used to define methods that should be really be executed when calling
     * {@link CommandListCreator#createCommandList(WithCommandType, boolean)}.
     */
    private interface WithCommandType {
        void invoke(State state);
    }
}
