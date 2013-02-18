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
    private class CustomSerializers<T> {
        private final Class<T> clazz;
        private final Serializer<T> serializer;

        public CustomSerializers(final Class<T> clazz, final Serializer<T> serializer) {
            this.clazz = clazz;
            this.serializer = serializer;
        }
    }
}
