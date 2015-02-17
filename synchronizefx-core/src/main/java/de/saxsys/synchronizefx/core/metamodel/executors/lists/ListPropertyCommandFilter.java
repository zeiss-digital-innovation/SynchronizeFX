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

import de.saxsys.synchronizefx.core.metamodel.ListPropertyMetaDataStore;
import de.saxsys.synchronizefx.core.metamodel.TemporaryReferenceKeeper;
import de.saxsys.synchronizefx.core.metamodel.WeakObjectRegistry;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

/**
 * Filters out commands from the command that are meant to be executed on other versions of a list property.
 * 
 * <p>
 * Each state of a list is identified by a version. A command that changes the list carries the version of the state on
 * which is meant to be executed. If the current confirmed state of a list has a different version the command needs to
 * be dropped. This is because there where other changes on the list which make it impossible to tell if the command can
 * safely be applied or not.
 * </p>
 * 
 * <p>
 * When a list command was dropped, the sending peer can detect this and resends the adapted command (see
 * {@link ReparingListPropertyCommandExecutor}). If the original list command carried a reference to an observable
 * object, the object needs to be cached for some time on this side. This way it is ensured that the observable object
 * is not garbage collected. The peer re-sending the command will only re-send the dropped list command, not the command
 * that created the observable object.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class ListPropertyCommandFilter {

    private final ReparingListPropertyCommandExecutor executor;
    private final TemporaryReferenceKeeper referenceKeeper;
    private final ListPropertyMetaDataStore listVersions;
    private final WeakObjectRegistry objectRegistry;

    /**
     * Initializes an instance with all its dependencies.
     * 
     * @param executor
     *            Used to execute commands that where not dropped.
     * @param referenceKeeper
     *            Used to prevent observable objects of dropped commands from being garbage collected.
     * @param listVersions
     *            Used to retrieve the current version of local list properties.
     * @param objectRegistry
     *            Used to retrieve observable objects that need to be prevented from being garbage collected.
     */
    public ListPropertyCommandFilter(final ReparingListPropertyCommandExecutor executor,
            final TemporaryReferenceKeeper referenceKeeper, final ListPropertyMetaDataStore listVersions,
            final WeakObjectRegistry objectRegistry) {
        this.executor = executor;
        this.referenceKeeper = referenceKeeper;
        this.listVersions = listVersions;
        this.objectRegistry = objectRegistry;
    }

    /**
     * Filters the passed command and passes it to the executor when it is approved.
     * 
     * @param command
     *            The command to filter
     */
    public void execute(final AddToList command) {
        if (!couldBeExecuted(command)) {
            keepReferenceIfObservable(command.getValue());
        }
    }

    /**
     * Filters the passed command and passes it to the executor when it is approved.
     * 
     * @param command
     *            The command to filter
     */
    public void execute(final RemoveFromList command) {
        couldBeExecuted(command);
    }

    /**
     * Filters the passed command and passes it to the executor when it is approved.
     * 
     * @param command
     *            The command to filter
     */
    public void execute(final ReplaceInList command) {
        if (!couldBeExecuted(command)) {
            keepReferenceIfObservable(command.getValue());
        }
    }

    private boolean couldBeExecuted(final ListCommand command) {
        final UUID listVersion = listVersions.getMetaDataOrFail(command.getListId()).getApprovedVersion();
        final UUID commandFromVersion = command.getListVersionChange().getFromVersion();

        if (commandFromVersion.equals(listVersion)) {
            executor.execute(command);
            return true;
        }

        return false;
    }

    private void keepReferenceIfObservable(final Value value) {
        if (value.isSimpleObject()) {
            return;
        }
        referenceKeeper.keepReferenceTo(objectRegistry.getByIdOrFail(value.getObservableObjectId()));
    }
}
