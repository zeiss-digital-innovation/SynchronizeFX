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

package de.saxsys.synchronizefx.core.metamodel;

import java.util.List;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;

/**
 * This is an callback interface for the meta model.
 * 
 * The meta model uses it to inform the underlying layer on several events like new commands that have to be transfered
 * to other peers or that an error occurred in the meta model layer.
 * 
 * @author Raik Bieniek
 */
public interface TopologyLayerCallback {

    /**
     * Called when the meta model layer has produced commands that need to be shared with other meta models.
     * 
     * <p>
     * This method should not block as this may could block the GUI thread. The order of the commands passed in
     * sequential calls to this methods however needs to be retained.
     * </p>
     * 
     * <p>
     * When you've called {@link MetaModel#commandsForDomainModel(CommandsForDomainModelCallback)} to get the initial
     * domain model for a new peer, you don't need to send the commands from this method to this peer as long as your
     * callback {@link CommandsForDomainModelCallback#commandsReady(List)} has not been called. They are incorporated
     * automatically.
     * </p>
     * 
     * @param commands
     *            The commands that need to be shared.
     */
    void sendCommands(List<Command> commands);

    /**
     * Called when the domain model has changed.
     * 
     * This method is most probably called only once. That is when the initial domain model has been transfered
     * completely to a peer.
     * 
     * @param root
     *            The root object of the new domain model.
     */
    void domainModelChanged(Object root);

    /**
     * Called when an error occurred in the meta model layer.
     * 
     * If an error occurred, synchronicity between clients can no longer be guaranteed. In some cases the meta model can
     * still be used and the error be ignored.
     * 
     * @param error
     *            The exception that caused the error.
     */
    void onError(SynchronizeFXException error);
}
