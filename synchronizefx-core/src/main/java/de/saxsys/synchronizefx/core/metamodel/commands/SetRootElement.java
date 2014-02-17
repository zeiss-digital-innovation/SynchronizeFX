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
 * A message that tells which object should be treated as the root object of the domain model.
 * 
 * @author raik.bieniek
 * 
 */
public class SetRootElement {
    private UUID rootElementId;

    /**
     * @return The id of the root element.
     */
    public UUID getRootElementId() {
        return rootElementId;
    }

    /**
     * 
     * @see SetRootElement#getRootElementId()
     * @param rootElementId the id
     */
    public void setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
    }

    @Override
    public String toString() {
        return "SetRootElement [rootElementId=" + rootElementId + "]";
    }
}
