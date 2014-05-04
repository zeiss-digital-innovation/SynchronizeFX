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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test if {@link WeakObjectRegistry} works as expected.
 */
public class WeakObjectRegistryTest {

    private WeakObjectRegistry cut = new WeakObjectRegistry();

    /**
     * The {@link Object} registered with an {@link UUID} should be returned when queried for this {@link UUID}.
     */
    @Test
    public void shouldStoreAndReturnObjectToIdMappings() {
        Object exampleObject = "an exemplary object to store in the registry";
        UUID exampleId = UUID.randomUUID();
    
        cut.registerObject(exampleObject, exampleId);
    
        Optional<UUID> queriedId = cut.getId(exampleObject);
        UUID queriedIdOrFailed = cut.getIdOrFail(exampleObject);
    
        assertThat(queriedId.isPresent()).isTrue();
        assertThat(queriedId.get()).isEqualTo(exampleId);
        assertThat(queriedId.get()).isSameAs(queriedIdOrFailed);
    }

    /**
     *  When queried for an registered {@link Object} the {@link WeakObjectRegistry} should return its {@link UUID}.
     */
    @Test
    public void shouldStoreAndReturnIdToObjectMappings() {
        Object exampleObject = "an exemplary object to store in the registry";
        UUID exampleId = UUID.randomUUID();
    
        cut.registerObject(exampleObject, exampleId);
        
        Optional<Object> queriedObject = cut.getById(exampleId);
        Object queriedObjectOrFailed = cut.getByIdOrFail(exampleId);
        
        assertThat(queriedObject.isPresent()).isTrue();
        assertThat(queriedObject.get()).isSameAs(exampleObject);
        assertThat(queriedObject.get()).isSameAs(queriedObjectOrFailed);
    }

    /**
     * When the {@link UUID} for an {@link Object} is requested that was not registered, an empty {@link Optional}
     * should be returned.
     */
    @Test
    public void shouldReturnEmptyOptionalForUnknownObject() {
        Object unknownObject = "example unknown object";

        Optional<UUID> id = cut.getId(unknownObject);

        assertThat(id.isPresent()).isFalse();
    }

    /**
     * When an {@link Object} for a {@link UUID} is requested that was not registered, an empty {@link Optional} should
     * be returned.
     */
    @Test
    public void shouldReturnEmptyOptionalForUnknownId() {
        UUID unknownId = UUID.randomUUID();

        Optional<Object> object = cut.getById(unknownId);

        assertThat(object.isPresent()).isFalse();
    }

    /**
     * When {@link WeakObjectRegistry#getIdOrFail(Object)} is called with an unregistered object, an exception should be
     * thrown.
     */
    @Test(expected = ObjectToIdMappingException.class)
    public void shouldFailWhenIdForAnUnregisteredObjectIsRequested() {
        Object unknownObject = "example unknown object";
        
        cut.getIdOrFail(unknownObject);
    }

    /**
     * When {@link WeakObjectRegistry#getByIdOrFail(UUID)} with an unknown id is called, an execption should be thrown.
     */
    @Test(expected = ObjectToIdMappingException.class)
    public void shouldFailWhenObjectForAnUnknownIdIsRequested() {
        UUID unknownId = UUID.randomUUID();
        
        cut.getByIdOrFail(unknownId);
    }

    /**
     * {@link WeakObjectRegistry#registerIfUnknown(Object)} should return the {@link UUID} of an {@link Object} that is
     * already registered.
     */
    @Test
    public void shouldReturnExistingIdForKnownObject() {
        Object exampleObject = "an exemplary object to store in the registry";
        UUID exampleId = UUID.randomUUID();

        cut.registerObject(exampleObject, exampleId);

        UUID queriedId = cut.registerIfUnknown(exampleObject);

        assertThat(queriedId).isEqualTo(exampleId);
    }

    /**
     * {@link WeakObjectRegistry#registerIfUnknown(Object)} should register an object with a new {@link UUID} when the
     * {@link Object} passed is unknown.
     */
    @Test
    public void shouldRegisterUnknownObjectWithNewId() {
        Object newObject = "example object that is not stored in the registry yet";

        UUID queriedId = cut.registerIfUnknown(newObject);
        Optional<Object> queriedObject = cut.getById(queriedId);

        assertThat(queriedId).isNotNull();
        assertThat(queriedObject.isPresent()).isTrue();
        assertThat(queriedObject.get()).isSameAs(newObject);
    }
}
