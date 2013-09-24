/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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
 * A command to remove an element in a set.
 * 
 */
public class RemoveFromSet {

    private UUID setId;
    private UUID observableObjectId;
    private Object simpleObjectValue;

    /**
     * @return The id of the set where an element should be added.
     */
    public UUID getListId() {
        return setId;
    }
    
    /**
     * @see RemoveFromSet#getListId()
     * @param setId The id
     */
    public void setSetId(final UUID setId) {
        this.setId = setId;
    }
    
    /**
     * @return The id of the observable object that should be removed from the set. If this is null, than the value is a
     *         simple object an can be retrieved through through {@link RemoveFromSet#getSimpleObjectValue()}.
     */
    public UUID getObservableObjectId() {
        return observableObjectId;
    }

    /**
     * @see RemoveFromSet#getObservableObjectId()
     * @param id The id
     */
    public void setObservableObjectId(final UUID id) {
        this.observableObjectId = id;
    }
    
    /**
     * @return The simple object that should be removed from the set. The returned value is only valid if
     *         {@link RemoveFromSet#getObservableObjectId()} returns null.
     */
    public Object getSimpleObjectValue() {
        return simpleObjectValue;
    }
    
    /**
     * @see RemoveFromSet#getSimpleObjectValue()
     * @param value The value
     */
    public void setSimpleObjectValue(final Object value) {
        this.simpleObjectValue = value;
    }

    @Override
    public String toString() {
        return "RemoveFromSet [setId=" + setId + ", observableObjectId=" + observableObjectId + ", simpleObjectValue="
                + simpleObjectValue + "]";
    }
}
