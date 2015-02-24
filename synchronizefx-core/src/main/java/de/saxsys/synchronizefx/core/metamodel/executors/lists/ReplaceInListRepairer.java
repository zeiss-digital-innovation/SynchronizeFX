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
 * Repairs a remote {@link ReplaceInList} commands in relation to local {@link ListCommand}s and local
 * {@link ReplaceInList} commands in relation to remote {@link ListCommand}s.
 *
 * <p>
 * Repairing a {@link ReplaceInList} command can result in an {@link AddToList} command in the case that the element
 * that should be replaced was removed. Therefore all methods take a {@link ReplaceOrAddInList} command as input instead
 * of a simple {@link ReplaceInList}.
 * </p>
 *
 * @author Raik Bieniek
 */
class ReplaceInListRepairer {

    /**
     * Repairs a {@link ReplaceOrAddInList} in relation to an {@link AddToList} command.
     * 
     * @param toRepair
     *            The command to repair.
     * @param repairAgainst
     *            The command to repair against.
     * @return The repaired command.
     */
    public ReplaceOrAddInList repairCommand(final ReplaceOrAddInList toRepair, final AddToList repairAgainst) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Repairs a {@link ReplaceOrAddInList} in relation to an {@link AddToList} command.
     * 
     * @param toRepair
     *            The command to repair.
     * @param repairAgainst
     *            The command to repair against.
     * @return The repaired command.
     */
    public ReplaceOrAddInList repairCommand(final ReplaceOrAddInList toRepair, final RemoveFromList repairAgainst) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Repairs a {@link ReplaceOrAddInList} in relation to an {@link AddToList} command.
     * 
     * @param toRepair
     *            The command to repair.
     * @param repairAgainst
     *            The command to repair against.
     * @return The repaired command.
     */
    public ReplaceOrAddInList repairCommand(final ReplaceOrAddInList toRepair, final ReplaceInList repairAgainst) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not implemented yet");
    }
}
