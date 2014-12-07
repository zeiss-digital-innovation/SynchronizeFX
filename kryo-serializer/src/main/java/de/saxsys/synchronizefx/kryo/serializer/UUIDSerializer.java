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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializes and deserializes {@link UUID} instances.
 * 
 * @author raik.bieniek
 * 
 */
public final class UUIDSerializer extends Serializer<UUID> {
    @Override
    public UUID read(final Kryo kryo, final Input input, final Class<UUID> type) {
        return new UUID(input.readLong(), input.readLong());
    }

    @Override
    public void write(final Kryo kryo, final Output output, final UUID object) {
        output.writeLong(object.getMostSignificantBits());
        output.writeLong(object.getLeastSignificantBits());
    }
}