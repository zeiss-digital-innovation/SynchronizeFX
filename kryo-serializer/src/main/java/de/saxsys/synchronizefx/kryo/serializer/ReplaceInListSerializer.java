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

import de.saxsys.synchronizefx.core.metamodel.commands.ReplaceInList;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializes and deserializes {@link ReplaceInList} commands.
 * 
 * @author Raik Bieniek
 */
public class ReplaceInListSerializer extends Serializer<ReplaceInList> {

    @Override
    public void write(final Kryo kryo, final Output output, final ReplaceInList input) {
        kryo.writeObject(output, input.getListId());
        output.writeInt(input.getListVersion());
        kryo.writeObject(output, input.getValue());
        output.writeInt(input.getPosition());
    }

    @Override
    public ReplaceInList read(final Kryo kryo, final Input input, final Class<ReplaceInList> type) {
        return new ReplaceInList(kryo.readObject(input, UUID.class), input.readInt(), kryo.readObject(input,
                Value.class), input.readInt());
    }
}
