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

package de.saxsys.synchronizefx.core.metamodel.executors.lists;

import java.util.List;
import java.util.Queue;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.ListPropertyMetaDataStore;
import de.saxsys.synchronizefx.core.metamodel.ListPropertyMetaDataStore.ListPropertyMetaData;
import de.saxsys.synchronizefx.core.metamodel.TopologyLayerCallback;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

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

    private final ListPropertyMetaDataStore listMetaDataStore;
    private final SimpleListPropertyCommandExecutor simpleExecutor;
    private final TopologyLayerCallback topologyLayerCallback;
    private final ListCommandIndexRepairer indexRepairer;

    private ListPropertyMetaData metaData;

    /**
     * Initializes an instance with all its dependencies.
     * 
     * @param listMetaDataStore
     *            Used to read and update versions of {@link List}s.
     * @param indexRepairer
     *            Used to repair the indices of remote and local commands.
     * @param simpleExecutor
     *            Used to execute changes on {@link List}s.
     * @param topologyLayerCallback
     *            Used to re-send repaired local commands.
     */
    public ReparingListPropertyCommandExecutor(final ListPropertyMetaDataStore listMetaDataStore,
            final ListCommandIndexRepairer indexRepairer, final SimpleListPropertyCommandExecutor simpleExecutor,
            final TopologyLayerCallback topologyLayerCallback) {
        this.listMetaDataStore = listMetaDataStore;
        this.indexRepairer = indexRepairer;
        this.simpleExecutor = simpleExecutor;
        this.topologyLayerCallback = topologyLayerCallback;
    }

    /**
     * Logs a command that was locally generated and send to other peers.
     * 
     * @param localCommand
     *            The command to log
     */
    public void logLocalCommand(final ListCommand localCommand) {
        getMetaData(localCommand);

        metaData.getUnapprovedCommands().offer(localCommand);
    }

    /**
     * Executes a remotely received command, repairs it when necessary and resends repaired versions of local commands
     * that where obsoleted by the received command.
     * 
     * @param command
     *            The command to execute.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    // The alternative would be to change the TopologolyLayerCallback.send() to take List<? extends Command>
    public void execute(final ListCommand command) {
        getMetaData(command);

        final Queue<ListCommand> log = metaData.getUnapprovedCommands();
        if (log.isEmpty()) {
            updateVersion(command);
            executeCommand(command);
        } else if (log.peek().equals(command)) {
            updateVersion(command);
            log.remove();
        } else {
            // change local list
            final List<? extends ListCommand> repairedCommands = indexRepairer.repairCommands(
                    metaData.getUnapprovedCommands(), command);
            for (final ListCommand repaired : repairedCommands) {
                executeCommand(repaired);
            }
            updateVersion(command);

            // re-send repaired local changes
            topologyLayerCallback.sendCommands((List) metaData.getUnapprovedCommandsAsList());
        }
    }

    private void getMetaData(final ListCommand command) {
        metaData = listMetaDataStore.getMetaDataOrFail(command.getListId());
    }

    private void updateVersion(final ListCommand command) {
        metaData.setApprovedVersion(command.getListVersionChange().getToVersion());
    }

    private void executeCommand(final ListCommand command) {
        if (command instanceof AddToList) {
            simpleExecutor.execute((AddToList) command);
        } else if (command instanceof RemoveFromList) {
            simpleExecutor.execute((RemoveFromList) command);
        } else if (command instanceof ReplaceInList) {
            simpleExecutor.execute((ReplaceInList) command);
        } else {
            throw new SynchronizeFXException(String.format(
                    "The executor does not know how to handle list commands of type '%s'.", command.getClass()));
        }
    }

}
