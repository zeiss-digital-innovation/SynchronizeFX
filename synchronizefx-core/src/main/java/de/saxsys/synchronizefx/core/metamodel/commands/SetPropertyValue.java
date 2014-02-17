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

import javafx.beans.property.Property;

/**
 * A command to set a new value for some {@link Property}.
 * 
 * @author raik.bieniek
 * 
 */
public class SetPropertyValue {
    private UUID propertyId;

    private UUID observableObjectId;
    private Object simpleObjectValue;

    /**
     * @return The id of the property that's value should be set.
     */
    public UUID getPropertyId() {
        return propertyId;
    }

    /**
     * @see SetPropertyValue#getPropertyId()
     * @param propertyId the id
     */
    public void setPropertyId(final UUID propertyId) {
        this.propertyId = propertyId;
    }

    /**
     * @return The id of the observable object that should be set as value of the property. If this is null, than the
     *         value is a simple object and can be retrieved via {@link #getSimpleObjectValue()}.
     */
    public UUID getObservableObjectId() {
        return observableObjectId;
    }

    /**
     * 
     * @see SetPropertyValue#getObservableObjectId()
     * @param observableObjectId the id
     */
    public void setObservableObjectId(final UUID observableObjectId) {
        this.observableObjectId = observableObjectId;
    }

    /**
     * @return The simple object that should be set as value of the property. The returned value is only in the case the
     *         valid value, if {@link #getObservableObjectId()} returns null.
     */
    public Object getSimpleObjectValue() {
        return simpleObjectValue;
    }

    /**
     * 
     * @see SetPropertyValue#getSimpleObjectValue()
     * @param simpleObjectValue the value
     */
    public void setSimpleObjectValue(final Object simpleObjectValue) {
        this.simpleObjectValue = simpleObjectValue;
    }

    @Override
    public String toString() {
        return "SetPropertyValue [propertyId=" + propertyId + ", observableObjectId=" + observableObjectId
                + ", simpleObjectValue=" + simpleObjectValue + "]";
    }
}
