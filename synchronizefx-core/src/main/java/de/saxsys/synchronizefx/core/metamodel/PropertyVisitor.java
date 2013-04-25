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
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;

/**
 * Visits all observable objects and their fields that extend {@link Property} in a domain model.
 * 
 * The {@link Property} fields for each object are only visited once. Even if they occur multiple times in the domain
 * model. This prevents endless loops.
 * 
 * Parents are visited before their childs.
 */
abstract class PropertyVisitor {
    /**
     * All class names of classes that are known to have no property fields and hence are treated as simple objects.
     */
    private static Set<Class<?>> simpleObjects = Collections.synchronizedSet(new HashSet<Class<?>>());

    private final Map<Object, Object> alreadyVisited = new IdentityHashMap<>();
    private Field currentField;

    private Deque<Object> currentObservableObject = new LinkedList<>();
    private Deque<Parent> parent = new LinkedList<>();

    /**
     * Starts the visiting of an object.
     * 
     * @param object The object that's {@link Property} fields should be visited.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SecurityException
     */
    PropertyVisitor(final Object object) throws IllegalArgumentException, IllegalAccessException, SecurityException {
        parent.push(new Parent(null, null, null, null));
        visit(object);
    }

    /**
     * Called when starting visiting an observable object.
     * 
     * This is called before any {@link Property} fields of the observable object are visited. The {@link Property}
     * fields of the observable object are only visited if they have not been visited yet to avoid endless loops.
     * 
     * Overwrite this method if you are interested in such events. The default implementation does nothing.
     */
    protected void visitObservableObjectStart() {
        // Does nothing by default. It is not declared abstract so that the user is not forced to implement if he is not
        // interested in this kind of events.
    }

    /**
     * Called when visiting of an observable object has finished.
     */
    protected void visitObservableObjectEnd() {
        // Does nothing by default.
    }

    /**
     * Called when a simple object is visited. This is a object without any {@link Property} fields.
     * 
     * @param object The simple object visited.
     */
    protected void visitSimpleObject(final Object object) {
        // Does nothing by default.
    }

    /**
     * Visit a field of type {@link ListProperty}.
     * 
     * @param fieldValue The value that is bound to the field.
     * @return {@code true} if the childs of this property should be visited, {@code false} if not.
     */
    protected abstract boolean visitCollectionProperty(ListProperty<?> fieldValue);

    /**
     * Visit a field of type {@link MapProperty}.
     * 
     * @param fieldValue The value that is bound to the field.
     * @return {@code true} if the childs of this property should be visited, {@code false} if not.
     */
    protected abstract boolean visitCollectionProperty(MapProperty<?, ?> fieldValue);

    /**
     * Visit a field of type {@link SetProperty}.
     * 
     * @param fieldValue The value that is bound to the field.
     * @return {@code true} if the childs of this property should be visited, {@code false} if not.
     */
    protected abstract boolean visitCollectionProperty(SetProperty<?> fieldValue);

    /**
     * Visit a field of type {@link Property} which doesn't hold a collection.
     * 
     * That means that this method doesn't visit {@link ListProperty}s, {@link SetProperty}s and {@link MapProperty}s.
     * 
     * @param fieldValue The value that is bound to the field.
     * @return {@code true} if the childs of this property should be visited, {@code false} if not.
     */
    protected abstract boolean visitSingleValueProperty(Property<?> fieldValue);

    /**
     * @return The {@link Field} object that describes the currently visited field.
     */
    public Field getCurrentField() {
        return currentField;
    }

    /**
     * @return The observable object that's fields are visited at the moment.
     */
    public Object getCurrentObservableObject() {
        return currentObservableObject.peek();
    }

    /**
     * @return The parent {@link ListProperty} if the parent of the current object is a {@link ListProperty}. If not or
     *         if this is the root element and therefore their is no parent {@code null} is returned.
     */
    public ListProperty<?> getParentList() {
        return parent.peek().parentList;
    }

    /**
     * @return The parent {@link SetProperty} if the parent of the current object is a {@link SetProperty}. If not or if
     *         this is the root element and therefore their is no parent {@code null} is returned.
     */
    public SetProperty<?> getParentSet() {
        return parent.peek().parentSet;
    }

    /**
     * @return The parent {@link MapProperty} if the parent of the current object is a {@link MapProperty}. If not or if
     *         this is the root element and therefore their is no parent {@code null} is returned.
     */
    public MapProperty<?, ?> getParentMap() {
        return parent.peek().parentMap;
    }

    /**
     * @return The parent single-value {@link Property} if the parent of the current object is a {@link Property} and it
     *         is not a {@link ListProperty}, a {@link SetProperty} or a {@link MapProperty}. If not or if this is the
     *         root element and therefore their is no parent {@code null} is returned.
     */
    public Property<?> getParentProperty() {
        return parent.peek().parentProperty;
    }

    private void visit(final Object object) throws IllegalAccessException {
        if (object == null) {
            return;
        }
        if (simpleObjects.contains(object.getClass())) {
            visitSimpleObject(object);
            return;
        }
        if (alreadyVisited.containsKey(object)) {
            startVisiting(object);
            stopVisiting();
            return;
        }

        boolean isObservableObject = visitFields(object);
        if (isObservableObject) {
            stopVisiting();
        } else {
            visitSimpleObject(object);
            simpleObjects.add(object.getClass());
        }
    }

    /**
     * 
     * @param object The object which fields should be visited.
     * @return {@code true} when the object was a observable object, {@code false} when it was a simple object.
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private boolean visitFields(final Object object) throws IllegalAccessException {
        boolean isObservableObject = false;
        for (final Field field : getInheritedFields(object.getClass())) {
            field.setAccessible(true);
            currentField = field;
            final Class<?> fieldClass = field.getType();

            if (!isObservableObject && classImplements(fieldClass, Property.class)) {
                startVisiting(object);
                isObservableObject = true;
            }

            if (fieldClass == ListProperty.class) {
                handle((ListProperty<?>) field.get(object));
            } else if (fieldClass == SetProperty.class) {
                handle((SetProperty<?>) field.get(object));
            } else if (fieldClass == MapProperty.class) {
                handle((MapProperty<?, ?>) field.get(object));
            } else if (classImplements(fieldClass, Property.class)) {
                handle((Property<?>) field.get(object));
            }
        }
        return isObservableObject;
    }

    private void startVisiting(final Object object) {
        alreadyVisited.put(object, null);
        currentObservableObject.push(object);
        visitObservableObjectStart();
    }

    private void stopVisiting() {
        visitObservableObjectEnd();
        currentObservableObject.pop();
    }

    private void handle(final ListProperty<?> property) throws IllegalAccessException {
        if (visitCollectionProperty(property)) {
            for (Object child : property) {
                visit(child);
            }
        }
    }

    private void handle(final SetProperty<?> property) throws IllegalAccessException {
        if (visitCollectionProperty(property)) {
            for (Object child : property) {
                visit(child);
            }
        }
    }

    private void handle(final MapProperty<?, ?> property) throws IllegalAccessException {
        if (visitCollectionProperty(property)) {
            for (Entry<?, ?> entry : property.entrySet()) {
                visit(entry.getKey());
                visit(entry.getValue());
            }
        }
    }

    private void handle(final Property<?> property) throws IllegalAccessException {
        if (visitSingleValueProperty(property)) {
            Object value = property.getValue();
            visit(value);
        }
    }

    private List<Field> getInheritedFields(final Class<?> type) {
        final List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    /**
     * Checks if a {@link Class} implements a specified interface.
     * 
     * @param is The class that should be checked if it implements an interface.
     * @param should The interface that should be checked for.
     * @return {@code true} if the class implements the interface {@code false} otherwise.
     */
    private boolean classImplements(final Class<?> is, final Class<?> should) {
        for (Class<?> clazz : is.getInterfaces()) {
            if (clazz.equals(should)) {
                return true;
            }
            if (classImplements(clazz, should)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A wrapper for all possible parents.
     */
    private static final class Parent {
        private final ListProperty<?> parentList;
        private final SetProperty<?> parentSet;
        private final MapProperty<?, ?> parentMap;
        private final Property<?> parentProperty;

        Parent(final Property<?> parentProperty, final ListProperty<?> parentList, final SetProperty<?> parentSet,
                final MapProperty<?, ?> parentMap) {
            this.parentList = parentList;
            this.parentSet = parentSet;
            this.parentMap = parentMap;
            this.parentProperty = parentProperty;
        }
    }

    /**
     * Checks if objects of a class are observable objects.
     * 
     * @param clazz The class to check
     * @return {@code true} if they are observable objects, {@code false} if not.
     */
    public static boolean isObservableObject(final Class<?> clazz) {
        return !simpleObjects.contains(clazz);
    }
}
