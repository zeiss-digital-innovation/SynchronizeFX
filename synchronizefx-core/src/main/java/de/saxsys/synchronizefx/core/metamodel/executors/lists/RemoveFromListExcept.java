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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;

/**
 * A command for removing all elements in a list within a given range except some explicitly stated elements.
 * 
 * @author Raik Bieniek
 */
class RemoveFromListExcept {

    private final RemoveFromList removeFromListCommand;
    private final Set<Integer> exceptedIndices = new HashSet<>();

    /**
     * Initializes an instance with an original command to wrap.
     * 
     * @param removeFromListCommand
     *            see {@link #getRemoveFromListCommand()}
     */
    public RemoveFromListExcept(final RemoveFromList removeFromListCommand) {
        this.removeFromListCommand = removeFromListCommand;
    }

    /**
     * The original remove command from which elements should be excluded.
     * 
     * @return the base command.
     */
    public RemoveFromList getRemoveFromListCommand() {
        return removeFromListCommand;
    }

    /**
     * All indices within the range of the {@link RemoveFromList} command that should not be removed.
     * 
     * @return The indices
     */
    public Iterable<Integer> getExceptedIndices() {
        return exceptedIndices;
    }

    /**
     * Adds an index to the list of indices that should not be removed.
     * 
     * @param index
     *            An index that should not be removed.
     */
    public void addExceptedIndice(final int index) {
        exceptedIndices.add(index);
    }

    /**
     * Converts this complex command to simple {@link RemoveFromList}.
     * 
     * @return All {@link RemoveFromList} commands that are necessary to reassemble the semantic of this complex
     *         command.
     */
    public List<RemoveFromList> toSimpleCommands() {
        // TODO real implementation
        return Arrays.asList(removeFromListCommand);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((exceptedIndices == null) ? 0 : exceptedIndices.hashCode());
        result = prime * result + ((removeFromListCommand == null) ? 0 : removeFromListCommand.hashCode());
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
        RemoveFromListExcept other = (RemoveFromListExcept) obj;
        if (exceptedIndices == null) {
            if (other.exceptedIndices != null) {
                return false;
            }
        } else if (!exceptedIndices.equals(other.exceptedIndices)) {
            return false;
        }
        if (removeFromListCommand == null) {
            if (other.removeFromListCommand != null) {
                return false;
            }
        } else if (!removeFromListCommand.equals(other.removeFromListCommand)) {
            return false;
        }
        return true;
    }
}
