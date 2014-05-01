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

package de.saxsys.synchronizefx.core.exceptions;

/**
 * Indicates an error with the <em>observable object</em> to id mapping.
 */
public class ObjectToIdMappingException extends SynchronizeFXException {

    private static final long serialVersionUID = 3997295840965130660L;

    /**
     * 
     * @param message
     *            A user readable message that describes the problem and optionally some advice for the user how the
     *            problem can be fixed.
     */
    public ObjectToIdMappingException(final String message) {
        super(message);
    }

}
