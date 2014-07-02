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
 * A command to set a value for some key in some map.
 * 
 * @author raik.bieniek
 */
public class PutToMap implements Command {
    
    private UUID mapId;
    private Value key;
    private Value value;

    /**
     * @return the id of the map where a value for a key should be put.
     */
    public UUID getMapId() {
        return mapId;
    }

    /**
     * @see PutToMap#getMapId()
     * @param mapId
     *            the id
     */
    public void setMapId(final UUID mapId) {
        this.mapId = mapId;
    }

    /**
     * @return The key of the mapping to add to the map.
     */
    public Value getKey() {
        return key;
    }

    /**
     * @see #getKey()
     * @param key
     *            The key
     */
    public void setKey(final Value key) {
        this.key = key;
    }

    /**
     * @return The value of the mapping to add.
     */
    public Value getValue() {
        return value;
    }

    /**
     * @see #getValue()
     * @param value
     *            The value
     */
    public void setValue(final Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PutToMap [mapId=" + mapId + ", key=" + key + ", value=" + value + "]";
    }
}
