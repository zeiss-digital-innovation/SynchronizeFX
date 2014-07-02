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
 * A command that indicates that a mapping should be removed from a map.
 * 
 * @author raik.bieniek
 */
public class RemoveFromMap implements Command {

    private UUID mapId;
    private Value key;

    /**
     * @return the id of the map where a mapping should be removed.
     */
    public UUID getMapId() {
        return mapId;
    }

    /**
     * @see RemoveFromMap#getMapId()
     * @param mapId the id
     */
    public void setMapId(final UUID mapId) {
        this.mapId = mapId;
    }

    /**
     * @return The key of the mapping to remove from this map.
     */
    public Value getKey() {
        return key;
    }

    /**
     * @see #getKey()
     * @param key the key
     */
    public void setKey(final Value key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "RemoveFromMap [mapId=" + mapId + ", key=" + key + "]";
    }
}
