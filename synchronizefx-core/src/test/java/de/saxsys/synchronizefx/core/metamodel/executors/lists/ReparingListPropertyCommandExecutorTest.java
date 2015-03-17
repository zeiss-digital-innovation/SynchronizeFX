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

import java.util.Arrays;
import java.util.Queue;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;

import de.saxsys.synchronizefx.core.metamodel.ListPropertyMetaDataStore;
import de.saxsys.synchronizefx.core.metamodel.ListPropertyMetaDataStore.ListPropertyMetaData;
import de.saxsys.synchronizefx.core.metamodel.TopologyLayerCallback;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand.ListVersionChange;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Checks if {@link ReparingListPropertyCommandExecutor} works as expected.
 * 
 * @author Raik Bieniek
 */
@RunWith(MockitoJUnitRunner.class)
public class ReparingListPropertyCommandExecutorTest {

    private static final ListVersionChange EXEMPLARY_CHANGE = new ListVersionChange(randomUUID(), randomUUID());
    private static final Value EXEMPLARY_VALUE = new Value("exemplary value");

    private static final AddToList EXEMPLARY_ADD_COMMAND = new AddToList(randomUUID(), EXEMPLARY_CHANGE,
            EXEMPLARY_VALUE, 5);

    @Mock
    private ListCommandIndexRepairer indexRepairer;

    @Mock
    private ListPropertyMetaDataStore listVersions;

    @Mock
    private SimpleListPropertyCommandExecutor simpleExecutor;

    @Mock
    private TopologyLayerCallback topologyLayerCallback;

    @InjectMocks
    private ReparingListPropertyCommandExecutor cut;

    /**
     * Wires up the mocks.
     */
    @Before
    public void setUpMockBehavior() {
        when(listVersions.getMetaDataOrFail(any(UUID.class))).thenReturn(new ListPropertyMetaData(null, null));
    }

    /**
     * When no local command is in the log for unapproved commands, all remote commands should be executed directly.
     */
    @Test
    public void shouldExecuteRemoteCommandWhenLogIsEmpty() {
        cut.execute(EXEMPLARY_ADD_COMMAND);

        verify(simpleExecutor).execute(argThat(new SameIdAndVersion<>(EXEMPLARY_ADD_COMMAND)));
    }

    /**
     * When a command is executed, the approved version of the list version should be updated.
     */
    @Test
    public void shouldUpdateApprovedVersionWhenExecutingACommand() {
        final ListPropertyMetaData listMetaData = new ListPropertyMetaData(null, null);
        when(listVersions.getMetaDataOrFail(EXEMPLARY_ADD_COMMAND.getListId())).thenReturn(listMetaData);

        cut.execute(EXEMPLARY_ADD_COMMAND);

        assertThat(listMetaData.getApprovedVersion()).isEqualTo(EXEMPLARY_CHANGE.getToVersion());
    }

    /**
     * When a received command equals the first command in the log of unapproved commands it should not be executed and
     * be removed from the command log.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldDropApprovedLocalCommandAndRemoveItFromTheCommandLog() {
        final ListPropertyMetaData listMetaData = new ListPropertyMetaData(null, null);
        final UUID changedList = EXEMPLARY_ADD_COMMAND.getListId();
        when(listVersions.getMetaDataOrFail(changedList)).thenReturn(listMetaData);

        final RemoveFromList otherChangeSameList = new RemoveFromList(changedList, new ListVersionChange(randomUUID(),
                randomUUID()), 5, 3);

        cut.logLocalCommand(EXEMPLARY_ADD_COMMAND);
        cut.logLocalCommand(otherChangeSameList);

        cut.execute(EXEMPLARY_ADD_COMMAND);

        // Version raised but not executed because it's a local command.
        assertThat(listMetaData.getApprovedVersion()).isEqualTo(
                EXEMPLARY_ADD_COMMAND.getListVersionChange().getToVersion());
        verify(simpleExecutor, times(0)).execute(any(AddToList.class));

        // check that the local command is removed from the list
        doReturn(asList(mock(AddToList.class))).when(indexRepairer).repairCommands(any(Queue.class),
                same(EXEMPLARY_ADD_COMMAND));
        cut.execute(EXEMPLARY_ADD_COMMAND);
        // Executed this time because it was removed from the command log.
        verify(simpleExecutor, times(1)).execute(any(AddToList.class));
    }

    /**
     * When a received command equals the first command in the log of unapproved commands, the approved version of the
     * list should be updated.
     */
    @Test
    public void shouldUpdateApprovedVersionWhenRemoteCommandEqualsOldestUnapprovedLocalCommand() {
        final ListPropertyMetaData listMetaData = new ListPropertyMetaData(null, null);
        when(listVersions.getMetaDataOrFail(EXEMPLARY_ADD_COMMAND.getListId())).thenReturn(listMetaData);

        cut.logLocalCommand(EXEMPLARY_ADD_COMMAND);
        cut.execute(EXEMPLARY_ADD_COMMAND);

        assertThat(listMetaData.getApprovedVersion()).isEqualTo(EXEMPLARY_CHANGE.getToVersion());
    }

    /**
     * If a remote command was not the oldest unapproved locally generated command, the server executed a command of
     * another peer first. This command has to be repaired and than be executed.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldRepairAndExecuteRemoteCommandThatWasntTheOldestUnapprovedLocalCommand() {
        final RemoveFromList otherCommandSameList = new RemoveFromList(EXEMPLARY_ADD_COMMAND.getListId(),
                EXEMPLARY_CHANGE, 5, 3);
        final RemoveFromList simulatedRepairedCommand = new RemoveFromList(randomUUID(), EXEMPLARY_CHANGE, 8, 6);

        doReturn(asList(simulatedRepairedCommand)).when(indexRepairer).repairCommands(any(Queue.class),
                same(otherCommandSameList));

        cut.logLocalCommand(EXEMPLARY_ADD_COMMAND);
        cut.execute(otherCommandSameList);

        verify(simpleExecutor).execute(simulatedRepairedCommand);
    }

    /**
     * If a remote command was not the oldest unapproved locally generated command, the server executed a command of
     * another peer first. All remote peers will drop the locally generated commands because they cannot be applied to
     * their version of the list. Because of this all local commands need to be repaired and resend.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldRepairAndResendAllUnapprovedLocalCommandsWhenRemoteCommandWasntFirstUnapprovedLocalCommand() {
        final RemoveFromList remoteCommand = new RemoveFromList(EXEMPLARY_ADD_COMMAND.getListId(), EXEMPLARY_CHANGE, 5,
                3);

        final AddToList localCommand1 = new AddToList(randomUUID(), EXEMPLARY_CHANGE, EXEMPLARY_VALUE, 7);
        final ReplaceInList localCommand2 = new ReplaceInList(randomUUID(), EXEMPLARY_CHANGE, EXEMPLARY_VALUE, 0);

        final RemoveFromList repairedRemoteCommand = new RemoveFromList(randomUUID(), EXEMPLARY_CHANGE, 8, 1);

        cut.logLocalCommand(localCommand1);
        cut.logLocalCommand(localCommand2);

        doReturn(asList(repairedRemoteCommand)).when(indexRepairer).repairCommands(any(Queue.class),
                same(remoteCommand));

        cut.execute(remoteCommand);

        // repaired commands should have been resent to the server
        // as the mock does no local command repairing, original commands will be resend
        verify(topologyLayerCallback).sendCommands(Arrays.<Command> asList(localCommand1, localCommand2));
    }

    /**
     * There should be a separate command log for each list.
     */
    @Test
    public void shouldUseSeparateCommandLogsForEachListProperty() {
        final UUID otherListId = randomUUID();
        final ListPropertyMetaData otherMetaData = new ListPropertyMetaData(null, null);
        when(listVersions.getMetaDataOrFail(otherListId)).thenReturn(otherMetaData);
        final ListCommand commandOtherList = new AddToList(otherListId, EXEMPLARY_CHANGE, EXEMPLARY_VALUE, 3);

        cut.logLocalCommand(commandOtherList);
        cut.execute(EXEMPLARY_ADD_COMMAND);

        // The logged command is logged for another list so the list for EXEMPLARY_ADD_COMMAND is empty and nothing must
        // be repaired.
        verifyNoMoreInteractions(indexRepairer);
    }

    /**
     * Compares the {@link ListCommand#getListId()} and {@link ListCommand#getListVersionChange()} of
     * {@link ListCommand}s.
     */
    private static class SameIdAndVersion<T extends ListCommand> extends ArgumentMatcher<T> {

        private final ListCommand compareTo;

        public SameIdAndVersion(final T compareTo) {
            this.compareTo = compareTo;
        }

        @Override
        public boolean matches(final Object argument) {
            if (!(argument instanceof ListCommand)) {
                return false;
            }
            final ListCommand candidate = (ListCommand) argument;
            return candidate.getListId().equals(compareTo.getListId())
                    && candidate.getListVersionChange().equals(compareTo.getListVersionChange());
        }

    }
}
