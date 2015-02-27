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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static java.util.Arrays.asList;

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
 * commands. They therefore have to be repaired as well to be based on the list version the remote command produced
 * and re-send to the other peers.
 * </p>
 * 
 * <p>
 * A local command gets confirmed when it is the same as a received remote command and if it is the oldest
 * unconfirmed command.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class ReparingListPropertyCommandExecutor {

    private final AddToListRepairer addToListRepairer;
    private final RemoveFromListRepairer removeFromListRepairer;
    private final ReplaceInListRepairer replaceInListRepairer;
    private final ListPropertyMetaDataStore listMetaDataStore;
    private final SimpleListPropertyCommandExecutor simpleExecutor;
    private final TopologyLayerCallback topologyLayerCallback;

    private ListPropertyMetaData metaData;

    /**
     * Initializes an instance with all its dependencies.
     * 
     * @param addToListRepairer Used to repair {@link AddToList} commands.
     * @param removeFromListRepairer Used to repair {@link RemoveFromList} commands.
     * @param replaceInListRepairer Used to repair {@link ReplaceInList} commands.
     * @param listMetaDataStore Used to read and update versions of {@link List}s.
     * @param simpleExecutor Used to execute changes on {@link List}s.
     * @param topologyLayerCallback Used to re-send repaired local commands.
     */
    public ReparingListPropertyCommandExecutor(final AddToListRepairer addToListRepairer,
            final RemoveFromListRepairer removeFromListRepairer, final ReplaceInListRepairer replaceInListRepairer,
            final ListPropertyMetaDataStore listMetaDataStore, final SimpleListPropertyCommandExecutor simpleExecutor,
            final TopologyLayerCallback topologyLayerCallback) {
        this.addToListRepairer = addToListRepairer;
        this.removeFromListRepairer = removeFromListRepairer;
        this.replaceInListRepairer = replaceInListRepairer;
        this.listMetaDataStore = listMetaDataStore;
        this.simpleExecutor = simpleExecutor;
        this.topologyLayerCallback = topologyLayerCallback;
    }

    /**
     * Logs a command that was locally generated and send to other peers.
     * 
     * @param localCommand The command to log
     */
    public void logLocalCommand(final ListCommand localCommand) {
        getMetaData(localCommand);

        metaData.getUnapprovedCommands().offer(localCommand);
    }

    /**
     * Executes a remotely received command, repairs it when necessary and resends repaired versions of local
     * commands that where obsoleted by the received command.
     * 
     * @param command The command to execute.
     */
    @SuppressWarnings({"rawtypes", "unchecked" })
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
            final List<? extends ListCommand> repairedCommands = repairCommands(command);
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
            throw failUnknownTyp(command);
        }
    }

    private SynchronizeFXException failUnknownTyp(final ListCommand command) {
        throw new SynchronizeFXException(String.format(
                "The executor does not know how to handle list commands of type '%s'.", command.getClass()));
    }

    // /////////////////////
    // / repair commands ///
    // /////////////////////

    private List<? extends ListCommand> repairCommands(final ListCommand remoteCommand) {
        if (remoteCommand instanceof AddToList) {
            return repairCommands((AddToList) remoteCommand);
        } else if (remoteCommand instanceof RemoveFromList) {
            return repairCommands((RemoveFromList) remoteCommand);
        } else if (remoteCommand instanceof ReplaceInList) {
            return repairCommands((ReplaceInList) remoteCommand);
        } else {
            throw failUnknownTyp(remoteCommand);
        }
    }

    private List<ListCommand> repairCommands(final AddToList remoteCommand) {
        AddToList repaired = remoteCommand;

        final int commandCount = metaData.getUnapprovedCommands().size();
        for (int i = 0; i < commandCount; i++) {
            final ListCommand localCommand = metaData.getUnapprovedCommands().poll();
            if (localCommand instanceof AddToList) {
                repaired = addToListRepairer.repairRemoteCommand(repaired, (AddToList) localCommand);
                metaData.getUnapprovedCommands().add(
                        addToListRepairer.repairLocalCommand((AddToList) localCommand, remoteCommand));
            } else if (localCommand instanceof RemoveFromList) {
                repaired = addToListRepairer.repairCommand(repaired, (RemoveFromList) localCommand);
                metaData.getUnapprovedCommands().addAll(
                        removeFromListRepairer.repairCommand((RemoveFromList) localCommand, remoteCommand));
            } else if (localCommand instanceof ReplaceInList) {
                repaired = addToListRepairer.repairCommand(repaired, (ReplaceInList) localCommand);
                metaData.getUnapprovedCommands().add(
                        replaceInListRepairer.repairCommand(new ReplaceOrAddInList((ReplaceInList) localCommand),
                                remoteCommand).toSimpleCommand());
            } else {
                throw failUnknownTyp(remoteCommand);
            }
        }

        final List<ListCommand> list = new ArrayList<>(1);
        list.add(repaired);
        return list;
    }

    private List<RemoveFromList> repairCommands(final RemoveFromList remoteCommand) {
        List<RemoveFromList> repaired = asList(remoteCommand);

        final int commandCount = metaData.getUnapprovedCommands().size();
        for (int i = 0; i < commandCount; i++) {
            final ListCommand localCommand = metaData.getUnapprovedCommands().poll();
            final List<RemoveFromList> repairedLastRound = repaired;
            repaired = new LinkedList<>();

            // repair remote command
            for (final RemoveFromList toRepair : repairedLastRound) {
                if (localCommand instanceof AddToList) {
                    repaired.addAll(removeFromListRepairer.repairCommand(toRepair, (AddToList) localCommand));
                } else if (localCommand instanceof RemoveFromList) {
                    repaired.addAll(removeFromListRepairer.repairCommand(toRepair, (RemoveFromList) localCommand));
                } else if (localCommand instanceof ReplaceInList) {
                    repaired.addAll(removeFromListRepairer.repairCommand(toRepair, (ReplaceInList) localCommand));
                } else {
                    throw failUnknownTyp(remoteCommand);
                }
            }

            // repair local commands
            if (localCommand instanceof AddToList) {
                metaData.getUnapprovedCommands().add(
                        addToListRepairer.repairCommand((AddToList) localCommand, remoteCommand));
            } else if (localCommand instanceof RemoveFromList) {
                metaData.getUnapprovedCommands().addAll(
                        removeFromListRepairer.repairCommand((RemoveFromList) localCommand, remoteCommand));
            } else if (localCommand instanceof ReplaceInList) {
                metaData.getUnapprovedCommands().add(
                        replaceInListRepairer.repairCommand(new ReplaceOrAddInList((ReplaceInList) localCommand),
                                remoteCommand).toSimpleCommand());
            } else {
                throw failUnknownTyp(remoteCommand);
            }
        }

        return repaired;
    }

    private List<ListCommand> repairCommands(final ReplaceInList remoteCommand) {
        ReplaceOrAddInList repaired = new ReplaceOrAddInList(remoteCommand);

        final int commandCount = metaData.getUnapprovedCommands().size();
        for (int i = 0; i < commandCount; i++) {
            final ListCommand localCommand = metaData.getUnapprovedCommands().poll();
            if (localCommand instanceof AddToList) {
                repaired = replaceInListRepairer.repairCommand(repaired, (AddToList) localCommand);
                metaData.getUnapprovedCommands().add(
                        addToListRepairer.repairCommand((AddToList) localCommand, remoteCommand));
            } else if (localCommand instanceof RemoveFromList) {
                repaired = replaceInListRepairer.repairCommand(repaired, (RemoveFromList) localCommand);
                metaData.getUnapprovedCommands().addAll(
                        removeFromListRepairer.repairCommand((RemoveFromList) localCommand, remoteCommand));
            } else if (localCommand instanceof ReplaceInList) {
                repaired = replaceInListRepairer.repairCommand(repaired, (ReplaceInList) localCommand);
                metaData.getUnapprovedCommands().add(
                        replaceInListRepairer.repairCommand(new ReplaceOrAddInList((ReplaceInList) localCommand),
                                remoteCommand).toSimpleCommand());
            } else {
                throw failUnknownTyp(remoteCommand);
            }
        }

        final List<ListCommand> list = new ArrayList<>(1);
        list.add(repaired.toSimpleCommand());
        return list;
    }
}
