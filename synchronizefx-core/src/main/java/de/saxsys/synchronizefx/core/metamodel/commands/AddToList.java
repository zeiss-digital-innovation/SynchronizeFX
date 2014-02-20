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
public class AddToList {

    private UUID listId;
    private UUID observableObjectId;
    private Object simpleObjectValue;
    private int position;
    private int newSize;

    /**
     * @return The id of the list where an element should be added.
     */
    public UUID getListId() {
        return listId;
    }

    /**
     * @see AddToList#getListId()
     * @param listId the id
     */
    public void setListId(final UUID listId) {
        this.listId = listId;
    }

    /**
     * @return The id of the observable object that should be added to the list. If this is null, than the value is a
     *         simple object an can be retrieved through through {@link #getSimpleObjectValue()}.
     */
    public UUID getObservableObjectId() {
        return observableObjectId;
    }

    /**
     * @see AddToList#getObservableObjectId()
     * @param observableObjectId the id
     */
    public void setObservableObjectId(final UUID observableObjectId) {
        this.observableObjectId = observableObjectId;
    }

    /**
     * @return The simple object that should be added to the list. The returned value is only valid if
     *         {@link #getObservableObjectId()} returns null.
     */
    public Object getSimpleObjectValue() {
        return simpleObjectValue;
    }

    /**
     * @see AddToList#getSimpleObjectValue()
     * @param simpleObjectValue the value
     */
    public void setSimpleObjectValue(final Object simpleObjectValue) {
        this.simpleObjectValue = simpleObjectValue;
    }

    /**
     * @return The index the new element will have in the list when it's added. The index of all items in the list thats
     *         index is greater or equal to the value returned here has to be incremented by 1 to make this index
     *         available.
     */
    public int getPosition() {
        return position;
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
    
    /**
     * @see AddToList#getPosition()
     * @param position the position
     */
    public void setPosition(final int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "AddToList [listId=" + listId + ", observableObjectId=" + observableObjectId + ", simpleObjectValue="
                + simpleObjectValue + ", position=" + position + "]";
    }
}
