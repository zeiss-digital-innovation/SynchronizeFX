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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.core.SynchronizeFXException;
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
 * Applies commands on a {@link MetaModel} to synchronize it's domain objects with other instances.
 * 
 */
public class CommandListExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(CommandListExecutor.class);

    private MetaModel parent;
    private Listeners listeners;
    /**
     * A set that holds hard references to objects that would otherwise only have weak references and thus could get
     * garbage collected before they are used.
     */
    private Map<Object, Object> hardReferences = new IdentityHashMap<>();
    // only needed for tracing
    private Map<Property<?>, Field> propFieldMap;

    private TopologyLayerCallback topology;

    /**
     * Initializes the executor.
     * 
     * @param parent The model to user for id lookup and registration.
     * @param listeners The listeners that should be registered on new properties.
     * @param topology The user callback that should be used to report errors.
     */
    public CommandListExecutor(final MetaModel parent, final Listeners listeners, final TopologyLayerCallback topology) {
        this.topology = topology;
        this.parent = parent;
        this.listeners = listeners;
        if (LOG.isTraceEnabled()) {
            propFieldMap = new HashMap<>();
        }
    }

    /**
     * @see MetaModel#execute(Object)
     * @param command The command to execute.
     */
    public void execute(final Object command) {
        if (command instanceof CreateObservableObject) {
            execute((CreateObservableObject) command);
        } else if (command instanceof SetPropertyValue) {
            execute((SetPropertyValue) command);
        } else if (command instanceof AddToList) {
            execute((AddToList) command);
        } else if (command instanceof RemoveFromList) {
            execute((RemoveFromList) command);
        } else if (command instanceof PutToMap) {
            execute((PutToMap) command);
        } else if (command instanceof RemoveFromMap) {
            execute((RemoveFromMap) command);
        } else if (command instanceof AddToSet) {
            execute((AddToSet) command);
        } else if (command instanceof RemoveFromSet) {
            execute((RemoveFromSet) command);
        } else if (command instanceof ClearReferences) {
            hardReferences.clear();
        } else if (command instanceof SetRootElement) {
            execute((SetRootElement) command);
        } else {
            LOG.warn("Unknown message recived. Ignoring it");
        }
    }

    private void execute(final CreateObservableObject command) {
        final Object obj;
        final Class<?> objClass;
        try {
            objClass = Class.forName(command.getClassName());
            obj = objClass.newInstance();
            listeners.registerListenersOnEverything(obj);
            for (Entry<String, UUID> entry : command.getPropertyNameToId().entrySet()) {
                Class<?> current = objClass;
                boolean fieldFound = false;
                while (current != Object.class) {
                    try {
                        Field field = current.getDeclaredField(entry.getKey());
                        field.setAccessible(true);
                        parent.registerObject(field.get(obj), entry.getValue());
                        fieldFound = true;
                        break;
                    } catch (NoSuchFieldException e) {
                        // If it's not in this class, it's maybe in the super class. So this is no exceptional state.
                    }
                    current = current.getSuperclass();
                }
                if (!fieldFound) {
                    topology.onError(new SynchronizeFXException(
                            "A message with a field name was recived which doesn't exist in the related class."
                                    + " Maybe you have different versions of the domain objects"
                                    + " in your clients and the server?"));
                }
            }
        } catch (InstantiationException e) {
            topology.onError(new SynchronizeFXException(
                    "Maybe you've forgot to add a public no-arg constructor to one of your domain objects?", e));
            return;
        } catch (IllegalAccessException e) {
            topology.onError(new SynchronizeFXException(
                    "Maybe one of your no-arg constructor of one of your domain objects is not public?", e));
            return;
        } catch (ClassNotFoundException e) {
            topology.onError(new SynchronizeFXException(
                    "Maybe not all of you're domain objects or their dependencies are availabe on every node?", e));
            return;
        } catch (SecurityException e) {
            topology.onError(new SynchronizeFXException(
                    "Maybe you're JVM doesn't allow reflection for this application?", e));
            return;
        }

        hardReferences.put(obj, null);
        parent.registerObject(obj, command.getObjectId());
    }

    private void execute(final SetPropertyValue command) {
        @SuppressWarnings("unchecked")
        final Property<Object> prop = (Property<Object>) parent.getById(command.getPropertyId());
        if (prop == null) {
            topology.onError(new SynchronizeFXException("SetPropertyValue with unknown property id recived. "
                    + command.getPropertyId()));
            return;
        }
        if (LOG.isTraceEnabled()) {
            Field field = propFieldMap.get(prop);
            if (field != null) {
                LOG.trace("Set on field " + field + " value " + command);
            } else {
                LOG.trace(command.toString());
            }
        }
        final Object value;
        final UUID valueId = command.getObservableObjectId();
        if (valueId != null) {
            value = parent.getById(valueId);
            if (value == null) {
                topology.onError(new SynchronizeFXException(
                        "SetPropertyValue command with unknown value object id recived. "
                                + command.getObservableObjectId()));
                return;
            }
        } else {
            value = command.getSimpleObjectValue();
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                listeners.disableFor(prop);
                prop.setValue(value);
                listeners.enableFor(prop);
            }
        };
        if (parent.isDoChangesInJavaFxThread()) {
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }
    }

    private void execute(final AddToList command) {
        @SuppressWarnings("unchecked")
        final List<Object> list = (List<Object>) parent.getById(command.getListId());
        if (list == null) {
            topology.onError(new SynchronizeFXException("AddToList command with unknown list id recived. "
                    + command.getListId()));
            return;
        }
        if (LOG.isTraceEnabled()) {
            Field field = propFieldMap.get(list);
            if (field != null) {
                LOG.trace("Add to list " + field + " value " + command);
            } else {
                LOG.trace(command.toString());
            }
        }
        final Object value;
        final UUID valueId = command.getObservableObjectId();
        if (valueId != null) {
            value = parent.getById(valueId);
            if (value == null) {
                topology.onError(new SynchronizeFXException(
                        "AddToList command unknown with value object id recived. " + command.getObservableObjectId()));
                return;
            }
        } else {
            value = command.getSimpleObjectValue();
        }

        // TODO catch index out of bounds exception.
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                listeners.disableFor(list);
                list.add(command.getPosition(), value);
                listeners.enableFor(list);
            }
        };
        if (parent.isDoChangesInJavaFxThread()) {
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }
    }

    private void execute(final RemoveFromList command) {
        @SuppressWarnings("unchecked")
        final List<Object> list = (List<Object>) parent.getById(command.getListId());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                listeners.disableFor(list);
                list.remove(command.getPosition());
                listeners.enableFor(list);
            }
        };
        if (parent.isDoChangesInJavaFxThread()) {
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }
    }

    private void execute(final PutToMap command) {
        @SuppressWarnings("unchecked")
        final Map<Object, Object> map = (Map<Object, Object>) parent.getById(command.getMapId());
        if (map == null) {
            topology.onError(new SynchronizeFXException("PutToMap command with unknown map id recived. "
                    + command.getMapId()));
            return;
        }
        if (LOG.isTraceEnabled()) {
            Field field = propFieldMap.get(map);
            if (field != null) {
                LOG.trace("Put in map " + field + " value " + command);
            } else {
                LOG.trace(command.toString());
            }
        }

        final Object key;
        final UUID keyId = command.getKeyObservableObjectId();
        if (keyId != null) {
            key = parent.getById(keyId);
            if (key == null) {
                topology.onError(new SynchronizeFXException(
                        "PutToMap command with unknown key object id recived. " + command.getKeyObservableObjectId()));
                return;
            }
        } else {
            key = command.getKeySimpleObjectValue();
        }

        final Object value;
        final UUID valueId = command.getValueObservableObjectId();
        if (valueId != null) {
            value = parent.getById(valueId);
            if (value == null) {
                topology.onError(new SynchronizeFXException(
                        "PutToMap command with unknown value object id recived. "
                                + command.getValueObservableObjectId()));
                return;
            }
        } else {
            value = command.getValueSimpleObjectValue();
        }

        // TODO catch index out of bounds exception.
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                listeners.disableFor(map);
                map.put(key, value);
                listeners.enableFor(map);
            }
        };
        if (parent.isDoChangesInJavaFxThread()) {
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }
    }

    private void execute(final RemoveFromMap command) {
        @SuppressWarnings("unchecked")
        final Map<Object, Object> map = (Map<Object, Object>) parent.getById(command.getMapId());
        final Object key =
                command.getKeySimpleObjectValue() != null ? command.getKeySimpleObjectValue() : parent.getById(command
                        .getKeyObservableObjectId());
        if (key == null) {
            topology.onError(new SynchronizeFXException(
                    "RemoveFromMap command with unknown key object id recived. " + command.getKeySimpleObjectValue()));
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                listeners.disableFor(map);
                map.remove(key);
                listeners.enableFor(map);
            }
        };
        if (parent.isDoChangesInJavaFxThread()) {
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }
    }


    private void execute(final AddToSet command) {
        @SuppressWarnings("unchecked")
        final Set<Object> set = (Set<Object>) parent.getById(command.getListId());
        if (set == null) {
            topology.onError(new SynchronizeFXException("AddToSet command with unknown list id recived. "
                    + command.getListId()));
            return;
        }
        final Object value;
        final UUID valueId = command.getObservableObjectId();
        if (valueId != null) {
            value = parent.getById(valueId);
            if (value == null) {
                topology.onError(new SynchronizeFXException(
                        "AddToSet command unknown with value object id recived. " + command.getObservableObjectId()));
                return;
            }
        } else {
            value = command.getSimpleObjectValue();
        }

        // TODO catch index out of bounds exception.
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                listeners.disableFor(set);
                set.add(value);
                listeners.enableFor(set);
            }
        };
        if (parent.isDoChangesInJavaFxThread()) {
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }
    }
    
    private void execute(final RemoveFromSet command) {
        @SuppressWarnings("unchecked")
        final Set<Object> set = (Set<Object>) parent.getById(command.getListId());
        final Object value =
                command.getSimpleObjectValue() != null ? command.getSimpleObjectValue() : parent.getById(command
                        .getObservableObjectId());
        if (value == null) {
            topology.onError(new SynchronizeFXException(
                    "RemoveFromSet command with unknown value object id recived. " + command.getSimpleObjectValue()));
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                listeners.disableFor(set);
                set.remove(value);
                listeners.enableFor(set);
            }
        };
        if (parent.isDoChangesInJavaFxThread()) {
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }
    }

    private void execute(final SetRootElement command) {
        Object root = this.parent.getById(command.getRootElementId());
        this.parent.setRoot(root);
    }
}
