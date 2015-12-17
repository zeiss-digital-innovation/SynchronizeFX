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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.CreateObservableObject;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.testutils.EasyCommandsForDomainModel;
import de.saxsys.synchronizefx.core.testutils.SaveParameterCallback;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests if {@link Property} fields that aren't {@link ListProperty}, {@link MapProperty}, or {@link SetProperty} are
 * synchronized correctly.
 * 
 * @author Raik Bieniek
 */
public class SyncSingleValuePropertyTest {

    private Root root;
    private SaveParameterCallback cb;
    private MetaModel model;

    /**
     * Initializes an example domain object and the meta model.
     */
    @Before
    public void init() {
        root = new Root();
        this.cb = new SaveParameterCallback();
        model = new MetaModel(this.cb, root);
    }

    /**
     * Tests the user initiated creation of commands for an observable object that contains {@link Property}s.
     */
    @Test
    public void testManualCreate() {
        // set some test data
        root.someString.set("Test");

        // create commands
        List<Command> commands = EasyCommandsForDomainModel.commandsForDomainModel(model);

        // check created commands
        boolean createRootObject = false;
        boolean createChildObject = false;
        boolean setTestValue = false;

        for (Object command : commands) {
            if (command instanceof CreateObservableObject) {
                CreateObservableObject cob = (CreateObservableObject) command;
                if (Root.class.getName().equals(cob.getClassName())) {
                    assertEquals(4, cob.getPropertyNameToId().size());
                    createRootObject = true;
                } else if (Child.class.getName().equals(cob.getClassName())) {
                    assertEquals(1, cob.getPropertyNameToId().size());
                    createChildObject = true;
                }
            } else if (command instanceof SetPropertyValue) {
                SetPropertyValue spv = (SetPropertyValue) command;
                if ("Test".equals(spv.getValue().getSimpleObjectValue())) {
                    setTestValue = true;
                }
            }
        }

        assertTrue(createRootObject);
        assertTrue(createChildObject);
        assertTrue(setTestValue);
    }

    /**
     * Tests that appropriate commands are generated when the value of a property is changed.
     */
    @Test
    public void testSetProperty() {
        // set simple object value
        root.someString.set("Some Test String");
        SetPropertyValue msg1 = (SetPropertyValue) cb.getCommands().get(0);
        assertNull(msg1.getValue().getObservableObjectId());
        assertEquals("Some Test String", msg1.getValue().getSimpleObjectValue());

        // set observable object value;
        Child newChild = new Child();
        newChild.childInt.set(275);
        root.someChild.set(newChild);
        // create the new child
        assertEquals(cb.getCommands().get(0).getClass(), CreateObservableObject.class);
        // get(1) = SetPropertyValue for childInt in child; get(2) = SetPropertyValue for child in Root
        SetPropertyValue msg2 = (SetPropertyValue) cb.getCommands().get(2);
        assertNull(msg2.getValue().getSimpleObjectValue());
        assertNotNull(msg2.getValue().getObservableObjectId());
    }

    /**
     * Tests that changes done to child observable objects that are saved in a {@link Property} of parent observable
     * object are also synchronized.
     */
    @Test
    public void testChangesOnChilds() {
        root.someChild.get().childInt.set(547);
        SetPropertyValue msg = (SetPropertyValue) cb.getCommands().get(0);

        assertEquals(547, msg.getValue().getSimpleObjectValue());
        assertNull(msg.getValue().getObservableObjectId());
    }

    /**
     * Tests that the generated commands can be a applied on copies of the domain model.
     * 
     * When done so, the copies should be equal to the original.
     */
    @Test
    public void testApplyGeneratedCommands() {
        // setup
        SaveParameterCallback copyCb = new SaveParameterCallback();
        MetaModel copyMeta = new MetaModel(copyCb);
        copyMeta.execute(EasyCommandsForDomainModel.commandsForDomainModel(model));
        Root copyRoot = (Root) copyCb.getRoot();

        assertEquals(copyRoot, root);

        // change simple object in Root
        root.someString.set("something");
        assertNotEquals(copyRoot, root);
        copyMeta.execute(cb.getCommands());
        assertEquals(copyRoot, root);

        // change simple object in Child
        root.someChild.get().childInt.set(42);
        assertNotEquals(copyRoot, root);
        copyMeta.execute(cb.getCommands());
        assertEquals(copyRoot, root);

        // change observable object in Root
        root.someChild.set(new Child());
        assertNotEquals(copyRoot, root);
        copyMeta.execute(cb.getCommands());
        assertEquals(copyRoot, root);
    }

    /**
     * Usually {@link SimpleLongProperty} are assigned to {@link LongProperty} class members but even if the are
     * assigned to {@link SimpleLongProperty} class members the synchronization should work.
     */
    @Test
    public void shouldSynchronizeFieldsWithConcreteTypes() {
        root.fieldWithConcreteType.set(53L);
        
        assertThat(cb.getCommands()).isNotNull().hasSize(2);
        SetPropertyValue msg = (SetPropertyValue) cb.getCommands().get(0);
        assertThat(msg.getValue().getObservableObjectId()).isNull();
        assertThat(msg.getValue().getSimpleObjectValue()).isEqualTo(53L);
    }
    
    /**
     * Class fields of the plain {@link Property} type should also be synchronized.
     */
    @Test
    public void plainPropertyFieldShouldBeSynchronized() {
        root.shouldAlsoBeSynchronized.setValue("SomeString");
        
        assertThat(cb.getCommands()).isNotNull().hasSize(2);
        SetPropertyValue msg = (SetPropertyValue) cb.getCommands().get(0);
        assertThat(msg.getValue().getObservableObjectId()).isNull();
        assertThat(msg.getValue().getSimpleObjectValue()).isEqualTo("SomeString");
    }

    private static void assertNotEquals(final Object obj1, final Object obj2) {
        assertFalse(obj2.equals(obj1));
    }

    /**
     * An example domain class that should be synchronized.
     * 
     */
    private static final class Root {

        double notSynced = 2.0;
        final StringProperty someString = new SimpleStringProperty();
        final ObjectProperty<Child> someChild = new SimpleObjectProperty<>(new Child());
        final SimpleLongProperty fieldWithConcreteType = new SimpleLongProperty();
        final Property<String> shouldAlsoBeSynchronized = new SimpleStringProperty();
        
        Root() {

        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(notSynced);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + ((someChild.get() == null) ? 0 : someChild.get().hashCode());
            result = prime * result + ((someString.get() == null) ? 0 : someString.get().hashCode());
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
            Root other = (Root) obj;
            if (Double.doubleToLongBits(notSynced) != Double.doubleToLongBits(other.notSynced)) {
                return false;
            }
            if (someString.get() == null) {
                if (other.someString.get() != null) {
                    return false;
                }
            } else if (!someString.get().equals(other.someString.get())) {
                return false;
            }
            if (someChild.get() == null) {
                if (other.someChild.get() != null) {
                    return false;
                }
            } else if (!someChild.get().equals(other.someChild.get())) {
                return false;
            }
            return true;
        }

        // CHECKSTYLE:ON

        @Override
        public String toString() {
            return "Root [notSynced=" + notSynced + ", someString=" + someString + ", someChild=" + someChild + "]";
        }
    }

    /**
     * This class is part of {@link Root}.
     * 
     * @see Root
     * @author raik.bieniek
     * 
     */
    private static final class Child {
        IntegerProperty childInt = new SimpleIntegerProperty();

        Child() {

        }

        @Override
        public int hashCode() {
            return childInt.get();
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
            Child other = (Child) obj;
            if (childInt.get() != other.childInt.get()) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Child [childInt=" + childInt + "]";
        }
    }
}
