package de.saxsys.synchronizefx.kryo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializes SynchronizeFX messages by using the Kryo library.
 * 
 */
public class KryoSerializer implements de.saxsys.synchronizefx.core.clientserver.Serializer {
    private KryoInitializer kryo = new KryoInitializer();

    /**
     * Registers a class that may be send over the network.
     * 
     * Use this method only before the first invocation of either {@link KryoSerializer#serialize(List)} or
     * {@link KryoSerializer#deserialize(byte[])}. If you invoke it after these methods it is not guaranteed that the
     * {@link Kryo} used by these methods will actually use your serializers.
     * 
     * @param clazz The class that's maybe send.
     * @param serializer An optional serializer for this class. If it's null than the default serialization of kryo
     *            is used.
     * @param <T> see clazz parameter.
     */
    public <T> void registerSerializableClass(final Class<T> clazz, final Serializer<T> serializer) {
        kryo.registerSerializableClass(clazz, serializer);
    }

    /**
     * Serializes SyncronizeFX messages to bytes.
     * 
     * To deserialize them, use {@link KryoSerializer#deserialize(byte[])}. This method is thread safe.
     * 
     * @param messages The messages to serialize.
     * @return The messages in serialized form.
     */
    @Override
    public byte[] serialize(final List<Object> messages) {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final Output output = new Output(outStream);
        kryo.get().writeObject(output, messages);
        output.close();
        byte[] bytes = outStream.toByteArray();
        try {
            outStream.close();
        } catch (IOException e) {
            // This will not happen, as there is no real I/O. Everything happens in RAM.
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * Deserializes {@code byte []} to SynchronizeFX messages.
     * 
     * This method is thread save.
     * 
     * @param messages The serialized form of SynchronizeFX messages that was created by
     *            {@link KryoSerializer#serialize(List)}.
     * @return The original messages.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Object> deserialize(final byte[] messages) {
        return kryo.get().readObject(new Input(messages), LinkedList.class);
    }
}
