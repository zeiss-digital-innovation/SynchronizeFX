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

package de.saxsys.synchronizefx.core.metamodel.executors.lists;

import de.saxsys.synchronizefx.core.metamodel.ListPropertyMetaDataStore;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

/**
 * A pass-through executor that updates the approved version of list properties.
 * 
 * <p>
 * When a command is received its <code>to</code> version is the new approved version of the list without any further
 * conditions. The command is than passed to an other command executor for further handling.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class SimpleListPropertyCommandApprover implements ListPropertyCommandExecutor {

    private ListPropertyCommandExecutor executor;
    private ListPropertyMetaDataStore metaData;

    /**
     * Initializes an instance with all its dependencies.
     * 
     * @param executor
     *            The next executor stage.
     * @param metaData
     *            Used to update the approved version of list properties.
     */
    public SimpleListPropertyCommandApprover(final ListPropertyCommandExecutor executor,
            final ListPropertyMetaDataStore metaData) {
        this.executor = executor;
        this.metaData = metaData;

    }

    @Override
    public void execute(final AddToList command) {
        approveVersion(command);
        executor.execute(command);
    }

    @Override
    public void execute(final RemoveFromList command) {
        approveVersion(command);
        executor.execute(command);
    }

    @Override
    public void execute(final ReplaceInList command) {
        approveVersion(command);
        executor.execute(command);
    }

    private void approveVersion(final ListCommand command) {
        metaData.getMetaDataOrFail(command.getListId()).setApprovedVersion(
                command.getListVersionChange().getToVersion());
    }
}
