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

import de.saxsys.synchronizefx.core.metamodel.SilentChangeExecutor;
import de.saxsys.synchronizefx.core.metamodel.ValueMapper;
import de.saxsys.synchronizefx.core.metamodel.WeakObjectRegistry;
import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;

/**
 * Executes all incoming {@link ListCommand}s regardless of whether they are executable or not.
 * 
 * @author Raik Bieniek
 */
class SimpleListPropertyCommandExecutor {

    private final WeakObjectRegistry objectRegistry;

    private final SilentChangeExecutor silentChangeExecutor;

    private final ValueMapper valueMapper;

    /**
     * Initializes the instance with all its dependencies.
     * 
     * @param objectRegistry Used to resolve UUIDs to objects.
     * @param silentChangeExecutor Used to disable listeners while executing changes.
     * @param valueMapper Used to map value objects to the correct values.
     */
    public SimpleListPropertyCommandExecutor(final WeakObjectRegistry objectRegistry,
            final SilentChangeExecutor silentChangeExecutor, final ValueMapper valueMapper) {
        this.objectRegistry = objectRegistry;
        this.silentChangeExecutor = silentChangeExecutor;
        this.valueMapper = valueMapper;
    }

    /**
     * Executes an command for adding new values to the list.
     * 
     * @param command The command to execute.
     */
    public void execute(final AddToList command) {
        @SuppressWarnings("unchecked")
        final List<Object> list = (List<Object>) objectRegistry.getByIdOrFail(command.getListId());

        final Object value = valueMapper.map(command.getValue());

        silentChangeExecutor.execute(list, new Runnable() {
            @Override
            public void run() {
                list.add(command.getPosition(), value);
            }
        });
    }
}
