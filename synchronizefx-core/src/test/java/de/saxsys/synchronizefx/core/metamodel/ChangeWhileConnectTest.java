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

import java.util.ConcurrentModificationException;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import de.saxsys.synchronizefx.core.metamodel.ModelWalkingSynchronizer.ActionType;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.testutils.EasyCommandsForDomainModel;
import de.saxsys.synchronizefx.core.testutils.SaveParameterCallback;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Checks that changes that are done while a client is connecting are not lost.
 * 
 * As the user should not be constrained to do only {@code synchronize}d changes on his domain model, it is hard to
 * ensure that the model is kept synchronous while a client is connected. That is because at the time of writing this
 * test, the commands to reproduce the current state of the domain model are produced by walking through the whole
 * domain model via reflection. This commands are than send to a connecting client. When changes on the server side are
 * done while this "walking" process is active to the part which has already been walked through, this changes are lost.
 * The purpose of this test is to reproduce this problem and show if it has been solved or not.
 * 
 */
@Ignore("Tests fail sporadically")
public class ChangeWhileConnectTest {

    private static final int WAIT_TIMEOUT = 5000;

    private Domain root;
    private SaveParameterCallback cb;
    private MetaModel meta;

    private BlockingIntegerProperty blockingProperty;

    private Object threadWaitMonitor = new Object();
    private boolean propertyVisitorThreadShouldWakeUp;
    private boolean testThreadShouldWakeUp;

    private List<Command> commands;

    /**
     * Initializes an example domain object and the meta model.
     */
    @Before
    public void init() {
        root = new Domain();
        root.waitingProperty.set(6);

        Domain child1 = new Domain();
        // If lost updates occur, this value is not changed on the second client
        // in test case testChangeWhileWalking().
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

        blockingProperty.waitAfterInvocationCount = 0;
        propertyVisitorThreadShouldWakeUp = false;
        testThreadShouldWakeUp = false;
    }

    /**
     * Changes the domain model while it is walked through and checks that everything is synchronized.
     * 
     * <p>
     * Assume the following object graph.
     * </p>
     * 
     * <pre>
     * {@code _
     *    (A)
     *   /   \
     * (B)   (C)}
     * </pre>
     * 
     * <p>
     * Assume that walker is already at C. When changes are done to B the walker won't get them as it already has
     * visited B. This test checks if this changes are lost (which shouldn't happen).
     * </p>
     */
    @Test
    public void testChangeWhileWalking() {
        final Domain child1 = root.list.get(0);

        Thread propertyVisitorThread = startPropertyVisitorThread();

        // this update may is lost
        doInNewThread(new Runnable() {
            @Override
            public void run() {
                child1.waitingProperty.set(9);
            }
        });
        finishPropertyVisitorThread(propertyVisitorThread);

        // check if updates are lost
        executeCommandsAndCompare(null);
    }

    /**
     * Makes a change the domain model (remove) while it is walked through in the part that wasn't visited yet. It then
     * checks that this change is merged correctly so that it won't result in errors.
     * 
     * <p>
     * Assume the following object graph.
     * </p>
     * 
     * <pre>
     * {@code _
     *    (A)
     *   /   \
     * (B)   (C)}
     * </pre>
     * 
     * <p>
     * This test produces a change that removes C while the walker is at A. This change is safed until the walking has
     * finished. The walker did'nt create a command to create C. If the command to remove C would be send to the client,
     * an error would occur that an unknown object should be removed. This test checks if this happens.
     * </p>
     */
    @Test
    public void testRemoveOfObjectThatWasntCreated() {
        final Domain child3 = new Domain();
        root.list.add(child3);

        // simulate the problem
        Thread propertyVisitorThread = startPropertyVisitorThread();
        doInNewThread(new Runnable() {
            @Override
            public void run() {
                root.list.remove(child3);
            }
        });

        finishPropertyVisitorThread(propertyVisitorThread);

        // check if command list can be executed without errors.
        executeCommandsAndCompare(null);
    }

    /**
     * This list ensures that {@link ConcurrentModificationException} thrown by a list iterator in the property walker
     * don't result in incorrect results.
     */
    @Test
    public void testProvokeConcurentModificationExceptionByListIterateors() {
        final Domain child3 = new Domain();
        root.list.add(child3);
        root.list.add(child3);

        // simulate the problem
        Thread propertyVisitorThread = startPropertyVisitorThread();
        doInNewThread(new Runnable() {
            @Override
            public void run() {
                root.list.remove(child3);
            }
        });

        finishPropertyVisitorThread(propertyVisitorThread);

        // check if command list can be executed without errors.
        executeCommandsAndCompare(null);
    }

    /**
     * Tests if changes that occurred after the property walking has finished but before the commands are send are lost.
     */
    @Test
    public void testSynchronizeChangesAfterWalkingBeforeSending() {
        // MetaModel of the setup() method is not useful here.
        root = new Domain();
        root.waitingProperty.set(40);
        meta = new MetaModel(cb, root);

        Thread commandListCreatorThread = new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setName("CommandListCreator Thread");
                meta.commandsForDomainModel(new BlockingCommandsForDomainModelCallback());
            }
        };
        synchronized (threadWaitMonitor) {
            Thread.currentThread().setName("Test Thread");
            commandListCreatorThread.start();
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

        doInNewThread(new Runnable() {
            @Override
            public void run() {
                root.waitingProperty.set(50);
            }
        });

        finishPropertyVisitorThread(commandListCreatorThread);

        // check if command list can be executed without errors.
        executeCommandsAndCompare(null);

    }

    /**
     * Test if incoming changes from other peers result n lost updates.
     */
    @Test
    public void testIncommingChanges() {
        final Domain child1 = root.list.get(0);
        child1.waitingProperty.set(40);
        child1.waitingProperty.set(5781);
        final List<Command> simulatedIncommingChanges = cb.getCommands();
        child1.waitingProperty.set(40);

        // simulate the problem
        Thread propertyVisitorThread = startPropertyVisitorThread();
        doInNewThread(new Runnable() {
            @Override
            public void run() {
                meta.execute(simulatedIncommingChanges);
            }
        });
        // simulated changes should not have been executed yet.
        assertEquals(40, child1.waitingProperty.get());

        finishPropertyVisitorThread(propertyVisitorThread);
        // but they should now.
        assertEquals(5781, child1.waitingProperty.get());

        // check if command list can be executed without errors.
        executeCommandsAndCompare(simulatedIncommingChanges);
    }

    /**
     * Starts the domain model walking in a separate {@link Thread} and blocks the current {@link Thread} until
     * {@link BlockingIntegerProperty} wakes it.
     * 
     * The commands created by this {@link Thread} are safed to {@link ChangeWhileConnectTest#commands}.
     * 
     * @return The thread with the paused property walker.
     */
    private Thread startPropertyVisitorThread() {
        Thread propertyVisitorThread = new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setName("PropertyVisitor Thread");
                commands = EasyCommandsForDomainModel.commandsForDomainModel(meta);
            }
        };
        synchronized (threadWaitMonitor) {
            Thread.currentThread().setName("Test Thread");
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
        return propertyVisitorThread;
    }

    /**
     * Continues the property visitor {@link Thread} (returned by
     * {@link ChangeWhileConnectTest#startPropertyVisitorThread()} and wait for it to finish.
     * 
     * @param propertyVisitorThread
     *            The property visitor {@link Thread}.
     */
    private void finishPropertyVisitorThread(final Thread propertyVisitorThread) {
        synchronized (threadWaitMonitor) {
            propertyVisitorThreadShouldWakeUp = true;
            threadWaitMonitor.notify();
        }
        meta.getModelWalkingSynchronizer().doWhenModelWalkerFinished(ActionType.TEST, new Runnable() {
            @Override
            public void run() {
                // noting to do, just block.
            }
        });
    }

    /**
     * Executes code in a new thread and waits until this new thread goes to sleep.
     * 
     * <p>
     * The test cases in this class block the Property Walker thread until they allow it to continue. The also change
     * property which causes the SynchronizeFX listeners to get invoked. These listeners will block the test thread
     * until the property walking finishes. This results in a dead lock.
     * </p>
     * <p>
     * Therefore this method does the property changing in a new thread and waits for until it goes to sleep. This
     * ensures that the property value has changed befor this method returns.
     * </p>
     * 
     * @param runnable
     *            The code to execute.
     */
    private void doInNewThread(final Runnable runnable) {
        Thread newThread = new Thread(runnable);
        newThread.start();
        while (newThread.getState() != Thread.State.WAITING) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                fail(ChangeWhileConnectTest.class.getName()
                        + "#doInNewThread() was woken up unexpectely by an exception.");
            }
        }
    }

    /**
     * Executes the generated commands and checks if the resulting domain model is identical to the original one.
     * 
     * @param simulatedIncommingChanges
     *            additional commands that should be executed on the copy model. This can be {@code null}.
     */
    private void executeCommandsAndCompare(final List<Command> simulatedIncommingChanges) {
        SaveParameterCallback copyCb = new SaveParameterCallback();
        MetaModel copyMeta = new MetaModel(copyCb);
        copyMeta.execute(commands);
        copyMeta.execute(cb.getCommands());
        if (simulatedIncommingChanges != null) {
            copyMeta.execute(simulatedIncommingChanges);
        }
        assertEquals(root, copyCb.getRoot());
    }

    /**
     * Blocks the user thread and wakes the test thread.
     */
    private void blockUserThread() {
        synchronized (threadWaitMonitor) {
            testThreadShouldWakeUp = true;
            threadWaitMonitor.notify();
            while (!propertyVisitorThreadShouldWakeUp) {
                try {
                    long time = System.currentTimeMillis();
                    threadWaitMonitor.wait(WAIT_TIMEOUT);
                    if (time + WAIT_TIMEOUT < System.currentTimeMillis()) {
                        fail("PropertyVisitor was not woken up by the test thread as expected " + "but by a timout.");
                        break;
                    }
                } catch (InterruptedException e) {
                    fail("PropertyVisitor was not woken up by the test thread as expected " + "but by an interuption.");
                    break;
                }
            }
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
                blockUserThread();
            }
            waitAfterInvocationCount--;
            return super.getValue();
        }
    };

    /**
     * Blocks the thread this class is executed in, when {@link CommandsForDomainModelCallback#sendCommands(List)} is
     * called.
     * 
     */
    private class BlockingCommandsForDomainModelCallback implements CommandsForDomainModelCallback {

        @Override
        public void commandsReady(final List<Command> theCommands) {
            blockUserThread();
            commands = theCommands;
        }
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
}
