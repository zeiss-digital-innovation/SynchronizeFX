package de.saxsys.synchronizefx.core.metamodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.junit.Before;
import org.junit.Test;

import de.saxsys.synchronizefx.core.testutils.SaveParameterCallback;

/**
 * The framework should not depend on {@link Object#equals(Object)} and {@link Object#hashCode()}. This test makes sure
 * that it doesn't.
 */
public class EqualsHashCodeTest {

    private MessedUpHashCodeEqualsClass root;
    private SaveParameterCallback cb;
    private MetaModel meta;

    private MessedUpHashCodeEqualsClass copyRoot;
    private SaveParameterCallback copyCb;
    private MetaModel copyMeta;

    /**
     * Initializes an example domain object and the meta model.
     */
    @Before
    public void init() {
        root = new MessedUpHashCodeEqualsClass();
        cb = new SaveParameterCallback();
        meta = new MetaModel(cb, root);

        copyCb = new SaveParameterCallback();
        copyMeta = new MetaModel(copyCb);
        copyMeta.execute(meta.commandsForDomainModel());
        copyRoot = (MessedUpHashCodeEqualsClass) copyCb.getRoot();
    }

    /**
     * Tests that domain models with messed up {@link Object#equals(Object)} and {@link Object#hashCode()} can be
     * synchronized.
     */
    @Test
    public void testSynchronizeDomainModels() {
        assertTrue(equals(root, copyRoot));

        // set child of root
        root.child.set(new MessedUpHashCodeEqualsClass());
        assertFalse(equals(root, copyRoot));
        copyMeta.execute(cb.getCommands());
        assertTrue(equals(root, copyRoot));

        // set child of child of root
        root.child.get().child.set(new MessedUpHashCodeEqualsClass());
        assertFalse(equals(root, copyRoot));
        copyMeta.execute(cb.getCommands());
        assertTrue(equals(root, copyRoot));

        // set child of root to null
        root.child.set(null);
        assertFalse(equals(root, copyRoot));
        copyMeta.execute(cb.getCommands());
        assertTrue(equals(root, copyRoot));
    }

    /**
     * Since {@link MessedUpHashCodeEqualsClass} has a messed up {@link Object#equals(Object)} method the equality has
     * to be checked in this method.
     * 
     * @param obj1 The first instance to check equality with.
     * @param obj2 The second instance to check equality with.
     * @return True when equal, false otherwise.
     */
    private static boolean equals(final MessedUpHashCodeEqualsClass obj1, final MessedUpHashCodeEqualsClass obj2) {
        if (obj1 == null) {
            if (obj2 == null) {
                return true;
            }
            return false;
        }
        if (obj2 == null) {
            return false;
        }
        return equals(obj1.child.get(), obj2.child.get());
    }

    /**
     * A class with messed up {@link Object#equals(Object)} and {@link Object#hashCode()}. The framework should not fail
     * on synchronizing this class.
     */
    private static final class MessedUpHashCodeEqualsClass {
        private final ObjectProperty<MessedUpHashCodeEqualsClass> child = new SimpleObjectProperty<>();

        public MessedUpHashCodeEqualsClass() {
        }

        @Override
        public boolean equals(final Object obj) {
            // In JavaFX change listener get only activated, if the new value doesn't equal the old value. So there must
            // be at least some possibility that the equals method generates false so that the change listeners get
            // activated at all.
            if (obj == null) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }
}
