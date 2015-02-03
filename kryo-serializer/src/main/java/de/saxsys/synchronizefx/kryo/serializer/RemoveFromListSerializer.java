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

package de.saxsys.synchronizefx.kryo.serializer;

import java.util.UUID;

import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand.ListVersionChange;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializes and deserializes {@link RemoveFromList} commands.
 * 
 * @author Raik Bieniek
 *
 */
public class RemoveFromListSerializer extends Serializer<RemoveFromList> {

    @Override
    public void write(final Kryo kryo, final Output output, final RemoveFromList input) {
        kryo.writeObject(output, input.getListId());
        kryo.writeObject(output, input.getListVersionChange().getFromVersion());
        kryo.writeObject(output, input.getListVersionChange().getToVersion());
        output.writeInt(input.getStartPosition());
        output.writeInt(input.getRemoveCount());
        output.writeInt(input.getNewSize());
    }

    @Override
    public RemoveFromList read(final Kryo kryo, final Input input, final Class<RemoveFromList> clazz) {
        return new RemoveFromList(kryo.readObject(input, UUID.class), new ListVersionChange(kryo.readObject(input,
                UUID.class), kryo.readObject(input, UUID.class)), input.readInt(), input.readInt(), input.readInt());
    }

}
