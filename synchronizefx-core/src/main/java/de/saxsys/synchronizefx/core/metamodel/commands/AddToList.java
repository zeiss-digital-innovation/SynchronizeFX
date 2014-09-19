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
 * A command that states that an element should be added to a list.
 * 
 * @author raik.bieniek
 * 
 */
public class AddToList implements ListCommand {

    private UUID listId;
    private Value value;
    private int position;
    private int newSize;

    @Override
    public UUID getListId() {
        return listId;
    }

    @Override
    public void setListId(final UUID listId) {
        this.listId = listId;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(final int position) {
        this.position = position;
    }

    /**
     * @return The value to add to the list.
     */
    public Value getValue() {
        return value;
    }

    /**
     * @see #getValue()
     * @param value the value
     */
    public void setValue(final Value value) {
        this.value = value;
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
        return "AddToList [listId=" + listId + ", value=" + value + ", position=" + position + ", newSize=" + newSize
                + "]";
    }
}
