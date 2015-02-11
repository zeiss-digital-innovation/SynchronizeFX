/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
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

package de.saxsys.synchronizefx.core.metamodel.executors.lists;

import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;

/**
 * Manages the repairing and executing respectively re-sending of local and remote commands.
 * 
 * <p>
 * Incoming remote commands need to be repaired if other changes where made on the local list. This class initiates
 * repairing of these command before executing them. Remote commands that do not need repair are executed directly.
 * </p>
 * 
 * <p>
 * When a remote command appears while unconfirmed local commands exists, other peers will drop all these local
 * commands. They therefore have to be repaired as well to be based on the list version the remote command produced and
 * re-send to the other peers.
 * </p>
 * 
 * <p>
 * A local command gets confirmed when it is the same as a received remote command and if it is the oldest unconfirmed
 * command.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class ReparingListPropertyCommandExecutor {

    /**
     * Logs a command that was locally generated and send to other peers.
     * 
     * @param localCommand
     *            The command to log
     */
    public void logLocalCommand(final ListCommand localCommand) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Executes a remotely received command, repairs it when necessary and resends repaired versions of local commands
     * that where obsoleted by the received command.
     * 
     * @param command
     *            The command to execute.
     */
    public void execute(final ListCommand command) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not implemented yet");
    }
}
