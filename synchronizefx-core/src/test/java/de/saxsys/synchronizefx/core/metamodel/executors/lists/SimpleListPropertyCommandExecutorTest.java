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

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import de.saxsys.synchronizefx.core.metamodel.SilentChangeExecutor;
import de.saxsys.synchronizefx.core.metamodel.ValueMapper;
import de.saxsys.synchronizefx.core.metamodel.WeakObjectRegistry;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Checks if {@link SimpleListPropertyCommandExecutor} works as expected.
 * 
 * @author Raik Bieniek
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleListPropertyCommandExecutorTest {

    private final UUID exemplaryListId = UUID.randomUUID();
    private final ListProperty<String> exemplaryList = new SimpleListProperty<>(
            FXCollections.<String> observableArrayList());

    @Mock
    private WeakObjectRegistry objectRegistry;

    @Mock
    private SilentChangeExecutor silentChangeExecutor;

    @Mock
    private ValueMapper valueMapper;

    @InjectMocks
    private SimpleListPropertyCommandExecutor cut;

    /**
     * Sets up the default behavior of the mocks.
     */
    @Before
    public void setUpMocks() {
        when(objectRegistry.getByIdOrFail(exemplaryListId)).thenReturn(exemplaryList);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) {
                ((Runnable) (invocation.getArguments()[1])).run();
                return null;
            }
        }).when(silentChangeExecutor).execute(any(), any(Runnable.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) {
                return ((Value) (invocation.getArguments()[0])).getSimpleObjectValue();
            }
        }).when(valueMapper).map(any(Value.class));
    }

    /**
     * The executor should be able to execute {@link AddToList} commands.
     */
    @Test
    public void shouldExecuteAddToListCommands() {
        final AddToList command1 = new AddToList(exemplaryListId, new Value("second"), 0);
        final AddToList command2 = new AddToList(exemplaryListId, new Value("first"), 0);
        final AddToList command3 = new AddToList(exemplaryListId, new Value("third"), 2);

        cut.execute(command1);
        cut.execute(command2);
        cut.execute(command3);

        assertThat(exemplaryList.get()).containsExactly("first", "second", "third");
    }

    /**
     * The executor should be able to execute {@link RemoveFromList} commands.
     */
    @Test
    @Ignore("not implemented yet")
    public void shouldExecuteRemoveFromListCommands() {
        fail("not implemented yet");
    }

    /**
     * The executor should be able to execute {@link ReplaceInList} commands.
     */
    @Test
    @Ignore("not implemented yet")
    public void shouldExecuteReplaceInListCommands() {
        fail("not implemented yet");
    }

    /**
     * All changes done to the list should be executed in the model change executor.
     * 
     * <p>
     * This is tested by assuming that the list is unchanged when no change was executed.
     * </p>
     * 
     */
    @Test
    @Ignore("not implemented yet")
    public void shouldUseSilentChangeExecutorForAllExecutions() {
        exemplaryList.add("initial value");

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) {
                // do not execute changes passed to the mock
                return null;
            }
        }).when(silentChangeExecutor).execute(any(), any(Runnable.class));

        final AddToList command1 = new AddToList(exemplaryListId, new Value("should not be added"), 0);

        cut.execute(command1);

        assertThat(exemplaryList).containsExactly("initial value");

        fail("TODO remove and set command");
    }
}
