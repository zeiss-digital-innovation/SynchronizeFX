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

import de.saxsys.synchronizefx.core.metamodel.ListVersions;
import de.saxsys.synchronizefx.core.metamodel.TemporaryReferenceKeeper;
import de.saxsys.synchronizefx.core.metamodel.WeakObjectRegistry;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Checks if {@link ListPropertyCommandFilter} works as expected.
 * 
 * @author Raik Bieniek
 */
@RunWith(MockitoJUnitRunner.class)
public class ListPropertyCommandFilterTest {

    private static final UUID EXEMPLARY_LIST_ID = UUID.randomUUID();
    private static final UUID EXEMPLARY_OBSERVABLE_OBJECT_ID_1 = UUID.randomUUID();
    private static final UUID EXEMPLARY_OBSERVABLE_OBJECT_ID_2 = UUID.randomUUID();

    private static final Value EXEMPLARY_VALUE_FOR_ADD = new Value(EXEMPLARY_OBSERVABLE_OBJECT_ID_1);
    private static final Value EXEMPLARY_VALUE_FOR_REPLACE = new Value(EXEMPLARY_OBSERVABLE_OBJECT_ID_2);

    private final AddToList exemplaryAddToListCommand = new AddToList(EXEMPLARY_LIST_ID, 15, EXEMPLARY_VALUE_FOR_ADD, //
            8);
    private final RemoveFromList exemplaryRemoveFromListCommand = new RemoveFromList(EXEMPLARY_LIST_ID, 95, 50, 7);
    private final ReplaceInList exemlaryReplaceInListCommand = new ReplaceInList(EXEMPLARY_LIST_ID, 31,
            EXEMPLARY_VALUE_FOR_REPLACE, 6);

    @Mock
    private ReparingListPropertyCommandExecutor executor;

    @Mock
    private TemporaryReferenceKeeper referenceKeeper;

    @Mock
    private ListVersions listVersions;

    @Mock
    private WeakObjectRegistry objectRegistry;

    @InjectMocks
    private ListPropertyCommandFilter cut;

    /**
     * Commands that can be applied on the current version of the local list property are executed.
     */
    @Test
    public void executesCommandsThatsVersionMatchesTheVersionOfTheLocalListProperty() {
        when(listVersions.getVersionOrFail(EXEMPLARY_LIST_ID)).thenReturn(15).thenReturn(95).thenReturn(31);

        cut.execute(exemplaryAddToListCommand);
        cut.execute(exemplaryRemoveFromListCommand);
        cut.execute(exemlaryReplaceInListCommand);

        verify(executor).execute(exemplaryAddToListCommand);
        verify(executor).execute(exemplaryRemoveFromListCommand);
        verify(executor).execute(exemlaryReplaceInListCommand);

        verifyNoMoreInteractions(referenceKeeper);
    }

    /**
     * Commands that can not be applied on the current version of the local list property are dropped.
     */
    @Test
    public void droppsCommandsThatsVersionDifferFromTheVersionOfTheLocalListProperty() {
        when(listVersions.getVersionOrFail(EXEMPLARY_LIST_ID)).thenReturn(16).thenReturn(-80).thenReturn(10);

        cut.execute(exemplaryAddToListCommand);
        cut.execute(exemplaryRemoveFromListCommand);
        cut.execute(exemlaryReplaceInListCommand);

        verifyNoMoreInteractions(executor);
    }

    /**
     * If dropped {@link AddToList} and {@link ReplaceInList} commands contain references to observable objects they are
     * cached.
     * 
     * <p>
     * This prevents them from being garbage collected before the remote peer send the repaired version of the command.
     * </p>
     */
    @Test
    public void cachesObservableObjectsOfAddToListAndReplaceInListCommands() {
        when(listVersions.getVersionOrFail(EXEMPLARY_LIST_ID)).thenReturn(16).thenReturn(10);

        final Object observableObjectForAdd = new Object();
        final Object observableObjectForReplace = new Object();

        when(objectRegistry.getByIdOrFail(EXEMPLARY_OBSERVABLE_OBJECT_ID_1)).thenReturn(observableObjectForAdd);
        when(objectRegistry.getByIdOrFail(EXEMPLARY_OBSERVABLE_OBJECT_ID_2)).thenReturn(observableObjectForReplace);

        cut.execute(exemplaryAddToListCommand);
        cut.execute(exemlaryReplaceInListCommand);

        verify(referenceKeeper).keepReferenceTo(observableObjectForAdd);
        verify(referenceKeeper).keepReferenceTo(observableObjectForReplace);
    }

    /**
     * If dropped {@link AddToList} and {@link ReplaceInList} commands contain a simple objects they are not cached.
     * 
     * <p>
     * Simple objects will be resent with the repaired version of the command. It is therefore OK when they are garbage
     * collected.
     * </p>
     */
    @Test
    public void doesntCacheSimpleObjectsOfAddToListAndReplaceInListCommands() {
        when(listVersions.getVersionOrFail(EXEMPLARY_LIST_ID)).thenReturn(1).thenReturn(1);

        final AddToList addCommandWithSimpleObject = new AddToList(EXEMPLARY_LIST_ID, 0,
                new Value("some simple object"), 6);
        final ReplaceInList replaceCommandWithSimpleObject = new ReplaceInList(EXEMPLARY_LIST_ID, 0, new Value(
                "other simple object"), 7);

        cut.execute(addCommandWithSimpleObject);
        cut.execute(replaceCommandWithSimpleObject);

        verifyNoMoreInteractions(referenceKeeper, objectRegistry, executor);
    }
}