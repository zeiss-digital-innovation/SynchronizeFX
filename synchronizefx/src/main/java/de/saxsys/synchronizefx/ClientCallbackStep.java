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

package de.saxsys.synchronizefx;

import de.saxsys.synchronizefx.core.clientserver.ClientCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;

/**
 * Mandatory step to set the callback for the client.
 */
public interface ClientCallbackStep {

    /**
     * @param callback As the SynchronizeFx framework works asynchronously, you must provide this callback instance
     *            for the framework to be able to inform you when the initial transfer of the domain model from the
     *            server has completed and of errors that have occurred. The methods in the callback are not called
     *            before you call {@link SynchronizeFxClient#connect()}.
     * @return The builder to provide a fluent API.
     */
    OptionalClientStep callback(ClientCallback callback);
}
