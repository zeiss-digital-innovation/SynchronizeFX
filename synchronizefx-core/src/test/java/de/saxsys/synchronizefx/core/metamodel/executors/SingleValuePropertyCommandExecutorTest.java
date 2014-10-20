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

package de.saxsys.synchronizefx.core.metamodel.executors;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Checks if {@link SingleValuePropertyCommandExecutor} works as expected.
 * 
 * @author Raik Bieniek
 */
@Ignore("Not implemented yet")
public class SingleValuePropertyCommandExecutorTest {

    /**
     * When the command log is empty, every incoming command should be executed.
     */
    @Test
    public void shouldExecuteIncommingCommandWhenLogIsEmpty() {
        fail("not implemented yet");
    }

    /**
     * When an incoming command does not equal the first command in the log queue it should not be executed.
     */
    @Test
    public void shouldDropCommandWhenItsNotEqualToFirstCommandInTheLog() {
        fail("not implemented yet");
    }

    /**
     * When an incoming command does equal the first command in the log queue it should not be executed and removed
     * from the queue.
     * 
     * <p>
     * This is tested by checking if its executed when it is received a second time.
     * </p>
     */
    @Test
    public void shouldDropCommandAndRemoveItFromTheLogWhenItIsEqualToTheFirstLogEntry() {
        fail("not implemented yet");
    }
}
