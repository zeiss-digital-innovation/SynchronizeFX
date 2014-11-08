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

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import javafx.beans.property.Property;

import de.saxsys.synchronizefx.core.metamodel.WeakObjectRegistry;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceIdentityMap;

/**
 * Executes incoming change events on single value properties.
 * 
 * This executor handles the following change command:
 * 
 * <ul>
 * <li>{@link SetPropertyValue}</li>
 * </ul>
 * 
 * @author Raik Bieniek
 */
public class SingleValuePropertyCommandExecutor {

    private final WeakObjectRegistry objectRegistry;

    // Apache commons collections are not generic
    @SuppressWarnings("unchecked")
    private Map<Property<Object>, Queue<UUID>> propertyToChangeLog = new ReferenceIdentityMap(
            AbstractReferenceMap.WEAK, AbstractReferenceMap.HARD);

    /**
     * Initializes the instance with all its dependencies.
     * 
     * @param objectRegistry
     *            the registry that stores all known observable objects.
     */
    public SingleValuePropertyCommandExecutor(final WeakObjectRegistry objectRegistry) {
        this.objectRegistry = objectRegistry;
    }

    /**
     * Logs a {@link SetPropertyValue} command that was send locally to the server.
     * 
     * @param command
     *            The command to log.
     */
    void logLocalCommand(final SetPropertyValue command) {
        getLog(command).offer(command.getCommandId());
    }

    /**
     * Executes an command that was received from an other peer if appropriate.
     * 
     * @param command
     *            The received event.
     */
    public void executeRemoteCommand(final SetPropertyValue command) {
        @SuppressWarnings("unchecked")
        final Property<Object> property = (Property<Object>) objectRegistry.getByIdOrFail(command.getPropertyId());
        final Queue<UUID> localCommands = propertyToChangeLog.get(property);
        
        if (!(localCommands == null || localCommands.isEmpty())) {
            if (localCommands.peek().equals(command.getCommandId())) {
                localCommands.poll();
            }
            return;
        }

        if (command.getValue().isSimpleObject()) {
            property.setValue(command.getValue().getSimpleObjectValue());
        } else {
            property.setValue(objectRegistry.getByIdOrFail(command.getValue().getObservableObjectId()));
        }
    }

    private Queue<UUID> getLog(final SetPropertyValue command) {
        @SuppressWarnings("unchecked")
        final Property<Object> prop = (Property<Object>) objectRegistry.getByIdOrFail(command.getPropertyId());

        if (propertyToChangeLog.containsKey(prop)) {
            return propertyToChangeLog.get(prop);
        }

        final Queue<UUID> log = new LinkedList<>();
        propertyToChangeLog.put(prop, log);
        return log;
    }
}
