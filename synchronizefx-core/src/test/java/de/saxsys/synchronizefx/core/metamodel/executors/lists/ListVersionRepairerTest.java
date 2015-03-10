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

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;

import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand.ListVersionChange;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks if {@link ListVersionRepairer} works as expected.
 * 
 * @author Raik Bieniek
 */
public class ListVersionRepairerTest {

    private final AddToList addToList = new AddToList(randomUUID(), new ListVersionChange(randomUUID(), randomUUID()),
            new Value("some value"), 42);
    private final RemoveFromList removeFromList = new RemoveFromList(randomUUID(), new ListVersionChange(randomUUID(),
            randomUUID()), 42, 3);
    private final ReplaceInList replaceInList = new ReplaceInList(randomUUID(), new ListVersionChange(randomUUID(),
            randomUUID()), new Value("some value"), 42);

    private final AddToList someRemoteCommand = new AddToList(randomUUID(), new ListVersionChange(randomUUID(),
            randomUUID()), new Value("other value"), 8);
    private final ReplaceInList someOtherRemoteCommand = new ReplaceInList(randomUUID(), new ListVersionChange(
            randomUUID(), randomUUID()), new Value("yet another value"), 20);

    private final LinkedList<ListCommand> localCommands = linkedList(addToList, removeFromList, replaceInList);

    private final ListVersionRepairer cut = new ListVersionRepairer();

    // ///////////////////////////
    // / repair local versions ///
    // ///////////////////////////

    /**
     * The list versions of the repaired local list commands are all different then the list versions of the original
     * local list commands.
     */
    @Test
    public void repairedLocalListVersionsAllDifferFromTheirOriginial() {
        cut.repairLocalCommandsVersion(localCommands, someRemoteCommand);

        assertThat(localCommands).hasSize(3);

        final ListVersionChange version1 = localCommands.poll().getListVersionChange();
        assertThat(version1.getFromVersion()).isNotEqualTo(addToList.getListVersionChange().getFromVersion());
        assertThat(version1.getToVersion()).isNotEqualTo(addToList.getListVersionChange().getToVersion());

        final ListVersionChange version2 = localCommands.poll().getListVersionChange();
        assertThat(version2.getFromVersion()).isNotEqualTo(removeFromList.getListVersionChange().getFromVersion());
        assertThat(version2.getToVersion()).isNotEqualTo(removeFromList.getListVersionChange().getToVersion());

        final ListVersionChange version3 = localCommands.poll().getListVersionChange();
        assertThat(version3.getFromVersion()).isNotEqualTo(replaceInList.getListVersionChange().getFromVersion());
        assertThat(version3.getToVersion()).isNotEqualTo(replaceInList.getListVersionChange().getToVersion());
    }

    /**
     * The <code>from</code> version of the first resent local list command equals the <code>to</code> version of the
     * remote command.
     */
    @Test
    public void firstRepairedLocalListVersionStartsFromToVersionOfRemoteCommand() {
        cut.repairLocalCommandsVersion(localCommands, someRemoteCommand);

        assertThat(localCommands).hasSize(3);

        assertThat(localCommands.poll().getListVersionChange().getFromVersion()).isEqualTo(
                someRemoteCommand.getListVersionChange().getToVersion());
    }

    // ////////////////////////////
    // / repair remote versions ///
    // ////////////////////////////

    /**
     * The <code>to</code> version of the last repaired remote command that should be executed locally should be equal
     * to the <code>to</code> version of the last repaired local command that is resent to the server.
     * 
     * <p>
     * This way the local client and the server will have the same list version when the server executed all local
     * commands.
     * </p>
     */
    @Test
    public void toVersionOfLastRemoteCommandEqualsToVersionOfLastLocalCommand() {
        final List<? extends ListCommand> repairRemoteCommands = cut.repairRemoteCommandVersion(
                asList(someRemoteCommand, someOtherRemoteCommand), localCommands);

        assertThat(repairRemoteCommands).hasSize(2);
        assertThat(repairRemoteCommands.get(1).getListVersionChange().getToVersion()).isEqualTo(
                replaceInList.getListVersionChange().getToVersion());
    }

    /**
     * If their is no remote command to repair, a new one is created to ensure that the local list version is updated.
     * 
     * <p>
     * The generated command will be a {@link RemoveFromList} command that removes 0 elements and therefore does not
     * change the list itself.
     * </p>
     */
    @Test
    public void whenThereAreNoRemoteCommandANewOneIsCreatedWhichDoesntChangeTheList() {
        final List<? extends ListCommand> repairRemoteCommands = cut.repairRemoteCommandVersion(
                new ArrayList<ListCommand>(0), localCommands);

        assertThat(repairRemoteCommands).hasSize(1);
        assertThat(repairRemoteCommands.get(0)).isInstanceOf(RemoveFromList.class);
        final RemoveFromList command = (RemoveFromList) repairRemoteCommands.get(0);

        assertThat(command.getListVersionChange().getToVersion()).isEqualTo(
                replaceInList.getListVersionChange().getToVersion());
        assertThat(command.getStartPosition()).isEqualTo(0);
        assertThat(command.getRemoveCount()).isEqualTo(0);
    }

    private LinkedList<ListCommand> linkedList(final ListCommand... commands) {
        final LinkedList<ListCommand> queue = new LinkedList<ListCommand>();
        for (final ListCommand command : commands) {
            queue.add(command);
        }
        return queue;
    }
}
