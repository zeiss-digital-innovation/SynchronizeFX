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
 * @author Raik Bieniek
 */
public class SetPropertyValue implements Command {

    private final UUID propertyId;
    private final Value value;

    /**
     * Initializes an instance.
     * 
     * @param propertyId The id of the property thats value is set.
     * @param value The value that is set.
     */
    public SetPropertyValue(final UUID propertyId, final Value value) {
        this.propertyId = propertyId;
        this.value = value;
    }

    /**
     * @return The value to set for the property.
     */
    public Value getValue() {
        return this.value;
    }

    /**
     * @return The id of the property that's value should be set.
     */
    public UUID getPropertyId() {
        return propertyId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((propertyId == null) ? 0 : propertyId.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SetPropertyValue other = (SetPropertyValue) obj;
        if (propertyId == null) {
            if (other.propertyId != null) {
                return false;
            }
        } else if (!propertyId.equals(other.propertyId)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SetPropertyValue [propertyId=" + propertyId + ", value=" + value + "]";
    }
}
