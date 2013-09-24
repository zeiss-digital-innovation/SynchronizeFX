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

import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;

/**
 * Optional Steps for the client and the final build step.
 */
public interface OptionalClientStep extends OptionalStep<OptionalClientStep> {
    
    /**
     * Creates a client instance to request a domain model from a server.
     * 
     * The returned client does not automatically connect. You have to call {@link SynchronizeFxClient#connect()} to
     * do so.
     * 
     * @return The new client instance.
     */
    SynchronizeFxClient build();
}
