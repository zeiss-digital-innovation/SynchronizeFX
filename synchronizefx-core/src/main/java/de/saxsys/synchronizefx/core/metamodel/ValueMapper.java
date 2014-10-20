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
import de.saxsys.synchronizefx.core.metamodel.commands.Command;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

/**
 * Maps the {@link Value} parts of {@link Command}s to the values they describe.
 */
class ValueMapper {

    private final WeakObjectRegistry objectRegistry;

    /**
     * Initializes an instance.
     * 
     * @param objectRegistry used to retrieve observable objects from the model registry.
     */
    public ValueMapper(final WeakObjectRegistry objectRegistry) {
        this.objectRegistry = objectRegistry;
    }

    /**
     * Maps a {@link Value} message to an {@link ObservedValue}.
     * 
     * @param message the message to map
     * @return The observed value
     * @throws ObjectToIdMappingException When the value described it he message is unknown in the {@link MetaModel}.
     */
    public ObservedValue map(final Value message) throws ObjectToIdMappingException {
        ObservedValue value;

        final UUID valueId = message.getObservableObjectId();
        if (valueId != null) {
            Object fromRegistry = objectRegistry.getByIdOrFail(valueId);
            value = new ObservedValue(fromRegistry, true);
        } else {
            value = new ObservedValue(message.getSimpleObjectValue(), false);
        }
        return value;
    }

    /**
     * Maps {@link ObservedValue}s to {@link Value} messages.
     * 
     * @param value The observed value to map.
     * @return The message.
     * @throws ObjectToIdMappingException When <code>value</code> wraps an <em>observable object</em> which has not
     *             been assigned an id yet.
     */
    public Value map(final ObservedValue value) throws ObjectToIdMappingException {
        Value msg;

        if (value.isObservable()) {
            final UUID id = objectRegistry.getIdOrFail(value.getValue());
            msg = new Value(id);
        } else {
            msg = new Value(value.getValue());
        }
        return msg;
    }
}
