/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToSet;
import de.saxsys.synchronizefx.core.metamodel.commands.ClearReferences;
import de.saxsys.synchronizefx.core.metamodel.commands.CreateObservableObject;
import de.saxsys.synchronizefx.core.metamodel.commands.PutToMap;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromMap;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromSet;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.metamodel.commands.SetRootElement;

/**
 * Creates various types of commands that describe changes on the domain model.
 */
class CommandListCreator {
    /**
     * Currently running command list creation states.
     * 
     * Only {@code synchronized} access is allowed.
     */
    private static final Set<State> STATES = new HashSet<>();

    private MetaModel parent;

    private TopologyLayerCallback topology;

    /**
     * Initializes the creator.
     * 
     * @param parent The model used to lookup and set ids for objects.
     * @param topology The user callback used to report errors.
     */
    public CommandListCreator(final MetaModel parent, final TopologyLayerCallback topology) {
        this.parent = parent;
        this.topology = topology;
    }

    /**
     * @see MetaModel#commandsForDomainModel()
     * 
     * @param root The root object of the domain model.
     * @param callback The callback that takes the commands necessary to rebuild the domain model at it's current state.
     */
    public void commandsForDomainModel(final Object root, final CommandsForDomainModelCallback callback) {
        synchronized (parent.getModelWalkingInProgressLock()) {
            parent.setModelWalkingInProgress(true);
        }
        State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                createObservableObject(root, state);
            }
        }, false);

        SetRootElement msg = new SetRootElement();
        msg.setRootElementId(parent.getId(root));
        // prepend it to ClearReferences message
        state.commands.add(state.commands.size() - 1, msg);

        callback.commandsReady(state.commands);
        synchronized (parent.getModelWalkingInProgressLock()) {
            parent.setModelWalkingInProgress(false);
            parent.getModelWalkingInProgressLock().notifyAll();
        }
    }

    /**
     * Checks if any of the currently running command list creation process has already visited an observable value that
     * has changed and therefore needs to restart.
     * 
     * <p>
     * This method is thread safe. You don't need to call it from the thread that runs the property visitor.
     * </p>
     * 
     * @param changedProperty The property that has changed.
     */
    public void checkForConcurentModification(final Observable changedProperty) {
        synchronized (STATES) {
            for (State state : STATES) {
                synchronized (state.alreadyVisited) {
                    if (state.alreadyVisited.containsKey(changedProperty)) {
                        state.concurentModification = true;
                    }
                }
            }
        }
    }

    /**
     * Creates the messages necessary to set a new value for a property.
     * 
     * @param propertyId The id of the property where the new value should be set.
     * @param value The value that should be set.
     * @return The commands.
     */
    public List<Object> setPropertyValue(final UUID propertyId, final Object value) {
        State state = createCommandList(new WithCommandType() {
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
     * @param listId The ID of the list where the element should be added.
     * @param position The position in the list at which the value object should be added.
     * @param value The object that should be added to the list.
     * @return a list with commands necessary to recreate this add to list command.
     */
    public List<Object> addToList(final UUID listId, final int position, final Object value) {
        State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                addToList(listId, position, value, state);
            }
        }, true);
        return state.commands;
    }

    /**
     * Creates the list with commands necessary for an add to set action.
     * 
     * @param setId The ID of the set where the element should be added.
     * @param value The object that should be added to the set.
     * @return a set with commands necessary to recreate this add to set command.
     */
    public List<Object> addToSet(final UUID setId, final Object value) {
        State state = createCommandList(new WithCommandType() {
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
     * @param mapId the id of the map where the mapping should be added.
     * @param key the key of the new mapping.
     * @param value the value of the new mapping.
     * @return the list with the commands.
     */
    public List<Object> putToMap(final UUID mapId, final Object key, final Object value) {
        State state = createCommandList(new WithCommandType() {
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
     * @param listId The ID of the list where an element should be removed.
     * @param position The position of the element in the list which should be removed.
     * @return The command list.
     */
    public List<Object> removeFromList(final UUID listId, final int position) {
        RemoveFromList msg = new RemoveFromList();
        msg.setListId(listId);
        msg.setPosition(position);
        List<Object> commands = new ArrayList<>(1);
        commands.add(msg);
        return commands;
    }

    /**
     * Creates the list with command necessary to remove a mapping from a map.
     * 
     * @param mapId the map where the mapping should be removed.
     * @param key the key of the mapping that should be removed.
     * @return the list with the commands.
     */
    public List<Object> removeFromMap(final UUID mapId, final Object key) {
        State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                createObservableObject(key, state);
            }
        }, false);

        boolean keyIsObservableObject = state.lastObjectWasObservable;

        RemoveFromMap msg = new RemoveFromMap();
        msg.setMapId(mapId);

        if (keyIsObservableObject) {
            msg.setKeyObservableObjectId(parent.getId(key));
        } else {
            msg.setKeySimpleObjectValue(key);
        }
        state.commands.add(state.commands.size() - 1, msg);
        return state.commands;
    }

    /**
     * Creates the list with commands necessary to remove a object from a set.
     * 
     * @param setId The ID of the set where an element should be removed.
     * @param value The element that should be removed.
     * @return The command list.
     */
    public List<Object> removeFromSet(final UUID setId, final Object value) {
        State state = createCommandList(new WithCommandType() {
            @Override
            public void invoke(final State state) {
                createObservableObject(value, state);
            }
        }, false);

        boolean keyIsObservableObject = state.lastObjectWasObservable;

        RemoveFromSet msg = new RemoveFromSet();
        msg.setSetId(setId);

        if (keyIsObservableObject) {
            msg.setObservableObjectId(parent.getId(value));
        } else {
            msg.setSimpleObjectValue(value);
        }

        state.commands.add(state.commands.size() - 1, msg);
        return state.commands;
    }

    private void setPropertyValue(final UUID propertyId, final Object value, final State state) {
        SetPropertyValue msg = new SetPropertyValue();
        msg.setPropertyId(propertyId);

        boolean isObservableObject = createObservableObject(value, state);
        if (isObservableObject) {
            msg.setObservableObjectId(parent.getId(value));
        } else {
            msg.setSimpleObjectValue(value);
        }

        state.commands.add(msg);
    }

    private void addToList(final UUID listId, final int position, final Object value, final State state) {
        AddToList msg = new AddToList();
        msg.setListId(listId);
        msg.setPosition(position);

        boolean isObservableObject = createObservableObject(value, state);
        if (isObservableObject) {
            msg.setObservableObjectId(parent.getId(value));
        } else {
            msg.setSimpleObjectValue(value);
        }

        state.commands.add(msg);
    }

    private void addToSet(final UUID setId, final Object value, final State state) {
        AddToSet msg = new AddToSet();
        msg.setSetId(setId);

        boolean isObservableObject = createObservableObject(value, state);
        if (isObservableObject) {
            msg.setObservableObjectId(parent.getId(value));
        } else {
            msg.setSimpleObjectValue(value);
        }

        state.commands.add(msg);
    }

    private void putToMap(final UUID mapId, final Object key, final Object value, final State state) {
        PutToMap msg = new PutToMap();
        msg.setMapId(mapId);

        boolean keyIsObservableObject = createObservableObject(key, state);
        boolean valueIsObservableObject = createObservableObject(value, state);

        if (keyIsObservableObject) {
            msg.setKeyObservableObjectId(parent.getId(key));
        } else {
            msg.setKeySimpleObjectValue(key);
        }

        if (valueIsObservableObject) {
            msg.setValueObservableObjectId(parent.getId(value));
        } else {
            msg.setValueSimpleObjectValue(value);
        }
        state.commands.add(msg);
    }

    /**
     * Adds commands to the list that are necessary to create the observable object.
     * 
     * If {@code value} isn't an observable object, then nothing is added to the commandList.
     * 
     * @param value The object for which the commands should be created.
     * @param commandList The list where the commands should be added to.
     * @param state The state of this domain model parsing.
     * @return true if value is an observable object and false otherwise.
     */
    private boolean createObservableObject(final Object value, final State state) {
        if (value == null || !PropertyVisitor.isObservableObject(value.getClass())) {
            return state.lastObjectWasObservable = false;
        }

        synchronized (state.alreadyVisited) {
            if (state.concurentModification) {
                throw new ConcurrentModificationException();
            }
            if (state.alreadyVisited.containsKey(value)) {
                return state.lastObjectWasObservable = true;
            }
            state.alreadyVisited.put(value, null);
        }

        if (state.skipKnown && parent.getId(value) != null) {
            return state.lastObjectWasObservable = true;
        }

        final CreateObservableObject msg = new CreateObservableObject();
        int currentSize = state.commands.size();

        try {
            new PropertyVisitor(value) {
                @Override
                protected boolean visitSingleValueProperty(final Property<?> fieldValue) {
                    state.alreadyVisited.put(fieldValue, null);
                    UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    setPropertyValue(fieldId, fieldValue.getValue(), state);
                    return false;
                }

                @Override
                protected boolean visitCollectionProperty(final ListProperty<?> fieldValue) {
                    state.alreadyVisited.put(fieldValue.get(), null);
                    UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    for (Object o : fieldValue) {
                        addToList(fieldId, fieldValue.indexOf(o), o, state);
                    }
                    return false;
                }

                @Override
                protected boolean visitCollectionProperty(final MapProperty<?, ?> fieldValue) {
                    state.alreadyVisited.put(fieldValue.get(), null);
                    UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    for (Entry<?, ?> entry : fieldValue.entrySet()) {
                        putToMap(fieldId, entry.getKey(), entry.getValue(), state);
                    }
                    return false;
                }

                @Override
                protected boolean visitCollectionProperty(final SetProperty<?> fieldValue) {
                    state.alreadyVisited.put(fieldValue.get(), null);
                    UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    for (Object entry : fieldValue) {
                        addToSet(fieldId, entry, state);
                    }
                    return false;
                }

                private UUID registerPropertyAndParent(final Field field, final Property<?> fieldValue) {
                    msg.setObjectId(parent.registerIfUnknown(value));
                    UUID fieldId = parent.registerIfUnknown(fieldValue);
                    msg.getPropertyNameToId().put(field.getName(), fieldId);
                    return fieldId;
                }
            };
        } catch (IllegalArgumentException | IllegalAccessException e) {
            topology.onError(new SynchronizeFXException(e));
        } catch (SecurityException e) {
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
        State state = new State(skipKnown);

        synchronized (STATES) {
            STATES.add(state);
        }

        state.concurentModification = true;
        while (state.concurentModification) {
            state.reset();
            try {
                type.invoke(state);
            } catch (ConcurrentModificationException e) {
                state.concurentModification = true;
            }
        }

        synchronized (STATES) {
            STATES.remove(state);
        }
        state.commands.add(new ClearReferences());
        return state;
    }

    /**
     * The state that must be keeped for the creation of depend messages.
     */
    private static class State {
        /**
         * only {@code synchronized} access allowed.
         */
        private final Map<Object, Object> alreadyVisited = new IdentityHashMap<>();
        /**
         * Accesses this only in {@code synchronized} block on {@code alreadyVisited}.
         */
        private boolean concurentModification;
        private final List<Object> commands = new LinkedList<>();
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
            concurentModification = false;
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
