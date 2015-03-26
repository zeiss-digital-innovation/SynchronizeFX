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

package de.saxsys.synchronizefx.core.metamodel.executors;

import java.util.List;

import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.ReparingListPropertyCommandExecutor;

/**
 * Logs commands that where generated and send from the local peer to the server to allow model repairing based on the
 * servers command stream.
 * 
 * <p>
 * This class dispatches the local commands to the different executors which are interested in them.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class CommandLogDispatcher {

    private final RepairingSingleValuePropertyCommandExecutor singleValue;
    private final ReparingListPropertyCommandExecutor lists;

    /**
     * Initializes this dispatcher with all executers that are interested in commands.
     * 
     * @param singleValue
     *            The executor for single-value-property change commands.
     * @param lists
     *            The executor for list property commands.
     */
    public CommandLogDispatcher(final RepairingSingleValuePropertyCommandExecutor singleValue,
            final ReparingListPropertyCommandExecutor lists) {
        this.singleValue = singleValue;
        this.lists = lists;
    }

    /**
     * Creates an instance that dispatches no commands as no executors are interested in them.
     */
    public CommandLogDispatcher() {
        this.singleValue = null;
        this.lists = null;
    }

    /**
     * Logs a list of locally generated commands that is sent to the server.
     * 
     * @param commands
     *            The commands that where send.
     */
    public void logLocalCommands(final List<Command> commands) {
        if (singleValue == null) {
            return;
        }
        for (final Command command : commands) {
            if (command instanceof SetPropertyValue) {
                singleValue.logLocalCommand((SetPropertyValue) command);
            } else if (command instanceof ListCommand) {
                lists.logLocalCommand((ListCommand) command);
            }
        }
    }
}
