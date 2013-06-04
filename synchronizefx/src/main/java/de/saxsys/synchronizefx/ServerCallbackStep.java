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

package de.saxsys.synchronizefx;

import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;

/**
 * Mandatory Step to set the callback for the server.
 */
public interface ServerCallbackStep {

    /**
     * @param callback As the SynchronizeFx framework works asynchronously, you must provide this callback instance
     *            for the framework to be able to inform you of errors than occurred. The methods in the callback are
     *            not called before you call {@link SynchronizeFxServer#start()}.
     * @return The builder to provide a fluent API.
     */
    OptionalServerStep callback(ServerCallback callback);
}
