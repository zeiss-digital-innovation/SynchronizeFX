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

import de.saxsys.synchronizefx.core.exceptions.ObjectToIdMappingException;
import de.saxsys.synchronizefx.core.metamodel.ListPropertyMetaDataStore.ListPropertyMetaData;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks if {@link ListPropertyMetaDataStore} works as expected.
 * 
 * @author Raik Bieniek
 */
public class ListPropertyMetaDataStoreTest {

    private final ListPropertyMetaDataStore cut = new ListPropertyMetaDataStore();

    private final UUID list1 = randomUUID();
    private final UUID list2 = randomUUID();
    private final ListPropertyMetaData exampleMetaData1 = new ListPropertyMetaData(randomUUID(), randomUUID());
    private final ListPropertyMetaData exampleMetaData2 = new ListPropertyMetaData(randomUUID(), randomUUID());

    /**
     * The cut should be able to store and retrieve {@link ListPropertyMetaData} for a given list property.
     */
    @Test
    public void canStoreAndRetriveMetaDataForNewListProperties() {
        cut.storeMetaDataOrFail(list1, exampleMetaData1);
        cut.storeMetaDataOrFail(list2, exampleMetaData2);

        assertThat(cut.getMetaDataOrFail(list1)).isSameAs(exampleMetaData1);
        assertThat(cut.getMetaDataOrFail(list2)).isSameAs(exampleMetaData2);
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
