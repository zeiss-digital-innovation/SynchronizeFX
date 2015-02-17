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
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

/**
 * A wrapper around either an {@link ReplaceInList} command or an {@link AddToList}.
 */
class ReplaceOrAddInList {

    private final ListCommand command;

    /**
     * Initializes an instance that wraps a {@link ReplaceInList}.
     * 
     * @param command
     *            The command to wrap.
     */
    public ReplaceOrAddInList(final ReplaceInList command) {
        this.command = command;
    }

    /**
     * The {@link ListCommand} that is wrapped.
     * 
     * @return The command
     */
    public ListCommand toSimpleCommand() {
        return command;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
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
        ReplaceOrAddInList other = (ReplaceOrAddInList) obj;
        if (command == null) {
            if (other.command != null) {
                return false;
            }
        } else if (!command.equals(other.command)) {
            return false;
        }
        return true;
    }
}
