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
 * @author Raik Bieniek
 */
public class AddToList extends ListCommand {

    private final Value value;
    private final int position;
    private final int newSize;

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
    public AddToList(final UUID listId, final ListVersionChange listVersionChange, final Value value, //
            final int position) {
        this(listId, listVersionChange, value, position, -1);
    }

    /**
     * Initializes an instance with a size value.
     * 
     * @param listId
     *            see {@link #getListId()}
     * @param listVersionChange
     *            see {@link #getListVersionChange()}
     * @param value
     *            see {@link #getValue()}
     * @param position
     *            see {@link #getPosition()}
     * @param newSize
     *            see {@link #getNewSize()}
     * @deprecated since newSize is no longer used in the new self repairing implementation.
     */
    @Deprecated
    public AddToList(final UUID listId, final ListVersionChange listVersionChange, final Value value,
            final int position, final int newSize) {
        super(listId, listVersionChange);
        this.value = value;
        this.position = position;
        this.newSize = newSize;
    }

    /**
     * The index the new element will have in the list when it's added.
     * 
     * <p>
     * The index of all items in the list thats index is greater or equal to the value returned here has to be
     * incremented by 1 to make this index available.
     * </p>
     * 
     * @return The index
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return The value to add to the list.
     */
    public Value getValue() {
        return value;
    }

    /**
     * The new size the list should have after this command has been executed on it.
     * 
     * @deprecated The new self repairing algorithm will not use this information.
     * @return the new size
     */
    @Deprecated
    public int getNewSize() {
        return newSize;
    }

    @Override
    public String toString() {
        return "AddToList [value=" + value + ", position=" + position + ", newSize=" + newSize + ", ListVersionChange="
                + super.toString() + "]";
    }

}
