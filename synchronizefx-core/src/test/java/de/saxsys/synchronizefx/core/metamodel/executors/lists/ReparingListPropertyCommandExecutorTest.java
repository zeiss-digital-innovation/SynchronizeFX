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

import java.util.UUID;

import static java.util.UUID.randomUUID;

import de.saxsys.synchronizefx.core.metamodel.ListVersions;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand.ListVersionChange;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Checks if {@link ReparingListPropertyCommandExecutor} works as expected.
 * 
 * @author Raik Bieniek
 */
@Ignore("implementation not finished yet")
@RunWith(MockitoJUnitRunner.class)
public class ReparingListPropertyCommandExecutorTest {

    private static final ListVersionChange EXEMPLARY_CHANGE = new ListVersionChange(randomUUID(), randomUUID());
    private static final Value EXEMPLARY_VALUE = new Value("exemplary value");

    private static final AddToList EXEMPLARY_ADD_COMMAND = new AddToList(randomUUID(), EXEMPLARY_CHANGE,
            EXEMPLARY_VALUE, 5);

    @Mock
    private ListVersions listVersions;

    @Mock
    private SimpleListPropertyCommandExecutor simpleExecutor;

    @InjectMocks
    private ReparingListPropertyCommandExecutor cut;

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
        cut.execute(EXEMPLARY_ADD_COMMAND);

        verify(listVersions).setApprovedVersion(EXEMPLARY_ADD_COMMAND.getListId(), EXEMPLARY_CHANGE.getToVersion());
    }

    /**
     * When a received command equals the first command in the log of unapproved commands it should not be executed and
     * be removed from the command log.
     */
    @Test
    public void shouldDropApprovedLocalCommandAndRemoveItFromTheCommandLog() {
        final UUID changedList = EXEMPLARY_ADD_COMMAND.getListId();
        final ListCommand otherChangeSameList = new RemoveFromList(changedList, new ListVersionChange(randomUUID(),
                randomUUID()), 5, 3);

        cut.logLocalCommand(EXEMPLARY_ADD_COMMAND);
        cut.logLocalCommand(otherChangeSameList);

        cut.execute(EXEMPLARY_ADD_COMMAND);
        // Version raised but not executed because it's a local command.
        verify(listVersions).setApprovedVersion(changedList,
                EXEMPLARY_ADD_COMMAND.getListVersionChange().getToVersion());
        verify(simpleExecutor, times(0)).execute(any(AddToList.class));

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
        cut.logLocalCommand(EXEMPLARY_ADD_COMMAND);

        cut.execute(EXEMPLARY_ADD_COMMAND);

        verify(listVersions).setApprovedVersion(EXEMPLARY_ADD_COMMAND.getListId(), EXEMPLARY_CHANGE.getToVersion());
    }

    @Test
    public void shouldRepairAndExecuteRemoteCommandThatWasntTheOldestUnapprovedLocalCommand() {
        fail("not implemented yet");
    }

    @Test
    public void shouldRepairAndResendAllUnapprovedLocalCommandsWhenRemoteCommandWasntFirstUnapprovedLocalCommand() {
        fail("not implemented yet");
    }

    @Test
    public void shouldUseSeparateCommandLogsForEachListProperty() {
        fail("not implemented yet");
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
