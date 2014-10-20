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
 * A value for properties and collections which is either an <em>observable object</em> or a <em>simple object</em>.
 */
public class Value {

    private final UUID observableObjectId;
    private final Object simpleObjectValue;

    /**
     * Initializes an instance that wrappes an <em>observable object</em>.
     * 
     * @param observableObjectID The id of the wrapped <em>observable object</em>.
     */
    public Value(final UUID observableObjectID) {
        this.observableObjectId = observableObjectID;
        this.simpleObjectValue = null;
    }
    
    /**
     * Initializes an instance that wrapps a <em>simple object</em>.
     * 
     * @param simpleObjectValue The simple object that is wrapped.
     */
    public Value(final Object simpleObjectValue) {
        this.observableObjectId = null;
        this.simpleObjectValue = simpleObjectValue;
    }

    /**
     * @return If this instance denotes an <em>observable object</em> this returns the id of this object. If this
     *         instance denotes a <em>simple object</em> this method returns <code>null</code>.
     */
    public UUID getObservableObjectId() {
        return observableObjectId;
    }

    /**
     * @return If this instance denotes a <em>simple object</em> this returns the value object. If this instance
     *         denotes an <em>observable object</em> this method returns <code>null</code>.
     */
    public Object getSimpleObjectValue() {
        return simpleObjectValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((observableObjectId == null) ? 0 : observableObjectId.hashCode());
        result = prime * result + ((simpleObjectValue == null) ? 0 : simpleObjectValue.hashCode());
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
        Value other = (Value) obj;
        if (observableObjectId == null) {
            if (other.observableObjectId != null) {
                return false;
            }
        } else if (!observableObjectId.equals(other.observableObjectId)) {
            return false;
        }
        if (simpleObjectValue == null) {
            if (other.simpleObjectValue != null) {
                return false;
            }
        } else if (!simpleObjectValue.equals(other.simpleObjectValue)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Value [observableObjectId=" + observableObjectId + ", simpleObjectValue=" + simpleObjectValue + "]";
    }
}
