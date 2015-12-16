/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
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
     * Called when a fatal error occurred in the synchronization code.
     * 
     * <p>
     * This includes errors occurred in the {@link CommandTransferServer} implementation.
     * </p>
     * 
     * @param error the exception that describes the error.
     */
    void onError(SynchronizeFXException error);

    /**
     * Called when an error with the connection to a client occurred.
     * 
     * <p>
     * When this event occurs, the server will still work. Just the connection to the affected client is broken.
     * </p>
     * 
     * @param client The client for which an error occurred. The type of this object depends on the network layer
     *            that is used.
     * @param error The error that occurred.
     */
    void onClientConnectionError(Object client, SynchronizeFXException error);
}
