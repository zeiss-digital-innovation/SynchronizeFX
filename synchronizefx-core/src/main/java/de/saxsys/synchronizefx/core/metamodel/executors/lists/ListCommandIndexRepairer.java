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
import de.saxsys.synchronizefx.core.metamodel.Optional;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

/**
 * Repairs the indices of {@link ListCommand}s.
 * 
 * @author Raik Bieniek
 */
public class ListCommandIndexRepairer {

    private final AddToListRepairer addToListRepairer;
    private final RemoveFromListRepairer removeFromListRepairer;
    private final ReplaceInListRepairer replaceInListRepairer;
    private Queue<ListCommand> localCommands;

    /**
     * Initializes an instance with all its dependencies.
     * 
     * @param addToListRepairer
     *            Used to repair {@link AddToList} commands.
     * @param removeFromListRepairer
     *            Used to repair {@link RemoveFromList} commands.
     * @param replaceInListRepairer
     *            Used to repair {@link ReplaceInList} commands.
     */
    public ListCommandIndexRepairer(final AddToListRepairer addToListRepairer,
            final RemoveFromListRepairer removeFromListRepairer, final ReplaceInListRepairer replaceInListRepairer) {
        this.addToListRepairer = addToListRepairer;
        this.removeFromListRepairer = removeFromListRepairer;
        this.replaceInListRepairer = replaceInListRepairer;
    }

    /**
     * Repairs the indices of local and remote {@link ListCommand}s.
     * 
     * @param remoteCommand
     *            The remote command to repair.
     * @param localCommands
     *            The local commands that should be repaired. After returning from this method, the queue will contain
     *            only the repaired commands.
     * @return The commands that resulted from repairing the remote command.
     */
    public List<? extends ListCommand> repairCommands(final Queue<ListCommand> localCommands,
            final ListCommand remoteCommand) {
        this.localCommands = localCommands;
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

        final int commandCount = localCommands.size();
        for (int i = 0; i < commandCount; i++) {
            final ListCommand localCommand = localCommands.poll();
            repaired = repairRemoteCommand(localCommand, repaired);
            if (localCommand instanceof AddToList) {
                localCommands.add(addToListRepairer.repairLocalCommand((AddToList) localCommand, remoteCommand));
            } else if (localCommand instanceof RemoveFromList) {
                localCommands
                        .addAll(removeFromListRepairer.repairCommand((RemoveFromList) localCommand, remoteCommand));
            } else if (localCommand instanceof ReplaceInList) {
                localCommands.add(replaceInListRepairer.repairCommand((ReplaceInList) localCommand, remoteCommand));
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

        final int commandCount = localCommands.size();
        for (int i = 0; i < commandCount; i++) {
            final ListCommand localCommand = localCommands.poll();
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
                localCommands.add(addToListRepairer.repairCommand((AddToList) localCommand, remoteCommand));
            } else if (localCommand instanceof RemoveFromList) {
                localCommands
                        .addAll(removeFromListRepairer.repairCommand((RemoveFromList) localCommand, remoteCommand));
            } else if (localCommand instanceof ReplaceInList) {
                localCommands.add(replaceInListRepairer.repairCommand((ReplaceInList) localCommand, remoteCommand));
            } else {
                throw failUnknownTyp(remoteCommand);
            }
        }

        return repaired;
    }

    private List<ListCommand> repairCommands(final ReplaceInList remoteCommand) {
        ListCommand repaired = remoteCommand;

        final int commandCount = localCommands.size();
        for (int i = 0; i < commandCount; i++) {
            final ListCommand localCommand = localCommands.poll();
            if (repaired instanceof ReplaceInList) {
                repaired = repairRemoteCommand(localCommand, (ReplaceInList) repaired);
            } else {
                repaired = repairRemoteCommand(localCommand, (AddToList) repaired);
            }
            if (localCommand instanceof AddToList) {
                localCommands.add(addToListRepairer.repairCommand((AddToList) localCommand, remoteCommand));
            } else if (localCommand instanceof RemoveFromList) {
                localCommands
                        .addAll(removeFromListRepairer.repairCommand((RemoveFromList) localCommand, remoteCommand));
            } else if (localCommand instanceof ReplaceInList) {
                final Optional<ReplaceInList> repairedLocalCommand = replaceInListRepairer.repairLocalCommand(
                        (ReplaceInList) localCommand, remoteCommand);
                if (repairedLocalCommand.isPresent()) {
                    localCommands.add(repairedLocalCommand.get());
                }
            } else {
                throw failUnknownTyp(remoteCommand);
            }
        }

        final List<ListCommand> list = new ArrayList<>(1);
        list.add(repaired);
        return list;
    }

    private AddToList repairRemoteCommand(final ListCommand localCommand, final AddToList remoteCommand) {
        if (localCommand instanceof AddToList) {
            return addToListRepairer.repairRemoteCommand(remoteCommand, (AddToList) localCommand);
        } else if (localCommand instanceof RemoveFromList) {
            return addToListRepairer.repairCommand(remoteCommand, (RemoveFromList) localCommand);
        } else if (localCommand instanceof ReplaceInList) {
            return addToListRepairer.repairCommand(remoteCommand, (ReplaceInList) localCommand);
        } else {
            throw failUnknownTyp(remoteCommand);
        }
    }

    private ListCommand repairRemoteCommand(final ListCommand localCommand, final ReplaceInList remoteCommand) {
        if (localCommand instanceof AddToList) {
            return replaceInListRepairer.repairCommand(remoteCommand, (AddToList) localCommand);
        } else if (localCommand instanceof RemoveFromList) {
            return replaceInListRepairer.repairCommand(remoteCommand, (RemoveFromList) localCommand);
        } else if (localCommand instanceof ReplaceInList) {
            return replaceInListRepairer.repairRemoteCommand(remoteCommand, (ReplaceInList) localCommand);
        } else {
            throw failUnknownTyp(remoteCommand);
        }
    }

    private SynchronizeFXException failUnknownTyp(final ListCommand command) {
        throw new SynchronizeFXException(String.format(
                "The executor does not know how to handle list commands of type '%s'.", command.getClass()));
    }
}
