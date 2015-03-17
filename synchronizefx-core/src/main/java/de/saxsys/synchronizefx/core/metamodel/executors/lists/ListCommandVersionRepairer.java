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
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand.ListVersionChange;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

/**
 * Updates the {@link ListCommand#getListVersionChange()} information of repaired commands.
 * 
 * @author Raik Bieniek
 */
public class ListCommandVersionRepairer {

    /**
     * Updates the versions of repaired local commands that should be resent to the server so that they are based on the
     * version the original remote command produced.
     * 
     * @param localCommands
     *            The local commands that should be repaired. <b>WARNING:</b> The repaired commands are added directly
     *            to the queue again.
     * @param originalRemoteCommand
     *            The remote command that contains the last known version of the list the server has.
     */
    public void repairLocalCommandsVersion(final Queue<ListCommand> localCommands,
            final ListCommand originalRemoteCommand) {
        if (localCommands.isEmpty()) {
            return;
        }

        localCommands.add(repairCommand(localCommands.poll(), originalRemoteCommand.getListVersionChange()
                .getToVersion(), randomUUID()));

        final int count = localCommands.size();
        for (int i = 1; i < count; i++) {
            localCommands.add(repairCommand(localCommands.poll(), randomUUID(), randomUUID()));
        }
    }

    /**
     * Updates the version of the remote commands that should be executed locally to ensure that the local list version
     * equals the version that is resent to the server when they are executed.
     * 
     * @param indexRepairedRemoteCommands
     *            The remote commands thats indices where already repaired. This list can be empty.
     * @param versionRepairedLocalCommands
     *            The local commands that should be resent to the server thats version was already repaired by this
     *            class. This queue must contain at least one element.
     * @return The remote commands with repaired list versions.
     */
    public List<? extends ListCommand> repairRemoteCommandVersion(
            final List<? extends ListCommand> indexRepairedRemoteCommands,
            final List<ListCommand> versionRepairedLocalCommands) {

        final int commandCount = indexRepairedRemoteCommands.size();
        final ListCommand lastLocalCommand = versionRepairedLocalCommands.get(versionRepairedLocalCommands.size() - 1);

        if (commandCount == 0) {
            return asList(new RemoveFromList(lastLocalCommand.getListId(), new ListVersionChange(randomUUID(),
                    lastLocalCommand.getListVersionChange().getToVersion()), 0, 0));
        }

        final List<ListCommand> repaired = new ArrayList<ListCommand>(commandCount);
        for (int i = 0; i < commandCount - 1; i++) {
            repaired.add(indexRepairedRemoteCommands.get(i));
        }
        repaired.add(repairCommand(indexRepairedRemoteCommands.get(commandCount - 1), randomUUID(), lastLocalCommand
                .getListVersionChange().getToVersion()));

        return repaired;
    }

    private ListCommand repairCommand(final ListCommand toRepair, final UUID fromVersion, final UUID toVersion) {
        final ListVersionChange change = new ListVersionChange(fromVersion, toVersion);
        if (toRepair instanceof AddToList) {
            final AddToList orig = (AddToList) toRepair;
            return new AddToList(orig.getListId(), change, orig.getValue(), orig.getPosition());
        } else if (toRepair instanceof RemoveFromList) {
            final RemoveFromList orig = (RemoveFromList) toRepair;
            return new RemoveFromList(orig.getListId(), change, orig.getStartPosition(), orig.getRemoveCount());
        } else if (toRepair instanceof ReplaceInList) {
            final ReplaceInList orig = (ReplaceInList) toRepair;
            return new ReplaceInList(orig.getListId(), change, orig.getValue(), orig.getPosition());
        } else {
            throw new SynchronizeFXException(String.format(
                    "The executor does not know how to handle list commands of type '%s'.", toRepair.getClass()));
        }
    }
}
