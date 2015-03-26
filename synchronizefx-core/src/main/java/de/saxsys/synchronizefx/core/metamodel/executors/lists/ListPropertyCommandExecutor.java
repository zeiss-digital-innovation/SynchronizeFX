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

package de.saxsys.synchronizefx.core.metamodel.executors.lists;

import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

/**
 * Executes {@link ListCommand}s received from other peers if appropriate.
 * 
 * @author Raik Bieniek
 */
public interface ListPropertyCommandExecutor {

    /**
     * Executes an command that was received from an other peer if appropriate.
     * 
     * @param command
     *            The received command.
     */
    void execute(AddToList command);

    /**
     * Executes an command that was received from an other peer if appropriate.
     * 
     * @param command
     *            The received command.
     */
    void execute(RemoveFromList command);

    /**
     * Executes an command that was received from an other peer if appropriate.
     * 
     * @param command
     *            The received command.
     */
    void execute(ReplaceInList command);
}
