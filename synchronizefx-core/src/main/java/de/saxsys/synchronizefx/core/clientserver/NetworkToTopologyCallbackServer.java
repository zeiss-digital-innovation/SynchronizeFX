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

import java.util.List;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;

/**
 * This is an callback interface for the network library to inform the upper layer of incoming events like commands
 * or new clients.
 * 
 * This is the interface for the server side. For the client side use {@link NetworkToTopologyCallbackClient}.
 * 
 * @author Raik Bieniek
 */
public interface NetworkToTopologyCallbackServer {

    /**
     * Commands were received.
     * 
     * @param commands The commands received.
     * @param sender An object that represents the sender of the commands.
     */
    void recive(List<Command> commands, Object sender);

    /**
     * A new client connected.
     * 
     * @param newClient an object that represents the new client.
     */
    void onConnect(Object newClient);

    /**
     * An error in the connection to some client occurred.
     * 
     * When this method is called, the connection to the problematic client has to be already be terminated. The
     * server must still be working. If not use
     * {@link NetworkToTopologyCallbackServer#onFatalError(SynchronizeFXException)} instead.
     * 
     * @param e an exception that describes the problem.
     */
    void onClientConnectionError(SynchronizeFXException e);

    /**
     * A fatal error that made the server shut down.
     * 
     * When this method is called, the server came across an error that made it impossible to continue the normal
     * operation. The connection to all remaining clients must already be closed and the server be shutdown.
     * 
     * This method must only be called after the server successfully started. If an error occurred while trying to
     * start up, throw an exception there.
     * 
     * @param e an exception that describes the problem.
     */
    void onFatalError(SynchronizeFXException e);
}
