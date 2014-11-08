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

package de.saxsys.synchronizefx.core.metamodel.executors;

import javafx.beans.property.Property;

import de.saxsys.synchronizefx.core.metamodel.SilentChangeExecutor;
import de.saxsys.synchronizefx.core.metamodel.ValueMapper;
import de.saxsys.synchronizefx.core.metamodel.WeakObjectRegistry;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

/**
 * Executes all {@link Command}s that are passed to it on the users domain model without any filtering.
 * 
 * @author Raik Bieniek
 */
public class SimpleSingleValuePropertyCommandExecutor implements SingleValuePropertyCommandExecutor {

    private final WeakObjectRegistry objectRegistry;
    private final SilentChangeExecutor changeExecutor;
    private final ValueMapper valueMapper;

    /**
     * Initializes the instance with all its dependencies.
     * 
     * @param objectRegistry
     *            the registry that stores all known observable objects.
     * @param valueMapper
     *            Used to map {@link Value} massages to the actual value of a property.
     * @param changeExecutor
     *            Used to prevent generation of change commands when doing changes to the users domain model.
     */
    public SimpleSingleValuePropertyCommandExecutor(final WeakObjectRegistry objectRegistry,
            final SilentChangeExecutor changeExecutor, final ValueMapper valueMapper) {
        this.objectRegistry = objectRegistry;
        this.changeExecutor = changeExecutor;
        this.valueMapper = valueMapper;
    }

    @Override
    public void executeRemoteCommand(final SetPropertyValue command) {
        @SuppressWarnings("unchecked")
        final Property<Object> property = (Property<Object>) objectRegistry.getByIdOrFail(command.getPropertyId());

        changeExecutor.execute(property, new Runnable() {
            @Override
            public void run() {
                property.setValue(valueMapper.map(command.getValue()));
            }
        });
    }
}
