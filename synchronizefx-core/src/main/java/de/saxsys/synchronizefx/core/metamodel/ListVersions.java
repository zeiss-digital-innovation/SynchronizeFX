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

package de.saxsys.synchronizefx.core.metamodel;

import java.util.UUID;

import de.saxsys.synchronizefx.core.exceptions.ObjectToIdMappingException;

/**
 * Stores the version of the current state of local list properties weakly.
 * 
 * <p>
 * Versions of lists are just random {@link UUID}s. When the state of a list is changed it gets a new random
 * {@link UUID} as version.
 * </p>
 * <p>
 * Weakly means, that when the garbage collector decides to collect a list properties its version assignment in this
 * class may be collected as well.
 * </p>
 * 
 * 
 * @author Raik Bieniek
 */
public class ListVersions {

    /**
     * Returns the version a list with a given id currently has.
     * 
     * @param listId
     *            The list thats version should be returned.
     * @return The version of the list.
     * @throws ObjectToIdMappingException
     *             When the version for the list with the passed id is unknown.
     */
    public UUID getVersionOrFail(final UUID listId) throws ObjectToIdMappingException {
        throw new UnsupportedOperationException("not implemented yet");
    }

}
