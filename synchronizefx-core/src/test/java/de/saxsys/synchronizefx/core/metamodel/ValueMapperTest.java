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

package de.saxsys.synchronizefx.core.metamodel;

import java.util.UUID;

import de.saxsys.synchronizefx.core.exceptions.ObjectToIdMappingException;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import static de.saxsys.synchronizefx.core.metamodel.commands.builders.ValueBuilder.randomSimpleObjectMessage;
import static de.saxsys.synchronizefx.core.metamodel.commands.builders.ValueBuilder.valueMessage;

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
    private MetaModel metaModel;

    @InjectMocks
    private ValueMapper cut;

    /**
     * When a message is received that describes a simple object, an {@link ObservedValue} wrapping an simple object
     * should be returned.
     */
    @Test
    public void shouldReturnSimpleObjectObservedValueForSimpleObjectMessages() {
        Value message = randomSimpleObjectMessage().build();

        ObservedValue maped = cut.map(message);

        assertThat(maped.isObservable()).isFalse();
        assertThat(maped.getValue()).isSameAs(message.getSimpleObjectValue());
    }

    /**
     * When a message is received that describes an observable object with an id that is known in the registry, an
     * {@link ObservedValue} containing that object should be returned.
     */
    @Test
    public void shouldReturnObservableObjectValueForObservableObjectMessage() {
        Object sampleObservableObject = "sample object";
        when(metaModel.getById(SAMPLE_ID)).thenReturn(sampleObservableObject);
        Value message = valueMessage().withObservableObjectId(SAMPLE_ID).build();

        ObservedValue maped = cut.map(message);

        assertThat(maped.isObservable()).isTrue();
        assertThat(maped.getValue()).isSameAs(sampleObservableObject);
    }

    /**
     * When a message is received that describes an observable object with an id that is unknown in the registry, a
     * {@link SynchronizeFXException} should be thrown.
     */
    @Test(expected = ObjectToIdMappingException.class)
    public void shouldFailWhenObservableObjectWithIdOfAnObservableObjectMessageIsNotRegistered() {
        Value message = valueMessage().withObservableObjectId(SAMPLE_ID).build();

        cut.map(message);
    }

    /**
     * {@link ObservedValue}s describing <em>simple objects</em> should be mapped to <em>simple object</em> messages.
     */
    @Test
    public void shouldReturnSimpleObjectMessageForSimpleObjectValue() {
        ObservedValue sampleSimpleObject = new ObservedValue("sample object", false);
        
        Value message = cut.map(sampleSimpleObject);

        assertThat(message.getObservableObjectId()).isNull();
        assertThat(message.getSimpleObjectValue()).isSameAs(sampleSimpleObject.getValue());
    }

    /**
     * {@link ObservedValue}s describing <em>observable objects</em> should be mapped to <em>observable object</em>
     * messages.
     */
    @Test
    public void shouldReturnObservableObjectMessageForObservableObjectValue() {
        Object sampleObservableObject = "sample object";
        when(metaModel.getId(sampleObservableObject)).thenReturn(SAMPLE_ID);

        Value message = cut.map(new ObservedValue(sampleObservableObject, true));
        
        assertThat(message.getObservableObjectId()).isEqualTo(SAMPLE_ID);
        assertThat(message.getSimpleObjectValue()).isNull();
    }

    /**
     * {@link ObservedValue}s describing <em>observable objects</em> which have not yet been assigned an id in the
     * registry should result in an exception to be thrown.
     */
    @Test(expected = ObjectToIdMappingException.class)
    public void shouldFailWhenObservableObjectHasNoIdYet() {
        cut.map(new ObservedValue("unknown observable object", true));
    }
}
