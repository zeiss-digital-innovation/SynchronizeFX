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
 * An interface for all commands that involve changing a list.
 * 
 * @author michael.thiele
 *
 */
public interface ListCommand extends Command {

    /**
     * @return The id of the list where a elements should be modified.
     */
    UUID getListId();

    /**
     * @see ListCommand#getListId()
     * @param listId the id
     */
    void setListId(final UUID listId);

    /**
     * @return The index the new element will have in the list when it's added / removed / replaced.
     *         <ul>
     *         <li>add: The index of all items in the list thats index is greater or equal to the value returned here
     *         has to be incremented by 1 to make this index available.</li>
     *         <li>remove: The index of all items in the list thats index is greater or equal to the value returned
     *         here has to be decremented by 1 to make this index available.
     *         </ul>
     */
    int getPosition();

    /**
     * @see ListCommand#getPosition()
     * @param position the position
     */
    void setPosition(final int position);
}
