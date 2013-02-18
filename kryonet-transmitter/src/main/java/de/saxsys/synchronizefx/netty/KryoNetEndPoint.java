package de.saxsys.synchronizefx.netty;

import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

import de.saxsys.synchronizefx.kryo.KryoSerializer;

/**
 * Contains common code for the client and server adapters that adapts the KryoNet library to the client server
 * interfaces of SynchronizeFX.
 * 
 * @author raik.bieniek
 */
public class KryoNetEndPoint {

    /**
     * How many commands are send at once. If this number is to high, some kryonet buffers will overflow.
     */
    private static final int CHUNK_SIZE = 20;

    /**
     * The instance that can be used to initialize {@link Kryo}.
     */
    protected KryoSerializer kryo = new KryoSerializer();

    /**
     * Registers a class that may be send over the network.
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
     * Splits a big list of messages which should be send into smaller lists of messages that can be send
     * individually.
     * 
     * The last list in the returned array has between 0 and {@link KryoNetMessageHandler#CHUNK_SIZE} elements. All
     * other lists have {@link KryoNetMessageHandler#CHUNK_SIZE} elements.
     * 
     * @param messages the big list that be split.
     * @return the parts into which the list was split.
     */
    @SuppressWarnings("unchecked")
    protected List<Object>[] chunk(final List<Object> messages) {
        int messageCount = messages.size();
        int chunkCount = messageCount / CHUNK_SIZE + (messageCount % CHUNK_SIZE == 0 ? 0 : 1);
        if (chunkCount == 1) {
            return new List[] {messages };
        }
        List<Object>[] chunks = new List[chunkCount];
        for (int i = 0; i < chunkCount; i++) {
            int start = i * CHUNK_SIZE;
            int end;
            if (i == chunkCount - 1) {
                end = start + messageCount % CHUNK_SIZE;
            } else {
                end = start + CHUNK_SIZE;
            }
            chunks[i] = new SubList<Object>(start, end, messages);
        }
        return chunks;
    }
}
