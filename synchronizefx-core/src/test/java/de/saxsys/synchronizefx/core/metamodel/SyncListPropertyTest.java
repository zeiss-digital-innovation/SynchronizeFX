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

import java.util.LinkedList;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.CreateObservableObject;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.testutils.DirectExecutor;
import de.saxsys.synchronizefx.core.testutils.EasyCommandsForDomainModel;
import de.saxsys.synchronizefx.core.testutils.SaveParameterCallback;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test if {@link ListPropery} fields in observable objects are synchronized properly.
 * 
 * @author Raik Bieniek
 */
public class SyncListPropertyTest {
    private MetaModel model;
    private Root root;
    private SaveParameterCallback cb;

    /**
     * Initializes an example domain object and the meta model.
     */
    @Before
    public void init() {
        root = new Root();
        cb = new SaveParameterCallback();
        model = new MetaModel(cb, root, new DirectExecutor());
    }

    /**
     * Tests the user initiated creation of commands for an observable object that contains {@link ListProperty}s.
     */
    @Test
    public void testManualCreate() {
        final List<Command> commands = EasyCommandsForDomainModel.commandsForDomainModel(model);
        final CreateObservableObject msg = (CreateObservableObject) commands.get(0);

        assertEquals(3, msg.getPropertyNameToId().size());
        assertNotNull(msg.getPropertyNameToId().get("wrappedList"));
        assertNotNull(msg.getPropertyNameToId().get("newList"));
        assertNotNull(msg.getPropertyNameToId().get("childList"));
    }

    /**
     * Tests if the commands to reproduce an add operation to a list are correctly created.
     */
    @Test
    public void testAdd() {
        // test adding of a simple object
        root.wrappedList.add("Test Value 257");
        final AddToList msg1 = (AddToList) cb.getCommands().get(0);
        assertEquals("Test Value 257", msg1.getValue().getSimpleObjectValue());
        assertNull(msg1.getValue().getObservableObjectId());
        assertEquals(0, msg1.getPosition());
        assertEquals(1, msg1.getNewSize());

        // test adding of a observable object
        root.childList.add(new Child());
        final CreateObservableObject msg2 = (CreateObservableObject) cb.getCommands().get(0);
        // get(1) = SetPropertyValue for setting someInt property of Child to 0.
        final AddToList msg3 = (AddToList) cb.getCommands().get(2);

        assertEquals(Child.class.getName(), msg2.getClassName());
        assertNull(msg3.getValue().getSimpleObjectValue());
        assertEquals(msg2.getObjectId(), msg3.getValue().getObservableObjectId());
        assertEquals(1, msg3.getNewSize());

        // test that the position is set correctly
        root.wrappedList.add("some text");
        root.wrappedList.add(1, "some more text");
        final AddToList msg4 = (AddToList) cb.getCommands().get(0);
        assertEquals(1, msg4.getPosition());
        assertEquals(3, msg4.getNewSize());

    }

    /**
     * Tests if the commands to reproduce an remove operation to a list are correctly created.
     */
    @Test
    public void testRemove() {
        simpleTestData();

        // check for the correct positions in the generated commands
        root.wrappedList.remove("Test Value 2");
        final RemoveFromList msg0 = (RemoveFromList) cb.getCommands().get(0);
        assertEquals(2, msg0.getStartPosition());
        assertEquals(1, msg0.getRemoveCount());
        assertEquals(3, msg0.getNewSize());

        root.wrappedList.remove("Test Value 0");
        final RemoveFromList msg1 = (RemoveFromList) cb.getCommands().get(0);
        assertEquals(0, msg1.getStartPosition());
        assertEquals(1, msg1.getRemoveCount());
        assertEquals(2, msg1.getNewSize());

        root.childList.remove(new Child(2));
        final RemoveFromList msg2 = (RemoveFromList) cb.getCommands().get(0);
        assertEquals(2, msg2.getStartPosition());
        assertEquals(1, msg2.getRemoveCount());
        assertEquals(2, msg2.getNewSize());

        root.childList.remove(1);
        final RemoveFromList msg3 = (RemoveFromList) cb.getCommands().get(0);
        assertEquals(1, msg3.getStartPosition());
        assertEquals(1, msg3.getRemoveCount());
        assertEquals(1, msg3.getNewSize());
    }

    /**
     * Tests whether the replace operation (List#set(int, T)) correctly creates replace commands.
     */
    @Test
    public void testReplace() {
        simpleTestData();

        root.wrappedList.set(0, "42");

        final Command command = cb.getCommands().get(0);
        assertTrue(command instanceof ReplaceInList);
        final ReplaceInList replaceCommand = (ReplaceInList) command;
        assertEquals(0, replaceCommand.getPosition());
        assertEquals("42", replaceCommand.getValue().getSimpleObjectValue());

        final Child child42 = new Child(42);
        root.childList.set(2, child42);

        // create and set are first 2 commands
        final Command command2 = cb.getCommands().get(2);
        assertTrue(command2 instanceof ReplaceInList);
        final ReplaceInList replaceCommand2 = (ReplaceInList) command2;
        assertEquals(2, replaceCommand2.getPosition());
        assertNull(replaceCommand2.getValue().getSimpleObjectValue());
        assertNotNull(replaceCommand2.getValue().getObservableObjectId());
    }
    
    /**
     * Tests whether the {@link List#clear()} operation removes all elements from the list.
     */
    @Test
    public void testClear() {
        simpleTestData();
        
        root.wrappedList.clear();

        final Command command = cb.getCommands().get(0);
        assertTrue(command instanceof RemoveFromList);
        final RemoveFromList replaceCommand = (RemoveFromList) command;
        assertEquals(0, replaceCommand.getStartPosition());
        assertEquals(4, replaceCommand.getRemoveCount());
        assertEquals(0, replaceCommand.getNewSize());
    }

    /**
     * Tests that commands that modify a map can be applied.
     * 
     * When the commands that are generated when an original list is changed are applied to an copy that was created
     * before this changes the original and the copy should be equal again.
     */
    @Test
    public void testApplyGeneratedCommands() {
        final SaveParameterCallback copyCb = new SaveParameterCallback();
        final MetaModel copy = new MetaModel(copyCb, new DirectExecutor());

        copy.execute(EasyCommandsForDomainModel.commandsForDomainModel(model));
        final Root copyRoot = (Root) copyCb.getRoot();

        assertEquals(root, copyRoot);

        // test add simple object
        root.wrappedList.add("some string");
        assertFalse(root.equals(copyRoot));
        copy.execute(cb.getCommands());
        assertEquals(root, copyRoot);

        // test add observable object
        root.childList.add(new Child(859));
        assertFalse(root.equals(copyRoot));
        copy.execute(cb.getCommands());
        assertEquals(root, copyRoot);

        // test remove simple object
        root.wrappedList.remove(0);
        assertFalse(root.equals(copyRoot));
        copy.execute(cb.getCommands());
        assertEquals(root, copyRoot);

        // test remove observable object
        root.childList.remove(new Child(859));
        assertFalse(root.equals(copyRoot));
        copy.execute(cb.getCommands());
        assertEquals(root, copyRoot);
    }

    /**
     * Tests that listeners to elements of a list are registered properly when they are add to the list.
     */
    @Test
    public void testChangesOnChilds() {
        // setup
        final Child someChild = new Child();
        root.childList.add(someChild);

        // produce changes on child
        someChild.someInt.set(924);

        // check commands
        final SetPropertyValue msg = (SetPropertyValue) cb.getCommands().get(0);
        assertEquals(924, msg.getValue().getSimpleObjectValue());
        assertNull(msg.getValue().getObservableObjectId());
    }

    /**
     * Tests that the correct commands are generated when the list in a {@link ListProperty} is exchanged.
     */
    @Test
    @Ignore
    public void testExchangeList() {
        fail("not yet supported by the software.");
    }

    private void simpleTestData() {
        // first, add some test data
        root.wrappedList.add("Test Value 0");
        root.wrappedList.add("Test Value 1");
        root.wrappedList.add("Test Value 2");
        root.wrappedList.add("Test Value 3");

        root.childList.add(new Child(0));
        root.childList.add(new Child(1));
        root.childList.add(new Child(2));
    }

    /**
     * An example domain class that should be synchronized.
     * 
     * Unused fields are accessed via reflection in the framework.
     */
    private static final class Root {
        // not synchronized because only properties are synchronized
        @SuppressWarnings("unused")
        final ObservableList<String> notSynchronized = FXCollections.observableList(new LinkedList<String>());
        // framework synchronizes depending on the type of the field, not the actual type. Since Property isn't an
        // ancestor of ObservableList, this field will not be synchronized. TODO maybe this should be fixed.
        @SuppressWarnings("unused")
        final ObservableList<String> notSynchronizedEither = new SimpleListProperty<>(
                FXCollections.<String> observableArrayList());
        final ListProperty<String> newList = new SimpleListProperty<>();
        final ListProperty<String> wrappedList = new SimpleListProperty<>(FXCollections.<String> observableArrayList());
        final ListProperty<Child> childList = new SimpleListProperty<>(FXCollections.<Child> observableArrayList());

        public Root() {

        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((childList.get() == null) ? 0 : childList.get().hashCode());
            result = prime * result + ((newList.get() == null) ? 0 : newList.get().hashCode());
            result = prime * result + ((wrappedList.get() == null) ? 0 : wrappedList.get().hashCode());
            return result;
        }

        // CHECKSTYLE:OFF more or less generated code
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Root other = (Root) obj;
            if (childList.get() == null) {
                if (other.childList.get() != null) {
                    return false;
                }
            } else if (!childList.get().equals(other.childList.get())) {
                return false;
            }
            if (newList.get() == null) {
                if (other.newList.get() != null) {
                    return false;
                }
            } else if (!newList.get().equals(other.newList.get())) {
                return false;
            }
            if (wrappedList.get() == null) {
                if (other.wrappedList.get() != null) {
                    return false;
                }
            } else if (!wrappedList.get().equals(other.wrappedList.get())) {
                return false;
            }
            return true;
        }
        // CHECKSTYLE:ON
    }

    /**
     * Part of the example domain class.
     * 
     * @see Root
     */
    private static final class Child {
        final IntegerProperty someInt = new SimpleIntegerProperty();

        public Child() {

        }

        public Child(final int someInt) {
            this.someInt.set(someInt);
        }

        @Override
        public int hashCode() {
            return someInt.get();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Child other = (Child) obj;
            if (someInt.get() != other.someInt.get()) {
                return false;
            }
            return true;
        }
    }
}
