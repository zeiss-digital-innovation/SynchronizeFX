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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.PutToMap;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;
import de.saxsys.synchronizefx.core.metamodel.executors.lists.ReparingListPropertyCommandExecutor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Checks if {@link CommandLogDispatcher} works as expected.
 * 
 * @author Raik Bieniek
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandLogDispatcherTest {

    @Mock
    private RepairingSingleValuePropertyCommandExecutor singleValue;

    @Mock
    private ReparingListPropertyCommandExecutor lists;

    @InjectMocks
    private CommandLogDispatcher cut;

    @Captor
    private ArgumentCaptor<SetPropertyValue> setPropertyValueCaptor;

    @Captor
    private ArgumentCaptor<ListCommand> listCommandCaptor;

    /**
     * {@link SetPropertyValue} commands should be sent to the {@link RepairingSingleValuePropertyCommandExecutor}.
     */
    @Test
    public void dispatchesSetPropertyValueToSingleValuePropertyCommandExecutor() {
        final SetPropertyValue msg1 = new SetPropertyValue(UUID.randomUUID(), new Value("exampleValue"));
        final SetPropertyValue msg2 = new SetPropertyValue(UUID.randomUUID(), new Value(UUID.randomUUID()));

        final List<Command> commands = Arrays.asList(new PutToMap(), mock(ReplaceInList.class), msg1, new Command() {
        }, msg2);

        cut.logLocalCommands(commands);

        verify(singleValue, times(2)).logLocalCommand(setPropertyValueCaptor.capture());
        assertThat(setPropertyValueCaptor.getAllValues().get(0)).isEqualTo(msg1);
        assertThat(setPropertyValueCaptor.getAllValues().get(1)).isEqualTo(msg2);
    }

    /**
     * {@link ListCommand}s should be sent to the {@link ReparingListPropertyCommandExecutor}.
     */
    @Test
    public void dispatchesListCommandsToReparingListPropertyCommandExecutor() {
        final AddToList msg1 = mock(AddToList.class);
        final RemoveFromList msg2 = mock(RemoveFromList.class);
        final ReplaceInList msg3 = mock(ReplaceInList.class);

        final List<Command> commands = Arrays.asList(new PutToMap(), msg1, mock(SetPropertyValue.class), msg2,
                new Command() {
                }, msg3);

        cut.logLocalCommands(commands);

        verify(lists, times(3)).logLocalCommand(listCommandCaptor.capture());
        assertThat(listCommandCaptor.getAllValues().get(0)).isEqualTo(msg1);
        assertThat(listCommandCaptor.getAllValues().get(1)).isEqualTo(msg2);
        assertThat(listCommandCaptor.getAllValues().get(2)).isEqualTo(msg3);
    }

    /**
     * When no executors are interested in commands, the dispatcher should not fail.
     */
    @Test
    public void doesNotFailWhenNoExecutorsAreRegisteredForCommands() {
        cut = new CommandLogDispatcher();

        cut.logLocalCommands(Arrays.asList(new PutToMap(), mock(ReplaceInList.class), mock(SetPropertyValue.class)));
        // passes when no exception is thrown.
    }
}
