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

import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;

/**
 * Optional Steps for the server and the final build step.
 */
public interface OptionalServerStep extends OptionalStep<OptionalServerStep> {

    /**
     * Creates a server instance for serving a domain model.
     * 
     * The returned server is not automatically started yet. You have call You have to call
     * {@link SynchronizeFxServer#start()} to actually start it.
     * 
     * @return The new server instance.
     */
    SynchronizeFxServer build();
}
