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

package de.saxsys.synchronizefx.core.clientserver;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

/**
 * This interface has to be implemented by the server to handle errors.
 * 
 * @author raik.bieniek
 * 
 */
public interface ServerCallback {

    /**
     * Called when an error occurred in the synchronization code.
     * 
     * This includes errors occurred in the {@link MessageTransferServer} implementation.
     * 
     * @param error the exception that describes the error.
     */
    void onError(SynchronizeFXException error);
}
