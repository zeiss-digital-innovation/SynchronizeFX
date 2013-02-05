package de.saxsys.synchronizefx.core.metamodel;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
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
 * Creates various types of commands that describe changes on the domain model.
 */
class CommandListCreator {
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
     * @return The commands necessary to rebuild the domain model at it's current state.
     */
    public List<Object> commandsForDomainModel(final Object root) {
        State state = new State(false);
        createObservableObject(root, state);

        SetRootElement msg = new SetRootElement();
        msg.setRootElementId(parent.getId(root));
        state.commands.add(msg);

        state.commands.add(new ClearReferences());
        return state.commands;
    }

    /**
     * Creates the messages necessary to set a new value for a property.
     * 
     * @param propertyId The id of the property where the new value should be set.
     * @param value The value that should be set.
     * @return The commands.
     */
    public List<Object> setPropertyValue(final UUID propertyId, final Object value) {
        State state = new State(true);
        setPropertyValue(propertyId, value, state);
        state.commands.add(new ClearReferences());
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
        State state = new State(true);
        addToList(listId, position, value, state);
        state.commands.add(new ClearReferences());
        return state.commands;
    }

    /**
     * Creates the list with commands necessary for an add to set action.
     * 
     * @param setId The ID of the set where the element should be added.
     * @param value The object that should be added to the set.
     * @return a set with commands necessary to recreate this add to set command.
     */
    public List<Object> addToSet(UUID setId, Object value) {
        State state = new State(true);
        addToSet(setId, value, state);
        state.commands.add(new ClearReferences());
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
        State state = new State(true);
        putToMap(mapId, key, value, state);
        state.commands.add(new ClearReferences());
        return state.commands;
    }

    /**
     * Creates the list with command necessard to remove a mapping from a map.
     * 
     * @param mapId the map where the mapping should be removed.
     * @param key the key of the mapping that should be removed.
     * @return the list with the commands.
     */
    public List<Object> removeFromMap(final UUID mapId, final Object key) {
        State state = new State(false);

        RemoveFromMap msg = new RemoveFromMap();
        msg.setMapId(mapId);
        boolean keyIsObservableObject = createObservableObject(key, state);

        if (keyIsObservableObject) {
            msg.setKeyObservableObjectId(parent.getId(key));
        } else {
            msg.setKeySimpleObjectValue(key);
        }
        state.commands.add(msg);
        state.commands.add(new ClearReferences());
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
        List<Object> list = new LinkedList<Object>();
        RemoveFromList msg = new RemoveFromList();
        msg.setListId(listId);
        msg.setPosition(position);
        list.add(msg);
        return list;
    }

    /**
     * Creates the list with commands necessary to remove a object from a set.
     * 
     * @param listId The ID of the list where an element should be removed.
     * @param value The element that should be removed.
     * @return The command list.
     */
    public List<Object> removeFromSet(UUID setId, Object value) {
        State state = new State(false);

        RemoveFromSet msg = new RemoveFromSet();
        msg.setSetId(setId);
        boolean keyIsObservableObject = createObservableObject(value, state);

        if (keyIsObservableObject) {
            msg.setObservableObjectId(parent.getId(value));
        } else {
            msg.setSimpleObjectValue(value);
        }
        
        state.commands.add(msg);
        state.commands.add(new ClearReferences());
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

    private void addToSet(UUID setId, Object value, State state) {
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
            return false;
        }

        if (state.alreadyVisited.containsKey(value)) {
            return true;
        }
        state.alreadyVisited.put(value, null);

        if (state.skipKnown && parent.getId(value) != null) {
            return true;
        }

        final CreateObservableObject msg = new CreateObservableObject();
        int currentSize = state.commands.size();

        try {
            new PropertyVisitor(value) {
                @Override
                protected boolean visitSingleValueProperty(final Property<?> fieldValue) {
                    UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    setPropertyValue(fieldId, fieldValue.getValue(), state);
                    return false;
                }

                @Override
                protected boolean visitCollectionProperty(final ListProperty<?> fieldValue) {
                    UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    for (Object o : fieldValue) {
                        addToList(fieldId, fieldValue.indexOf(o), o, state);
                    }
                    return false;
                }

                @Override
                protected boolean visitCollectionProperty(final MapProperty<?, ?> fieldValue) {
                    UUID fieldId = registerPropertyAndParent(getCurrentField(), fieldValue);
                    for (Entry<?, ?> entry : fieldValue.entrySet()) {
                        putToMap(fieldId, entry.getKey(), entry.getValue(), state);
                    }
                    return false;
                }

                @Override
                protected boolean visitCollectionProperty(final SetProperty<?> fieldValue) {
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
            return false;
        }
        msg.setClassName(value.getClass().getName());
        // create the object before it's field values are set
        state.commands.add(currentSize, msg);
        return true;
    }

    /**
     * The state that must be keeped for the creation of depend messages.
     */
    private static class State {
        private final Map<Object, Object> alreadyVisited = new IdentityHashMap<>();
        private final List<Object> commands = new LinkedList<>();
        private final boolean skipKnown;

        public State(final boolean skipKnown) {
            this.skipKnown = skipKnown;
        }
    }
}
