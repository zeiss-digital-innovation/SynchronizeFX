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

import javafx.beans.property.Property;

/**
 * A command to set a new value for some {@link Property}.
 * 
 * @author Raik Bieniek
 */
public class SetPropertyValue implements Command {

    private final UUID propertyId;
    private final Value value;
    private final UUID commandId;

    /**
     * Creates an instance with a predefined command id.
     * 
     * @param commandId
     *            The unique id of this command.
     * @param propertyId
     *            The id of the property thats value is set.
     * @param value
     *            The value that is set.
     */
    public SetPropertyValue(final UUID commandId, final UUID propertyId, final Value value) {
        this.commandId = commandId;
        this.propertyId = propertyId;
        this.value = value;
    }

    /**
     * Creates an instance with an auto-generated command id.
     * 
     * @param propertyId
     *            The id of the property thats value is set.
     * @param value
     *            The value that is set.
     */
    public SetPropertyValue(final UUID propertyId, final Value value) {
        this.commandId = UUID.randomUUID();
        this.propertyId = propertyId;
        this.value = value;
    }

    /**
     * The unique id of this command.
     * 
     * @return The id
     */
    public UUID getCommandId() {
        return commandId;
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

    /**
     * Two {@link SetPropertyValue} commands are equal when their {@link #getCommandId()}s are equal.
     * 
     * @param obj
     *            The other object to compare the equality with.
     * @return <code>true</code> if this instance is equal to <code>obj</code> and <code>false</code> if not.
     */
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
        if (commandId == null) {
            if (other.commandId != null) {
                return false;
            }
        } else if (!commandId.equals(other.commandId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((commandId == null) ? 0 : commandId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "SetPropertyValue [propertyId=" + propertyId + ", value=" + value + "]";
    }
}
