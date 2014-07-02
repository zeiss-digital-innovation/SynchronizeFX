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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A command to create a new instance of an observable object.
 * 
 * @author raik.bieniek
 */
public class CreateObservableObject {
    private UUID objectId;
    private String className;
    private Map<String, UUID> propertyNameToId = new HashMap<>();

    /**
     * @return The id this observable object gets.
     */
    public UUID getObjectId() {
        return objectId;
    }

    /**
     * @see CreateObservableObject#getObjectId()
     * @param objectId the id
     */
    public void setObjectId(final UUID objectId) {
        this.objectId = objectId;
    }

    /**
     * @return The class name of the type of this observable object.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @see CreateObservableObject#getClassName()
     * @param className the class name
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * @return A mapping of the names of property fields to the id of the property object.
     */
    public Map<String, UUID> getPropertyNameToId() {
        return propertyNameToId;
    }

    /**
     * @see CreateObservableObject#getPropertyNameToId()
     * @param propertyNameToId the new name-object mapping
     */
    public void setPropertyNameToId(final Map<String, UUID> propertyNameToId) {
        this.propertyNameToId = propertyNameToId;
    }

    @Override
    public String toString() {
        return "CreateObservableObject [objectId=" + objectId + ", className=" + className + ", propertyNameToId="
                + propertyNameToId + "]";
    }
}
