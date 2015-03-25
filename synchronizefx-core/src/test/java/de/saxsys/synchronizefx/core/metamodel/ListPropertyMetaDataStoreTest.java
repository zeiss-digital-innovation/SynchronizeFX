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

import static java.util.UUID.randomUUID;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

import de.saxsys.synchronizefx.core.exceptions.ObjectToIdMappingException;
import de.saxsys.synchronizefx.core.metamodel.ListPropertyMetaDataStore.ListPropertyMetaData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Checks if {@link ListPropertyMetaDataStore} works as expected.
 * 
 * @author Raik Bieniek
 */
@RunWith(MockitoJUnitRunner.class)
public class ListPropertyMetaDataStoreTest {

    private final ListProperty<?> list1 = new SimpleListProperty<>();
    private final ListProperty<?> list2 = new SimpleListProperty<>();
    private final UUID list1Id = randomUUID();
    private final UUID list2Id = randomUUID();
    private final ListPropertyMetaData exampleMetaData1 = new ListPropertyMetaData(randomUUID(), randomUUID());
    private final ListPropertyMetaData exampleMetaData2 = new ListPropertyMetaData(randomUUID(), randomUUID());

    @Mock
    private WeakObjectRegistry objectRegistry;

    @InjectMocks
    private ListPropertyMetaDataStore cut;

    /**
     * Wires up the mocked dependencies.
     */
    @Before
    public void setUpMocks() {
        when(objectRegistry.getByIdOrFail(list1Id)).thenReturn(list1);
        when(objectRegistry.getByIdOrFail(list2Id)).thenReturn(list2);
    }

    /**
     * The cut should be able to store and retrieve {@link ListPropertyMetaData} for a given list property.
     */
    @Test
    public void canStoreAndRetrieveMetaDataForNewListProperties() {
        cut.storeMetaDataOrFail(list1, exampleMetaData1);
        cut.storeMetaDataOrFail(list2, exampleMetaData2);

        assertThat(cut.getMetaDataOrFail(list1)).isSameAs(exampleMetaData1);
        assertThat(cut.getMetaDataOrFail(list2)).isSameAs(exampleMetaData2);
    }

    /**
     * The cut should be able to retrieve {@link ListPropertyMetaData} for an id of a list property.
     */
    @Test
    public void canRetrieveMetaDataByListId() {
        cut.storeMetaDataOrFail(list1, exampleMetaData1);

        assertThat(cut.getMetaDataOrFail(list1Id)).isSameAs(exampleMetaData1);
    }
    
    /**
     * The cut can check if meta data is already known for a list.
     */
    @Test
    public void canCheckIfMetaDataIsStoredForList() {
        cut.storeMetaDataOrFail(list1, exampleMetaData1);
        
        assertThat(cut.hasMetaDataFor(list1)).isTrue();
        assertThat(cut.hasMetaDataFor(list2)).isFalse();
    }

    /**
     * When {@link ListPropertyMetaData} should be stored for a property that already has meta data, the cut should
     * fail.
     */
    @Test(expected = ObjectToIdMappingException.class)
    public void failsWhenMetaDataShouldBeStoredForAPropertyThatAlreadyHasMetaData() {
        cut.storeMetaDataOrFail(list1, exampleMetaData1);
        cut.storeMetaDataOrFail(list1, exampleMetaData2);
    }

    /**
     * When {@link ListPropertyMetaData} should be retrieved for a property that has no known
     * {@link ListPropertyMetaDataStore}, the cut should fail.
     */
    @Test(expected = ObjectToIdMappingException.class)
    public void failsWhenMetaDataShouldBeRetrivedForAPropertyThatHasNoKnownMetaData() {
        cut.getMetaDataOrFail(list1);
    }
}
