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

import de.saxsys.synchronizefx.core.metamodel.commands.Command;

/**
 * A callback that is informed when the commands that where requested via
 * {@link MetaModel#commandsForDomainModel(CommandsForDomainModelCallback)} are ready.
 */
public interface CommandsForDomainModelCallback {

    /**
     * Used to inform when the commands are ready.
     * 
     * Before you return from this method make sure that steps have been taken that ensure that commands passed to you
     * via future calls of {@link TopologyLayerCallback#sendCommands(List)} are also send to the peer that you've
     * requested this initial command set for. {@link TopologyLayerCallback#sendCommands(List)} will not been called as
     * long as you've not returned from this method.
     * 
     * @param commands the commands
     */
    void commandsReady(List<Command> commands);
}
