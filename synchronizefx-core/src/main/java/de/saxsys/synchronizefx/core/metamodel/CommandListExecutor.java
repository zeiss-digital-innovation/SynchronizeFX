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
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javafx.beans.property.Property;

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
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies commands on a {@link MetaModel} to synchronize it's domain objects with other instances.
 * 
 */
public class CommandListExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(CommandListExecutor.class);

    private final MetaModel parent;
    private final Listeners listeners;
    private final TopologyLayerCallback topology;
    private final SilentChangeExecutor changeExecutor;
    private final ValueMapper valueMapper;

    /**
     * A set that holds hard references to objects that would otherwise only have weak references and thus could get
     * garbage collected before they are used.
     */
    private final Map<Object, Object> hardReferences = new IdentityHashMap<>();
    // only needed for tracing
    private Map<Property<?>, Field> propFieldMap;

    /**
     * Initializes the executor.
     * 
     * @param parent
     *            The model to user for id lookup and registration.
     * @param listeners
     *            The listeners that should be registered on new properties.
     * @param changeExecutor
     *            Used to prevent generation of change commands when doing changes to the users domain model.
     * @param valueMapper
     *            Used to translate {@link Value} messages to the real values the represent.
     * @param topology
     *            The user callback that should be used to report errors.
     */
    public CommandListExecutor(final MetaModel parent, final Listeners listeners,
            final SilentChangeExecutor changeExecutor, final ValueMapper valueMapper,
            final TopologyLayerCallback topology) {
        this.topology = topology;
        this.parent = parent;
        this.changeExecutor = changeExecutor;
        this.listeners = listeners;
        this.valueMapper = valueMapper;
        if (LOG.isTraceEnabled()) {
            propFieldMap = new HashMap<>();
        }
    }

    /**
     * @see MetaModel#execute(Object)
     * @param command
     *            The command to execute.
     * @throws SynchronizeFXException
     *             when the execution of an command failed.
     */
    public void execute(final Object command) throws SynchronizeFXException {
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
            for (final Entry<String, UUID> entry : command.getPropertyNameToId().entrySet()) {
                Class<?> current = objClass;
                boolean fieldFound = false;
                while (current != Object.class) {
                    try {
                        final Field field = current.getDeclaredField(entry.getKey());
                        field.setAccessible(true);
                        parent.registerObject(field.get(obj), entry.getValue());
                        fieldFound = true;
                        break;
                    } catch (final NoSuchFieldException e) {
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
        } catch (final InstantiationException e) {
            topology.onError(new SynchronizeFXException(
                    "Maybe you've forgot to add a public no-arg constructor to one of your domain objects?", e));
            return;
        } catch (final IllegalAccessException e) {
            topology.onError(new SynchronizeFXException(
                    "Maybe one of your no-arg constructor of one of your domain objects is not public?", e));
            return;
        } catch (final ClassNotFoundException e) {
            topology.onError(new SynchronizeFXException(
                    "Maybe not all of you're domain objects or their dependencies are availabe on every node?", e));
            return;
        } catch (final SecurityException e) {
            topology.onError(new SynchronizeFXException(
                    "Maybe you're JVM doesn't allow reflection for this application?", e));
            return;
        }

        hardReferences.put(obj, null);
        parent.registerObject(obj, command.getObjectId());
    }

    private void execute(final SetPropertyValue command) throws SynchronizeFXException {
        @SuppressWarnings("unchecked")
        final Property<Object> prop = (Property<Object>) parent.getById(command.getPropertyId());
        if (prop == null) {
            topology.onError(new SynchronizeFXException("SetPropertyValue with unknown property id recived. "
                    + command.getPropertyId()));
            return;
        }
        if (LOG.isTraceEnabled()) {
            final Field field = propFieldMap.get(prop);
            if (field != null) {
                LOG.trace("Set on field " + field + " value " + command);
            } else {
                LOG.trace(command.toString());
            }
        }

        final Object value = valueMapper.map(command.getValue()).getValue();

        changeExecutor.execute(prop, new Runnable() {
            @Override
            public void run() {
                prop.setValue(value);
            }
        });
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
            final Field field = propFieldMap.get(list);
            if (field != null) {
                LOG.trace("Add to list " + field + " value " + command);
            } else {
                LOG.trace(command.toString());
            }
        }
        
        final ObservedValue value = valueMapper.map(command.getValue());

        changeExecutor.execute(list, new Runnable() {
            @Override
            public void run() {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Add value {} to list {} at position {}.",
                            new Object[] { value, Arrays.toString(list.toArray()), command.getPosition() });
                }
                if (list.size() >= command.getNewSize()) {
                    LOG.warn("Preconditions to apply AddToList command are not met. This may be OK if you've just connected.");
                    return;
                }
                list.add(command.getPosition(), value.getValue());
            }
        });
    }

    private void execute(final RemoveFromList command) {
        @SuppressWarnings("unchecked")
        final List<Object> list = (List<Object>) parent.getById(command.getListId());
        if (LOG.isTraceEnabled()) {
            final Field field = propFieldMap.get(list);
            if (field != null) {
                LOG.trace("Remove from list " + field + " value " + command);
            } else {
                LOG.trace(command.toString());
            }
        }
        changeExecutor.execute(list, new Runnable() {
            @Override
            public void run() {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Remove from list {} at position {}.", Arrays.toString(list.toArray()),
                            command.getPosition());
                }
                if (list.size() <= command.getNewSize()) {
                    LOG.warn("Preconditions to apply RemoveFromList command are not met."
                            + "This may be OK if you've just connected.");
                    return;
                }
                list.remove(command.getPosition());
            }
        });
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
            final Field field = propFieldMap.get(map);
            if (field != null) {
                LOG.trace("Put in map " + field + " value " + command);
            } else {
                LOG.trace(command.toString());
            }
        }

        final ObservedValue key = valueMapper.map(command.getKey());
        final ObservedValue value = valueMapper.map(command.getValue());
        
        changeExecutor.execute(map, new Runnable() {
            @Override
            public void run() {
                map.put(key.getValue(), value.getValue());
            }
        });
    }

    private void execute(final RemoveFromMap command) {
        @SuppressWarnings("unchecked")
        final Map<Object, Object> map = (Map<Object, Object>) parent.getById(command.getMapId());
        final ObservedValue key = valueMapper.map(command.getKey());
        
        changeExecutor.execute(map, new Runnable() {
            @Override
            public void run() {
                map.remove(key.getValue());
            }
        });
    }

    private void execute(final AddToSet command) {
        @SuppressWarnings("unchecked")
        final Set<Object> set = (Set<Object>) parent.getById(command.getSetId());
        if (set == null) {
            topology.onError(new SynchronizeFXException("AddToSet command with unknown list id recived. "
                    + command.getSetId()));
            return;
        }
        final ObservedValue value = valueMapper.map(command.getValue());

        changeExecutor.execute(set, new Runnable() {
            @Override
            public void run() {
                set.add(value.getValue());
            }
        });
    }

    private void execute(final RemoveFromSet command) {
        @SuppressWarnings("unchecked")
        final Set<Object> set = (Set<Object>) parent.getById(command.getSetId());
        final ObservedValue value = valueMapper.map(command.getValue());
        
        changeExecutor.execute(set, new Runnable() {
            @Override
            public void run() {
                set.remove(value.getValue());
            }
        });
    }

    private void execute(final SetRootElement command) {
        final Object root = this.parent.getById(command.getRootElementId());
        this.parent.setRoot(root);
    }
}
