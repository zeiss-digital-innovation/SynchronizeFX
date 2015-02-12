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

package de.saxsys.synchronizefx.core.metamodel.executors.lists;

import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

/**
 * Repairs a remote {@link RemoveFromList} commands in relation to local {@link ListCommand}s and local
 * {@link RemoveFromList} commands in relation to remote {@link ListCommand}s.
 *
 * <p>
 * Repairing {@link RemoveFromList} command can result in more complex instructions thats semantics are not covered by
 * simple {@link RemoveFromList} commands. Therefore in this class the extended version {@link RemoveFromListExcept} is
 * used.
 * </p>
 * 
 * @author Raik Bieniek
 */
class RemoveFromListRepairer {

    /**
     * Repairs a local {@link AddToList} command in relation to a remote {@link RemoveFromListExcept} command.
     * 
     * @param localCommand
     *            The local command to repair.
     * @param remoteCommand
     *            The remote command.
     * @return The repaired local command
     */
    public AddToList repairLocalCommand(final AddToList localCommand, final RemoveFromListExcept remoteCommand) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Repairs a local {@link ReplaceInList} command in relation to a remote {@link RemoveFromListExcept} command.
     * 
     * @param localCommand
     *            The local command to repair.
     * @param remoteCommand
     *            The remote command.
     * @return The repaired local command
     */
    public ReplaceInList repairLocalCommand(final ReplaceInList localCommand, //
            final RemoveFromListExcept remoteCommand) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not implemented yet");
    }

}
