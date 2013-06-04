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

import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;

/**
 * The initial step to choose whether to create a server or a client instance.
 */
public interface InitialStep {

    /**
     * Creates a Builder to create a {@link SynchronizeFxServer}.
     * 
     * @return The builder to provide a fluent API.
     */
    ServerModelStep server();

    /**
     * Creates a Builder to create a {@link SynchronizeFxClient}.
     * 
     * @return The builder to provide a fluent API.
     */
    ClientAddressStep client();
}
