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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static java.util.Arrays.asList;

import de.saxsys.synchronizefx.core.metamodel.Optional;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks if {@link ListCommandIndexRepairer} works as expected.
 * 
 * @author Raik Bieniek
 */
@RunWith(MockitoJUnitRunner.class)
public class ListCommandIndexRepairerTest {

    @Mock
    private AddToListRepairer addToListRepairer;

    @Mock
    private RemoveFromListRepairer removeFromListRepairer;

    @Mock
    private ReplaceInListRepairer replaceInListRepairer;

    @InjectMocks
    private ListCommandIndexRepairer cut;

    /**
     * The cut can repair {@link AddToList} commands.
     */
    @Test
    public void canRepairRemoteAddToListCommands() {
        final AddToList remote = mock(AddToList.class);
        final AddToList repairedRemote = mock(AddToList.class);

        final AddToList local = mock(AddToList.class);
        final AddToList repairedLocal = mock(AddToList.class);
        final Queue<ListCommand> localQueue = queue(local);

        when(addToListRepairer.repairRemoteCommand(remote, local)).thenReturn(repairedRemote);
        when(addToListRepairer.repairLocalCommand(local, remote)).thenReturn(repairedLocal);

        final List<? extends ListCommand> repairCommands = cut.repairCommands(localQueue, remote);

        assertThat(repairCommands).hasSize(1);
        assertThat(repairCommands.get(0)).isSameAs(repairedRemote);
        assertThat(localQueue).containsExactly(repairedLocal);
    }

    /**
     * The cut can repair {@link RemoveFromList} commands.
     */
    @Test
    public void canRepairRemoteRemoveFromListCommands() {
        final RemoveFromList remote = mock(RemoveFromList.class);
        final RemoveFromList repairedRemote = mock(RemoveFromList.class);

        final RemoveFromList local = mock(RemoveFromList.class);
        final RemoveFromList repairedLocal = mock(RemoveFromList.class);
        final Queue<ListCommand> localQueue = queue(local);

        when(removeFromListRepairer.repairCommand(remote, local)).thenReturn(asList(repairedRemote));
        when(removeFromListRepairer.repairCommand(local, remote)).thenReturn(asList(repairedLocal));

        final List<? extends ListCommand> repairCommands = cut.repairCommands(localQueue, remote);

        assertThat(repairCommands).hasSize(1);
        assertThat(repairCommands.get(0)).isSameAs(repairedRemote);
        assertThat(localQueue).containsExactly(repairedLocal);
    }

    /**
     * The cut can repair {@link ReplaceInList} commands.
     */
    @Test
    public void canRepairRemoteReplaceInListCommands() {
        final ReplaceInList remote = mock(ReplaceInList.class);
        final ReplaceInList repairedRemote = mock(ReplaceInList.class);

        final ReplaceInList local = mock(ReplaceInList.class);
        final ReplaceInList repairedLocal = mock(ReplaceInList.class);
        final Queue<ListCommand> localQueue = queue(local);

        when(replaceInListRepairer.repairRemoteCommand(remote, local)).thenReturn(repairedRemote);
        when(replaceInListRepairer.repairLocalCommand(local, remote)).thenReturn(Optional.of(repairedLocal));

        final List<? extends ListCommand> repairCommands = cut.repairCommands(localQueue, remote);

        assertThat(repairCommands).hasSize(1);
        assertThat(repairCommands.get(0)).isSameAs(repairedRemote);
        assertThat(localQueue).containsExactly(repairedLocal);
    }

    private Queue<ListCommand> queue(final ListCommand... commands) {
        final Queue<ListCommand> list = new LinkedList<>();
        for (final ListCommand command : commands) {
            list.add(command);
        }
        return list;
    }
}
