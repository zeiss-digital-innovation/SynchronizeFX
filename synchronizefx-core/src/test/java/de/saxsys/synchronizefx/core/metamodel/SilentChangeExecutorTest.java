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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Checks if {@link SilentChangeExecutor} works as expected.
 */
public class SilentChangeExecutorTest {

    private static final int NEW_VALUE = 42;

    private final Listeners listeners = mock(Listeners.class);
    private final SilentChangeExecutor cut = new SilentChangeExecutor();

    private final DummyObservable dummyObservable = mock(DummyObservable.class);

    /**
     * Sets up the class under test.
     */
    @Before
    public void setupCut() {
        cut.registerListenersToSilence(listeners);
    }
    
    /**
     * The change to the domain model should not be propagated to the synchronizeFX specific listeners that generate
     * change commands.
     */
    @Test
    public void shouldDisableAndReanableChangeNotificationWhenChangingThePropertyValue() {
        cut.execute(dummyObservable, new Runnable() {
            @Override
            public void run() {
                dummyObservable.setValue(NEW_VALUE);
            }
        });

        final InOrder inOrder = inOrder(listeners, dummyObservable);

        inOrder.verify(listeners).disableFor(dummyObservable);
        inOrder.verify(dummyObservable).setValue(NEW_VALUE);
        inOrder.verify(listeners).enableFor(dummyObservable);
    }

    /**
     * A dummy observable that simulates a wrapper for a single changeable value.
     */
    private interface DummyObservable {
        /**
         * Sets the new value of the observable.
         * 
         * @param newValue
         *            The new value.
         */
        void setValue(int newValue);
    }
}
