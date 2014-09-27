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

    private UUID listId;
    private int startPosition;
    private int removeCount;
    private int newSize;

    @Override
    public UUID getListId() {
        return listId;
    }

    @Override
    public void setListId(final UUID listId) {
        this.listId = listId;
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
     * @see #getStartPosition()
     * @param startPosition
     *            the position
     */
    public void setStartPosition(final int startPosition) {
        this.startPosition = startPosition;
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
     * @see #getRemoveCount()
     * @param removeCount
     *            The count
     */
    public void setRemoveCount(final int removeCount) {
        this.removeCount = removeCount;
    }

    /**
     * The new size the list should have after this command has been executed on it.
     * 
     * @return the new size
     */
    public int getNewSize() {
        return newSize;
    }

    /**
     * @see RemoveFromList#getNewSize()
     * @param newSize
     *            the new size
     */
    public void setNewSize(final int newSize) {
        this.newSize = newSize;
    }

    @Override
    public String toString() {
        return "RemoveFromList [listId=" + listId + ", startPosition=" + startPosition + ", removeCount=" + removeCount
                + ", newSize=" + newSize + "]";
    }
}
