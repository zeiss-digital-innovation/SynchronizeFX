package de.saxsys.synchronizefx.core.metamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import org.junit.Before;
import org.junit.Test;

import de.saxsys.synchronizefx.core.metamodel.commands.CreateObservableObject;
import de.saxsys.synchronizefx.core.metamodel.commands.PutToMap;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromMap;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.testutils.SaveParameterCallback;

/**
 * Test if {@link MapPropery} fields in observable objects are synchronized properly.
 * 
 * @author raik.bieniek
 * 
 */
public class SyncMapPropertyTest {
    private MetaModel model;
    private Root root;
    private SaveParameterCallback cb;

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
     * Tests the user initiated creation of commands for an observable object that contains {@link MapProperty}s.
     */
    @Test
    public void testManualCreate() {
        UUID testId = UUID.randomUUID();
        String testString = "testString";
        root.map.put(testId, testString);

        List<Object> commands = model.commandsForDomainModel();
        CreateObservableObject msg = (CreateObservableObject) commands.get(0);

        assertEquals(2, msg.getPropertyNameToId().size());
        assertNotNull(msg.getPropertyNameToId().get("map"));
        assertNotNull(msg.getPropertyNameToId().get("otherMap"));

        // test that some PutToMap messages puts "testString" as value
        boolean correctSetValueMessageFound = false;
        for (Object command : commands) {
            if (command instanceof PutToMap) {
                if ("testString".equals(((PutToMap) command).getValueSimpleObjectValue())) {
                    correctSetValueMessageFound = true;
                    break;
                }
            }
        }
        assertTrue(correctSetValueMessageFound);
    }

    /**
     * Tests that the framework produces the right change commands on its own, when the user puts a mapping into the
     * map.
     */
    @Test
    public void testPut() {
        UUID testId = UUID.randomUUID();
        String testString = "testString";

        root.map.put(testId, testString);
        List<Object> commands1 = cb.getCommands();
        PutToMap msg1 = (PutToMap) commands1.get(0);

        assertNotNull(msg1.getMapId());
        assertNull(msg1.getKeyObservableObjectId());
        assertEquals(msg1.getKeySimpleObjectValue(), testId);
        assertNull(msg1.getValueObservableObjectId());
        assertEquals(msg1.getValueSimpleObjectValue(), testString);

        Child key = new Child();
        Child value = new Child();

        root.otherMap.put(key, value);
        List<Object> commands2 = cb.getCommands();
        // new commands should have been generated
        assertNotSame(commands1, commands2);

        // key object CreateObservableObject + SetPropertyValue(someInt=0)
        assertTrue(commands2.get(0) instanceof CreateObservableObject);
        // value object CreateObservableObject + SetPropertyValue(someInt=0)
        assertTrue(commands2.get(2) instanceof CreateObservableObject);

        PutToMap msg2 = (PutToMap) commands2.get(4);

        assertNotNull(msg2.getMapId());
        assertEquals(key, model.getById(msg2.getKeyObservableObjectId()));
        assertNull(msg2.getKeySimpleObjectValue());
        assertEquals(value, model.getById(msg2.getValueObservableObjectId()));
        assertNull(msg2.getValueSimpleObjectValue());
    }

    /**
     * Tests that the framework produces the right change commands on its own, when the user removes a mapping from the
     * map.
     */
    @Test
    public void testRemoveAMapping() {
        UUID testId1 = UUID.randomUUID();
        UUID testId2 = UUID.randomUUID();
        root.map.put(testId1, "testString");
        root.map.put(testId2, "testString");
        root.otherMap.put(new Child(0), new Child(1));
        root.otherMap.put(new Child(42), new Child(1337));
        
        root.map.remove(testId1);
        RemoveFromMap msg = (RemoveFromMap) cb.getCommands().get(0);
        assertEquals(root.map, model.getById(msg.getMapId()));
        assertNull(msg.getKeyObservableObjectId());
        assertEquals(testId1, msg.getKeySimpleObjectValue());
        
        root.otherMap.remove(new Child(0));
        assertEquals(CreateObservableObject.class, cb.getCommands().get(0).getClass());
        RemoveFromMap msg2 = (RemoveFromMap) cb.getCommands().get(2);
        assertEquals(root.otherMap, model.getById(msg2.getMapId()));
        assertNull(msg2.getKeySimpleObjectValue());
        assertEquals(new Child(0), model.getById(msg2.getKeyObservableObjectId()));
    }

    /**
     * Tests that command that modify a map can be applied.
     * 
     * When the commands that are generated when an original map is changed are applied to an copy that was created
     * before this changes the original and the copy should be equal again.
     */
    @Test
    public void testApplyGeneratedMessages() {
        SaveParameterCallback copyCb = new SaveParameterCallback();
        MetaModel copy = new MetaModel(copyCb);
        UUID uuid = UUID.randomUUID();

        copy.execute(model.commandsForDomainModel());
        Root copyRoot = (Root) copyCb.getRoot();

        assertEquals(root, copyRoot);

        root.map.put(uuid, "someValue");
        assertFalse(root.equals(copyRoot));
        copy.execute(cb.getCommands());
        assertEquals(root, copyRoot);

        root.map.remove(uuid);
        assertFalse(root.equals(copyRoot));
        copy.execute(cb.getCommands());
        assertEquals(root, copyRoot);

        root.otherMap.put(new Child(40), new Child(50));
        assertFalse(root.equals(copyRoot));
        copy.execute(cb.getCommands());
        assertEquals(root, copyRoot);
    }

    /**
     * Tests that listeners to childs of a map are registered properly when they are put to the map.
     */
    @Test
    public void testChangesOnChilds() {
        Child key = new Child();
        Child value = new Child();

        root.otherMap.put(key, value);
        key.someInt.set(42);
        assertEquals(42, ((SetPropertyValue) cb.getCommands().get(0)).getSimpleObjectValue());

        value.someInt.set(1337);
        assertEquals(1337, ((SetPropertyValue) cb.getCommands().get(0)).getSimpleObjectValue());
    }

    /**
     * An example domain class that should be synchronized.
     * 
     */
    private static class Root {
        @SuppressWarnings("unused")
        final ObservableMap<UUID, String> notSynchronized = FXCollections.observableMap(new HashMap<UUID, String>());
        final MapProperty<UUID, String> map = new SimpleMapProperty<>(
                FXCollections.observableMap(new HashMap<UUID, String>()));
        final MapProperty<Child, Child> otherMap = new SimpleMapProperty<>(
                FXCollections.observableMap(new HashMap<Child, Child>()));

        public Root() {

        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((map.get() == null) ? 0 : map.get().hashCode());
            result = prime * result + ((otherMap.get() == null) ? 0 : otherMap.get().hashCode());
            return result;
        }

        // CHECKSTYLE:OFF more or less generated code
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Root other = (Root) obj;
            if (map == null) {
                if (other.map != null) {
                    return false;
                }
            } else if (!map.get().equals(other.map.get())) {
                return false;
            }
            if (otherMap == null) {
                if (other.otherMap != null) {
                    return false;
                }
            } else if (!otherMap.get().equals(other.otherMap.get())) {
                return false;
            }
            return true;
        }
        // CHECKSTYLE:ON
    }

    /**
     * Part of {@link Root}.
     * 
     * @see Root
     */
    private static class Child {
        final IntegerProperty someInt = new SimpleIntegerProperty();

        public Child(final int someInt) {
            this.someInt.set(someInt);
        }

        public Child() {
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
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Child other = (Child) obj;
            if (someInt.get() != other.someInt.get()) {
                return false;
            }
            return true;
        }
    }
}
