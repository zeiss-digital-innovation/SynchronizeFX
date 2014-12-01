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

import java.util.UUID;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import de.saxsys.synchronizefx.core.metamodel.SilentChangeExecutor;
import de.saxsys.synchronizefx.core.metamodel.ValueMapper;
import de.saxsys.synchronizefx.core.metamodel.WeakObjectRegistry;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Checks if {@link SimpleSingleValuePropertyCommandExecutor} works as expected.
 * 
 * @author Raik Bieniek
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleSingleValuePropertyCommandExecutorTest {

    private final UUID exemplaryProperty1Id = UUID.randomUUID();
    private final String exemplaryProperty1Value = "original value";
    private final StringProperty exemplaryProperty1 = new SimpleStringProperty(exemplaryProperty1Value);
    private final SetPropertyValue exemplaryProperty1Change = new SetPropertyValue(UUID.randomUUID(),
            exemplaryProperty1Id, new Value("changed value"));

    @Mock
    private WeakObjectRegistry objectRegistry;

    @Mock
    private SilentChangeExecutor silentChangeExecutor;

    @Mock
    private ValueMapper valueMapper;

    @InjectMocks
    private SimpleSingleValuePropertyCommandExecutor cut;

    /**
     * Wires up the mocks.
     */
    @Before
    public void setUpMocks() {
        when(objectRegistry.getByIdOrFail(exemplaryProperty1Id)).thenReturn(exemplaryProperty1);
        
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) {
                ((Runnable) (invocation.getArguments()[1])).run();
                return null;
            }
        }).when(silentChangeExecutor).execute(any(), any(Runnable.class));

        when(valueMapper.map(exemplaryProperty1Change.getValue())).thenReturn("changed value");
    }

    /**
     * The executor should change the value of a {@link Property} according to the contents of the command it executes.
     */
    @Test
    public void shouldChangeTheValueOfAProperty() {
        cut.execute(exemplaryProperty1Change);

        assertThat(exemplaryProperty1.get()).isEqualTo("changed value");
    }

    /**
     * All changes done to the properties in the users domain model must not be signalized to the change message
     * generator of SynchronizeFX.
     */
    @Test
    public void shouldChangeTheDomainModelOfTheUserWithoutNotifingTheListeners() {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) {
                // do not execute changes passed to the mock
                return null;
            }
        }).when(silentChangeExecutor).execute(any(), any(Runnable.class));

        cut.execute(exemplaryProperty1Change);

        verify(silentChangeExecutor).execute(any(), any(Runnable.class));

        // changes on Property 1 have not been executed
        assertThat(exemplaryProperty1.get()).isEqualTo(exemplaryProperty1Value);
    }
}
