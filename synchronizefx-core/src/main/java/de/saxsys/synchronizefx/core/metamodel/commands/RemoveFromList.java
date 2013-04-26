/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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
 * Command to remove an element in a list.
 * 
 * @author raik.bieniek
 * 
 */
public class RemoveFromList {
    private UUID listId;
    private int position;
    private int newSize;

    /**
     * @return The id of the list where an element should be removed.
     */
    public UUID getListId() {
        return listId;
    }

    /**
     * 
     * @see RemoveFromList#getListId()
     * @param listId the id
     */
    public void setListId(final UUID listId) {
        this.listId = listId;
    }

    /**
     * @return The index of the element that should be removed in the list.
     */
    public int getPosition() {
        return position;
    }

    /**
     * 
     * @see RemoveFromList#getPosition()
     * @param position the position
     */
    public void setPosition(final int position) {
        this.position = position;
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
     * @param newSize the new size
     */
    public void setNewSize(final int newSize) {
        this.newSize = newSize;
    }

    @Override
    public String toString() {
        return "RemoveFromList [listId=" + listId + ", position=" + position + "]";
    }

}
