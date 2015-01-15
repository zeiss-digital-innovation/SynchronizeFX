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

package de.saxsys.synchronizefx.core.metamodel.commands;

import java.util.UUID;

/**
 * Command to remove elements from a list.
 * 
 * @author Raik Bieniek
 */
public class RemoveFromList implements ListCommand {

    private final UUID listId;
    private final int startPosition;
    private final int removeCount;
    private final int newSize;

    /**
     * Initializes an instance.
     * 
     * @param listId
     *            see {@link #getListId()}
     * @param startPosition
     *            see {@link #getStartPosition()}
     * @param removeCount
     *            see {@link #getRemoveCount()}
     */
    public RemoveFromList(final UUID listId, final int startPosition, final int removeCount) {
        this(listId, startPosition, removeCount, 0);
    }

    /**
     * Initializes an instance.
     * 
     * @param listId
     *            see {@link #getListId()}
     * @param startPosition
     *            see {@link #getStartPosition()}
     * @param removeCount
     *            see {@link #getRemoveCount()}
     * @param newSize
     *            see {@link #getNewSize()}
     * @deprecated since newSize is no longer used in the new self repairing implementation.
     */
    @Deprecated
    public RemoveFromList(final UUID listId, final int startPosition, final int removeCount, final int newSize) {
        this.listId = listId;
        this.startPosition = startPosition;
        this.removeCount = removeCount;
        this.newSize = newSize;
    }

    @Override
    public UUID getListId() {
        return listId;
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

    /**
     * The new size the list should have after this command has been executed on it.
     * 
     * @return the new size
     */
    public int getNewSize() {
        return newSize;
    }

    @Override
    public String toString() {
        return "RemoveFromList [listId=" + listId + ", startPosition=" + startPosition + ", removeCount=" + removeCount
                + "]";
    }
}
