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

package de.saxsys.synchronizefx.kryo;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

/**
 * Initializes {@link Kryo} instances with all the Serializers needed for SynchronizeFX and the one the user
 * registerend.
 * 
 * A new {@link Kryo} instance is created for each {@link Thread} to ensure thread-safety.
 */
final class KryoInitializer extends ThreadLocal<Kryo> {

    private List<CustomSerializers<?>> customSerializers = new LinkedList<>();

    @Override
    protected Kryo initialValue() {
        Kryo kryo = new Kryo();
        kryo.register(UUID.class, new UUIDSerializer());

        synchronized (customSerializers) {
            for (CustomSerializers<?> serializer : customSerializers) {
                if (serializer.serializer != null) {
                    kryo.register(serializer.clazz, serializer.serializer);
                } else {
                    kryo.register(serializer.clazz);
                }
            }
        }
        return kryo;
    }

    /**
     * See {@link KryoSerializer#registerSerializableClass(Class, Serializer)}.
     * 
     * @param clazz see {@link KryoSerializer#registerSerializableClass(Class, Serializer)}.
     * @param serializer see {@link KryoSerializer#registerSerializableClass(Class, Serializer)}.
     * @param <T> see {@link KryoSerializer#registerSerializableClass(Class, Serializer)}.
     * @see KryoSerializer#registerSerializableClass(Class, Serializer)
     */
    <T> void registerSerializableClass(final Class<T> clazz, final Serializer<T> serializer) {
        synchronized (customSerializers) {
            customSerializers.add(new CustomSerializers<>(clazz, serializer));
        }
    }

    /**
     * A simple storage class for {@link Class} that should be serialized and the {@link Serializer} to use.
     * 
     * @param <T> The class to serialize.
     */
    private static class CustomSerializers<T> {
        private final Class<T> clazz;
        private final Serializer<T> serializer;

        public CustomSerializers(final Class<T> clazz, final Serializer<T> serializer) {
            this.clazz = clazz;
            this.serializer = serializer;
        }
    }
}
