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

package de.saxsys.synchronizefx.core.metamodel.executors;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.PutToMap;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private CommandLogDispatcher cut;

    @Captor
    private ArgumentCaptor<SetPropertyValue> setPropertyValueCaptor;

    /**
     * {@link SetPropertyValue} commands should be sent to the {@link RepairingSingleValuePropertyCommandExecutor}.
     */
    @Test
    public void dispatchesSetPropertyValueToSingleValuePropertyCommandExecutor() {
        final SetPropertyValue msg1 = new SetPropertyValue(UUID.randomUUID(), new Value("exampleValue"));
        final SetPropertyValue msg2 = new SetPropertyValue(UUID.randomUUID(), new Value(UUID.randomUUID()));

        final List<Command> commands = Arrays.asList(new PutToMap(), new ReplaceInList(), msg1, new Command() {
        }, msg2);

        cut.logLocalCommands(commands);

        verify(singleValue, times(2)).logLocalCommand(setPropertyValueCaptor.capture());
        assertThat(setPropertyValueCaptor.getAllValues().get(0)).isEqualTo(msg1);
        assertThat(setPropertyValueCaptor.getAllValues().get(1)).isEqualTo(msg2);
    }

    /**
     * When no executors are interested in commands, the dispatcher should not fail.
     */
    @Test
    public void doesNotFailWhenNoExecutorsAreRegisteredForCommands() {
        cut = new CommandLogDispatcher();

        cut.logLocalCommands(Arrays.asList(new PutToMap(), new ReplaceInList(), new SetPropertyValue(null, null)));
        // passes when no exception is thrown.
    }
}
