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

package de.saxsys.synchronizefx.kryo.serializer;

import java.util.UUID;

import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand.ListVersionChange;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializes and deserializes {@link AddToList} commands.
 * 
 * @author Raik Bieniek
 */
public class AddToListSerializer extends Serializer<AddToList> {

    @Override
    public void write(final Kryo kryo, final Output output, final AddToList object) {
        kryo.writeObject(output, object.getListId());
        kryo.writeObject(output, object.getListVersionChange().getFromVersion());
        kryo.writeObject(output, object.getListVersionChange().getToVersion());
        kryo.writeObject(output, object.getValue());
        output.writeInt(object.getPosition());
        output.writeInt(object.getNewSize());
    }

    @Override
    public AddToList read(final Kryo kryo, final Input input, final Class<AddToList> type) {
        return new AddToList(kryo.readObject(input, UUID.class), new ListVersionChange(kryo.readObject(input,
                UUID.class), kryo.readObject(input, UUID.class)), kryo.readObject(input, Value.class), input.readInt(),
                input.readInt());
    }
}
