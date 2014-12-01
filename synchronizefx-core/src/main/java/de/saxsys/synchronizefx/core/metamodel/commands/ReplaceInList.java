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
 * A command to replace elements in a list.
 * 
 * @see java.util.List#set(int, Object)
 * @author michael.thiele
 *
 */
public class ReplaceInList implements ListCommand {

    private UUID listId;
    private int position;
    private Value value;

    @Override
    public UUID getListId() {
        return listId;
    }

    /**
     * @see ListCommand#getListId()
     * @param listId the id
     */
    public void setListId(final UUID listId) {
        this.listId = listId;
    }

    /**
     * The index of the element that should be replaced.
     * 
     * @return the index
     */
    public int getPosition() {
        return position;
    }

    /**
     * @see #getPosition()
     * @param position
     *            the position
     */
    public void setPosition(final int position) {
        this.position = position;
    }

    /**
     * @return The value to set in the specified position in the list.
     */
    public Value getValue() {
        return value;
    }

    /**
     * @see #getValue()
     * @param value
     *            the value
     */
    public void setValue(final Value value) {
        this.value = value;
    }
}
