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
 * A command to remove an element in a set.
 * 
 */
public class RemoveFromSet implements Command {

    private UUID setId;
    private Value value;

    /**
     * @return The id of the set where an element should be added.
     */
    public UUID getSetId() {
        return setId;
    }
    
    /**
     * @see #getSetId()
     * @param setId The id
     */
    public void setSetId(final UUID setId) {
        this.setId = setId;
    }

    /**
     * @return The value that should be removed from the set.
     */
    public Value getValue() {
        return value;
    }

    /**
     * @see #getValue()
     * @param value the value
     */
    public void setValue(final Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "RemoveFromSet [setId=" + setId + ", value=" + value + "]";
    }
}
