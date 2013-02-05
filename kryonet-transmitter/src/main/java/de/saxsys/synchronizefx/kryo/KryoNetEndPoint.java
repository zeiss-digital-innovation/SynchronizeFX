package de.saxsys.synchronizefx.kryo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;

/**
 * Contains common code for the client and server adapters that adapts the KryoNet library to the client server
 * interfaces of SynchronizeFX.
 * 
 * @author raik.bieniek
 */
public class KryoNetEndPoint {

    /**
     * The size of the serialisation buffer for a single object.
     * 
     * @see Client#Client(int, int)
     */
    // TODO find out good size
    protected static final int WRITE_BUFFER_SIZE = 819200;

    /**
     * The size for a whole object graph that need's to be sent at once over the network.
     */
    // TODO find out good size
    protected static final int OBJECT_BUFFER_SIZE = 819200;

    private static final Logger LOG = LoggerFactory.getLogger(KryoNetEndPoint.class);

    /**
     * How many commands are send at once. If this number is to high, some kryonet buffers will overflow.
     */
    private static final int CHUNK_SIZE = 20;

    /**
     * The instance that can be used to initialize {@link Kryo}.
     */
    protected KryoInitializer kryoInitializer = new KryoInitializer();

    /**
     * Registers a class that may be send over the network.
     * 
     * @param clazz The class that's maybe send.
     * @param serializer An optional serializer for this class. If it's null than the default serialization of kryo is
     *            used.
     * @param <T> see clazz parameter.
     */
    public <T> void registerSerializableClass(final Class<T> clazz, final Serializer<T> serializer) {
        kryoInitializer.registerSerializableClass(clazz, serializer);
    }

    /**
     * Filters incoming messages for messages that are commands that where send by the user.
     * 
     * @param object The incoming message
     * @return The list with commands that where send or null if this message wasn't a command list.
     */
    // cast is validated
    @SuppressWarnings("unchecked")
    protected List<Object> filterIncommingMessages(final Object object) {

        if (object instanceof KeepAlive) {
            return null;
        }
        if (!(object instanceof List)) {
            LOG.warn("Recived an object that was expected to be a list but wasn't.");
            return null;
        }
        return (List<Object>) object;
    }

    /**
     * Splits a big list of messages which should be send into smaller lists of messages that can be send individually.
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
            return new List[] {messages};
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
