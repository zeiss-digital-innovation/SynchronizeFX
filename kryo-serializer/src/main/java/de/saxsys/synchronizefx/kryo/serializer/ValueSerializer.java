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

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.commands.Value;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializes and deserializes {@link Value} messages.
 * 
 * @author Raik Bieniek
 */
public class ValueSerializer extends Serializer<Value> {

    @Override
    public void write(final Kryo kryo, final Output output, final Value object) {
        if (object.getObservableObjectId() == null) {
            if (object.getSimpleObjectValue() == null) {
                output.writeByte(0);
            } else {
                output.writeByte(1);
                kryo.writeClassAndObject(output, object.getSimpleObjectValue());
            }
        } else {
            output.writeByte(2);
            kryo.writeObject(output, object.getObservableObjectId());
        }
    }

    @Override
    public Value read(final Kryo kryo, final Input input, final Class<Value> type) {
        switch (input.readByte()) {
            case 0:
                return new Value(null);
            case 1:
                return new Value(kryo.readClassAndObject(input));
            case 2:
                return new Value(kryo.readObject(input, UUID.class));
            default:
                throw new SynchronizeFXException("Received a Value message of an unknown type.");
        }
    }
}
