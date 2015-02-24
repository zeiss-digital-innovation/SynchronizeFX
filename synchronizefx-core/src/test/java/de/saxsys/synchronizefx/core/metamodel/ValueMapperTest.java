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

package de.saxsys.synchronizefx.core.metamodel;

import java.util.UUID;

import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests if {@link ValueMapper} works as expected.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValueMapperTest {

    private static final UUID SAMPLE_ID = UUID.randomUUID();

    @Mock
    private WeakObjectRegistry objectRegistry;

    @InjectMocks
    private ValueMapper cut;

    /**
     * When a message is received that describes a simple object, an {@link ObservedValue} wrapping an simple object
     * should be returned.
     */
    @Test
    public void shouldReturnSimpleObjectObservedValueForSimpleObjectMessages() {
        final Value message = new Value("some value");

        final Object maped = cut.map(message);

        assertThat(maped).isSameAs(message.getSimpleObjectValue());
    }

    /**
     * When a message is received that describes an observable object with an id that is known in the registry, an
     * {@link ObservedValue} containing that object should be returned.
     */
    @Test
    public void shouldReturnObservableObjectValueForObservableObjectMessage() {
        final Object sampleObservableObject = "sample object";
        when(objectRegistry.getByIdOrFail(SAMPLE_ID)).thenReturn(sampleObservableObject);
        final Value message = new Value(SAMPLE_ID);

        final Object maped = cut.map(message);

        assertThat(maped).isSameAs(sampleObservableObject);
    }

    /**
     * {@link ObservedValue}s describing <em>simple objects</em> should be mapped to <em>simple object</em> messages.
     */
    @Test
    public void shouldReturnSimpleObjectMessageForSimpleObjectValue() {
        final Value message = cut.map("sample object", false);

        assertThat(message.getObservableObjectId()).isNull();
        assertThat(message.getSimpleObjectValue()).isSameAs("sample object");
    }

    /**
     * {@link ObservedValue}s describing <em>observable objects</em> should be mapped to <em>observable object</em>
     * messages.
     */
    @Test
    public void shouldReturnObservableObjectMessageForObservableObjectValue() {
        Object sampleObservableObject = "sample object";
        when(objectRegistry.getIdOrFail(sampleObservableObject)).thenReturn(SAMPLE_ID);

        Value message = cut.map(sampleObservableObject, true);

        assertThat(message.getObservableObjectId()).isEqualTo(SAMPLE_ID);
        assertThat(message.getSimpleObjectValue()).isNull();
    }
}
