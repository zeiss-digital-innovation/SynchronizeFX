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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import org.junit.Before;
import org.junit.Test;

import de.saxsys.synchronizefx.core.testutils.SaveParameterCallback;

/**
 * Checks that changes that are done while a client is connecting are not lost.
 * 
 * As the user should not be constrained to do only {@code synchronize}d changes on his domain model, it is hard to
 * ensure that the model is kept synchronous while a client is connected. That is because at the time of writing this
 * test, the messages to reproduce the current state of the domain model are produced by walking through the whole
 * domain model via reflection. This messages are than send to a connecting client. When changes on the server side are
 * done while this "walking" process is active to the part which has already been walked through, this changes are lost.
 * The purpose of this test is to reproduce this problem and show if it has been solved or not.
 * 
 */
public class ChangeWhileConnectTest {

    private static final int WAIT_TIMEOUT = 5000;

    private Domain root;
    private SaveParameterCallback cb;
    private MetaModel meta;

    private BlockingIntegerProperty blockingProperty;

    private Object threadWaitMonitor = new Object();
    private boolean propertyVisitorThreadShouldWakeUp;
    private boolean testThreadShouldWakeUp;

    private List<Object> commands;

    /**
     * Initializes an example domain object and the meta model.
     */
    @Before
    public void init() {
        root = new Domain();
        root.waitingProperty.set(6);

        Domain child1 = new Domain();
        //If lost updates occur, this value is not changed on the second client.
        child1.waitingProperty.set(7);
        root.list.add(child1);
        
        Domain child2 = new Domain();
        blockingProperty = new BlockingIntegerProperty();
        blockingProperty.waitAfterInvocationCount = -1;
        child2.waitingProperty = blockingProperty;
        child2.waitingProperty.set(5);
        root.list.add(child2);

        cb = new SaveParameterCallback();
        meta = new MetaModel(cb, root);
    }

    /**
     * Changes the domain model while it is walked through and checks that everything is synchronized.
     */
    @Test()
    public void testChangeWhileWalking() {
        // simulate the situation in which updates can be lost
        blockingProperty.waitAfterInvocationCount = 0;
        propertyVisitorThreadShouldWakeUp = false;
        testThreadShouldWakeUp = false;
        
        Domain child1 = root.list.get(0);

        Thread propertyVisitorThread = new Thread() {
            @Override
            public void run() {
                commands = meta.commandsForDomainModel();
            }
        };
        synchronized (threadWaitMonitor) {
            propertyVisitorThread.start();
            while (!testThreadShouldWakeUp) {
                try {
                    long time = System.currentTimeMillis();
                    threadWaitMonitor.wait(WAIT_TIMEOUT);
                    if (time + WAIT_TIMEOUT < System.currentTimeMillis()) {
                        fail("Test was not woken up by the PropertyVisitor thread as it was expected "
                                + "but by a timout.");
                        break;
                    }
                } catch (InterruptedException e) {
                    fail("Test was not woken up by the PropertyVisitor thread as it was expected "
                            + "but by an interuption.");
                }
            }
        }

        //this update may is lost
        child1.waitingProperty.set(9);
        synchronized (threadWaitMonitor) {
            propertyVisitorThreadShouldWakeUp = true;
            threadWaitMonitor.notify();
        }
        try {
            propertyVisitorThread.join();
        } catch (InterruptedException e) {
            fail("Could not wait for the PropertyVisitorThread to finish.");
        }

        // check if updates are lost
        SaveParameterCallback copyCb = new SaveParameterCallback();
        MetaModel copyMeta = new MetaModel(copyCb);
        copyMeta.execute(commands);
        assertEquals(root, copyCb.getRoot());
        assertNull(cb.getCommands());
    }

    /**
     * A test domain model which can block the {@link PropertyVisitor} thread.
     */
    private static class Domain {
        final ListProperty<Domain> list = new SimpleListProperty<>(FXCollections.<Domain> observableArrayList());
        IntegerProperty waitingProperty = new SimpleIntegerProperty();

        public Domain() {

        }

        @Override
        public String toString() {
            return "Domain [list=" + list + ", waitingProperty=" + waitingProperty + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((list == null) ? 0 : list.hashCode());
            result = prime * result + ((waitingProperty == null) ? 0 : waitingProperty.hashCode());
            return result;
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
            Domain other = (Domain) obj;
            if (list == null) {
                if (other.list.get() != null) {
                    return false;
                }
            } else if (!list.get().equals(other.list.get())) {
                return false;
            }

            if (waitingProperty.get() != other.waitingProperty.get()) {
                return false;
            }
            return true;
        }
    }

    /**
     * Blocks a the executing thread after {@link ChangeWhileConnectTest#waitAfterInvocationCount} reaches 0.
     */
    private class BlockingIntegerProperty extends SimpleIntegerProperty {
        private int waitAfterInvocationCount;

        /**
         * Blocks the thread that is executing this method when {@link ChangeWhileConnectTest#waitAfterInvocationCount}
         * reaches 0.
         * 
         * Each invocation decreases {@link ChangeWhileConnectTest#waitAfterInvocationCount} by 1.
         */
        public Integer getValue() {
            if (waitAfterInvocationCount == 0) {
                synchronized (threadWaitMonitor) {
                    testThreadShouldWakeUp = true;
                    threadWaitMonitor.notify();
                    while (!propertyVisitorThreadShouldWakeUp) {
                        try {
                            long time = System.currentTimeMillis();
                            threadWaitMonitor.wait(WAIT_TIMEOUT);
                            if (time + WAIT_TIMEOUT < System.currentTimeMillis()) {
                                fail("Test was not woken up by the PropertyVisitor thread as it was expected "
                                        + "but by a timout.");
                                break;
                            }
                        } catch (InterruptedException e) {
                            fail("PropertyVisitor was not woken up by the test thread as expected"
                                    + "but by an interuption.");
                            break;
                        }
                    }
                }
            }
            waitAfterInvocationCount--;
            return super.getValue();
        }
    };
}
