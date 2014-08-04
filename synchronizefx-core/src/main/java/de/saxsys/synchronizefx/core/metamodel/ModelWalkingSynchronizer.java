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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to synchronize {@link Thread}s that are waiting for an active {@link PropertyVisitor} to
 * finish.
 * 
 * <p>
 * An active {@link PropertyVisitor} is called "model walker", as it walkes through the whole domain model of the user
 * via reflection.
 * </p>
 * 
 * @author Raik Bieniek
 */
class ModelWalkingSynchronizer {

    /**
     * The type of an action that should be executed when the model walking has finished.
     * 
     * <p>
     * {@link Thread}s that need to wait for the model walking process to finish need to call
     * {@link ModelWalkingSynchronizer#waitUntilModelWalkerFinishes(ActionType)}. When the model walker finishes these
     * threads are executed in the order of this {@link Enum}.
     * </p>
     * 
     * <p>
     * Multiple {@link Thread}s with that registered with the same {@link ActionType} are woken up in no particular
     * order.
     * </p>
     */
    public enum ActionType {
        /**
         * Changes that where done on this side of the users domain model.
         */
        LOCAL_PROPERTY_CHANGES,
        /**
         * Incoming commands from other peers that need to be executed.
         */
        INCOMMING_COMMANDS,
        /**
         * A model walking process.
         */
        MODEL_WALKNG,
        /**
         * Tests that wait for the model walking to finish.
         */
        TEST
    }

    private static final Logger LOG = LoggerFactory.getLogger(ModelWalkingSynchronizer.class);

    private final Lock memberLock = new ReentrantLock();
    private final List<Set<CountDownLatch>> actionLocks
        = new ArrayList<Set<CountDownLatch>>(ActionType.values().length);

    private boolean modelWalkingInProgess;

    /**
     * Initializes this synchronizer.
     */
    public ModelWalkingSynchronizer() {
        for (int i = 0; i < ActionType.values().length; i++) {
            actionLocks.add(new HashSet<CountDownLatch>());
        }
    }

    /**
     * Informs this synchronizer, that a new model walking process has started.
     * 
     * <p>
     * If an other model walking process is currently in progress, this blocks until it has finished.
     * </p>
     */
    public void startModelWalking() {
        doWhenModelWalkerFinished(ActionType.MODEL_WALKNG, new Runnable() {
            @Override
            public void run() {
                memberLock.lock();
                modelWalkingInProgess = true;
                memberLock.unlock();
            }
        });
    }

    /**
     * Finishes a previously started model walking process.
     */
    public void finishedModelWalking() {
        runNext();
    }

    /**
     * Lets the current thread sleep until a currently running model walking thread has finished.
     * 
     * <p>
     * If no model walking is currently in progress, nothing happens.
     * </p>
     * 
     * @param type
     *            The type of the action that waits for model walking to finish.
     * @param action
     *            The action that should be performed.
     */
    public void doWhenModelWalkerFinished(final ActionType type, final Runnable action) {
        memberLock.lock();
        if (!modelWalkingInProgess) {
            memberLock.unlock();
            action.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Set<CountDownLatch> latches = getLocksForAction(type);
        latches.add(latch);
        // FIXME There may be a race condition between the following unlock and lock.
        memberLock.unlock();
        awaitUninterupptetly(latch);

        action.run();

        memberLock.lock();
        latches.remove(latch);
        runNext();
        memberLock.unlock();
    }

    private void awaitUninterupptetly(final CountDownLatch latch) {
        while (latch.getCount() > 0) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                LOG.warn("Waiting for model walking to finish was interuppted.");
            }
        }
    }

    private void runNext() {
        int actionTypeCount = ActionType.values().length;
        for (int nextType = 0; nextType < actionTypeCount; nextType++) {
            Set<CountDownLatch> nextLocks = actionLocks.get(nextType);
            if (!nextLocks.isEmpty()) {
                CountDownLatch next = nextLocks.iterator().next();
                // FIXME If there is a race condition in the FIX_ME above, check if next.getCount > 0 here and sleep.
                next.countDown();
                return;
            }
        }
        memberLock.lock();
        if (modelWalkingInProgess) {
            modelWalkingInProgess = false;
        }
        memberLock.unlock();
    }

    private Set<CountDownLatch> getLocksForAction(final ActionType action) {
        return actionLocks.get(action.ordinal());
    }
}
