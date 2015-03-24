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

package de.saxsys.synchronizefx.core.metamodel.commands;

import java.util.UUID;

/**
 * Command to remove elements from a list.
 * 
 * @author Raik Bieniek
 */
public class RemoveFromList extends ListCommand {

    private final int startPosition;
    private final int removeCount;

    /**
     * Initializes an instance.
     * 
     * @param listId
     *            see {@link #getListId()}
     * @param listVersionChange
     *            see {@link #getListVersionChange()}
     * @param startPosition
     *            see {@link #getStartPosition()}
     * @param removeCount
     *            see {@link #getRemoveCount()}
     */
    public RemoveFromList(final UUID listId, final ListVersionChange listVersionChange, final int startPosition,
            final int removeCount) {
        super(listId, listVersionChange);
        this.startPosition = startPosition;
        this.removeCount = removeCount;
    }

    /**
     * The index of the first element in the list that should be removed.
     * 
     * @return The index
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * The amount of elements starting from {@link #getStartPosition()} that should be removed.
     * 
     * @return The element amount to delete.
     */
    public int getRemoveCount() {
        return removeCount;
    }

    @Override
    public String toString() {
        return "RemoveFromList [startPosition=" + startPosition + ", removeCount=" + removeCount
                + ", ListVersionChange=" + super.toString() + "]";
    }

}
