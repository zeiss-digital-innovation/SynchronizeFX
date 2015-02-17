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

import java.util.List;

import de.saxsys.synchronizefx.core.metamodel.ListPropertyMetaDataStore;
import de.saxsys.synchronizefx.core.metamodel.SilentChangeExecutor;
import de.saxsys.synchronizefx.core.metamodel.ValueMapper;
import de.saxsys.synchronizefx.core.metamodel.WeakObjectRegistry;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;

/**
 * Executes all incoming {@link ListCommand}s regardless of whether they are executable or not.
 * 
 * @author Raik Bieniek
 */
class SimpleListPropertyCommandExecutor {

    private final WeakObjectRegistry objectRegistry;

    private final SilentChangeExecutor silentChangeExecutor;

    private final ValueMapper valueMapper;

    private final ListPropertyMetaDataStore listMetaData;

    /**
     * Initializes the instance with all its dependencies.
     * 
     * @param objectRegistry
     *            Used to resolve UUIDs to objects.
     * @param silentChangeExecutor
     *            Used to disable listeners while executing changes.
     * @param valueMapper
     *            Used to map value objects to the correct values.
     * @param listVersions
     *            Used to update the local version of a list property.
     */
    public SimpleListPropertyCommandExecutor(final WeakObjectRegistry objectRegistry,
            final SilentChangeExecutor silentChangeExecutor, final ValueMapper valueMapper,
            final ListPropertyMetaDataStore listVersions) {
        this.objectRegistry = objectRegistry;
        this.silentChangeExecutor = silentChangeExecutor;
        this.valueMapper = valueMapper;
        this.listMetaData = listVersions;
    }

    /**
     * Executes an command for adding new values to a list.
     * 
     * @param command
     *            The command to execute.
     */
    public void execute(final AddToList command) {
        final List<Object> list = getListOrFail(command);

        final Object value = valueMapper.map(command.getValue());

        silentChangeExecutor.execute(list, new Runnable() {
            @Override
            public void run() {
                list.add(command.getPosition(), value);
            }
        });

        updateVersion(command);
    }

    private void updateVersion(final ListCommand command) {
        listMetaData.getMetaDataOrFail(command.getListId()).setLocalVersion(
                command.getListVersionChange().getToVersion());
    }

    /**
     * Executes a command for removing values from a list.
     * 
     * @param command
     *            The command to execute.
     */
    public void execute(final RemoveFromList command) {
        final List<Object> list = getListOrFail(command);

        silentChangeExecutor.execute(list, new Runnable() {
            @Override
            public void run() {
                final int position = command.getStartPosition();
                final int count = command.getRemoveCount();
                if (position == 0 && list.size() == count) {
                    list.clear();
                } else {
                    for (int i = 0; i < count; i++) {
                        list.remove(position);
                    }
                }
            }
        });

        updateVersion(command);
    }

    /**
     * Executes a command for replacing an element in a list.
     * 
     * @param command
     *            The command to execute.
     */
    public void execute(final ReplaceInList command) {
        final List<Object> list = getListOrFail(command);

        silentChangeExecutor.execute(list, new Runnable() {
            @Override
            public void run() {
                list.set(command.getPosition(), valueMapper.map(command.getValue()));
            }
        });

        updateVersion(command);
    }

    @SuppressWarnings("unchecked")
    private List<Object> getListOrFail(final ListCommand command) {
        return (List<Object>) objectRegistry.getByIdOrFail(command.getListId());
    }
}
