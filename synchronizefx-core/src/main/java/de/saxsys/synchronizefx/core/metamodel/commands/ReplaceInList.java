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
 * A command to replace elements in a list.
 * 
 * @see java.util.List#set(int, Object)
 * @author michael.thiele
 */
public class ReplaceInList extends ListCommand {

    private final int position;
    private final Value value;

    /**
     * Initializes an instance.
     * 
     * @param listId
     *            see {@link #getListId()}
     * @param listVersionChange
     *            see {@link #getListVersionChange()}
     * @param value
     *            see {@link #getValue()}
     * @param position
     *            see {@link #getPosition()}
     */
    public ReplaceInList(final UUID listId, final ListVersionChange listVersionChange, final Value value,
            final int position) {
        super(listId, listVersionChange);
        this.value = value;
        this.position = position;
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
     * @return The value to set in the specified position in the list.
     */
    public Value getValue() {
        return value;
    }
}
