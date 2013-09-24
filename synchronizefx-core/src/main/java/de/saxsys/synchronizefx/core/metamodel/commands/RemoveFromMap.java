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
 * A command that indicates that a mapping should be removed from a map.
 * 
 * @author raik.bieniek
 */
public class RemoveFromMap {

    private UUID mapId;
    private UUID keyObservableObjectId;
    private Object keySimpleObjectValue;

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
     * @return the id for the observable object that represents the key for the mapping that should be removed. If this
     *         is null, than the key is a simple object and can be retrieved via
     *         {@link RemoveFromMap#getKeySimpleObjectValue()}.
     */
    public UUID getKeyObservableObjectId() {
        return keyObservableObjectId;
    }

    /**
     * @see RemoveFromMap#getKeyObservableObjectId()
     * @param keyObservableObjectId the id
     */
    public void setKeyObservableObjectId(final UUID keyObservableObjectId) {
        this.keyObservableObjectId = keyObservableObjectId;
    }

    /**
     * @return the object that represents the key for the mapping that should be removed. If this is null than the key
     *         is an observable object whose id can be retrieved via {@link RemoveFromMap#getKeyObservableObjectId()}.
     */
    public Object getKeySimpleObjectValue() {
        return keySimpleObjectValue;
    }

    /**
     * @see RemoveFromMap#getKeySimpleObjectValue()
     * @param keySimpleObjectValue the key
     */
    public void setKeySimpleObjectValue(final Object keySimpleObjectValue) {
        this.keySimpleObjectValue = keySimpleObjectValue;
    }

    @Override
    public String toString() {
        return "RemoveFromMap [mapId=" + mapId + ", keyObservableObjectId=" + keyObservableObjectId
                + ", keySimpleObjectValue=" + keySimpleObjectValue + "]";
    }
}
