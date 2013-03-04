/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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
 * This interface is used to inform the user about different events like errors that occurred in the library.
 * 
 * @author raik.bieniek
 * 
 */
public interface UserCallbackClient {

    /**
     * Called when the initial transfer of the model has completed.
     * 
     * @param model The root object of the synchronized object tree.
     */
    void modelReady(Object model);

    /**
     * Called when an error occurred in the synchronization code.
     * 
     * This includes errors occurred in the {@link MessageTransferClient} implementation.
     * 
     * @param error the exception that describes the error.
     */
    void onError(SynchronizeFXException error);
}
