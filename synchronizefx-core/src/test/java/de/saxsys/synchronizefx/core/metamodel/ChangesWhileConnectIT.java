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

import org.junit.Ignore;
import org.junit.Test;


/**
 * These tests ensure that the tests in {@link ChangeWhileConnectTest} and the code it tests are reliable when they are
 * run often.
 * 
 * @author Raik Bieniek
 */
@Ignore("Tests fail sporadically")
public class ChangesWhileConnectIT {
    
    /**
     * How often every single test should be executed.
     */
    private static final int EXECUTION_COUNT = 1000;

    /**
     * @see ChangeWhileConnectTest#testChangeWhileWalking().
     */
    @Test
    public void testChangeWhileWalking() {
        for (int i = 0; i < EXECUTION_COUNT; i++) {
            initializedChangeWhileConnectTest().testChangeWhileWalking();
        }
    }

    /**
     * @see ChangeWhileConnectTest#testRemoveOfObjectThatWasntCreated().
     */
    @Test
    public void testRemoveOfObjectThatWasntCreated() {
        for (int i = 0; i < EXECUTION_COUNT; i++) {
            initializedChangeWhileConnectTest().testRemoveOfObjectThatWasntCreated();
        }
    }

    /**
     * @see ChangeWhileConnectTest#testProvokeConcurentModificationExceptionByListIterateors().
     */
    @Test
    public void testProvokeConcurentModificationExceptionByListIterateors() {
        for (int i = 0; i < EXECUTION_COUNT; i++) {
            initializedChangeWhileConnectTest().testProvokeConcurentModificationExceptionByListIterateors();
        }
    }

    /**
     * @see ChangeWhileConnectTest#testSynchronizeChangesAfterWalkingBeforeSending().
     */
    @Test
    public void testSynchronizeChangesAfterWalkingBeforeSending() {
        for (int i = 0; i < EXECUTION_COUNT; i++) {
            initializedChangeWhileConnectTest().testSynchronizeChangesAfterWalkingBeforeSending();
        }
    }

    /**
     * @see ChangeWhileConnectTest#testIncommingChanges().
     */
    @Test
    public void testIncommingChanges() {
        for (int i = 0; i < EXECUTION_COUNT; i++) {
            initializedChangeWhileConnectTest().testIncommingChanges();
        }
    }

    private ChangeWhileConnectTest initializedChangeWhileConnectTest() {
        ChangeWhileConnectTest test = new ChangeWhileConnectTest();
        test.init();
        return test;
    }
}
