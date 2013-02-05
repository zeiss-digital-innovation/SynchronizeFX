package de.saxsys.synchronizefx.kryo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryonet.EndPoint;

import de.saxsys.synchronizefx.core.metamodel.commands.AddToList;
import de.saxsys.synchronizefx.core.metamodel.commands.ClearReferences;
import de.saxsys.synchronizefx.core.metamodel.commands.CreateObservableObject;
import de.saxsys.synchronizefx.core.metamodel.commands.PutToMap;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromList;
import de.saxsys.synchronizefx.core.metamodel.commands.RemoveFromMap;
import de.saxsys.synchronizefx.core.metamodel.commands.SetPropertyValue;
import de.saxsys.synchronizefx.core.metamodel.commands.SetRootElement;
import de.saxsys.synchronizefx.kryo.serializers.ListSerializer;
import de.saxsys.synchronizefx.kryo.serializers.MapSerializer;
import de.saxsys.synchronizefx.kryo.serializers.UUIDSerializer;

/**
 * Registers the classes that may be serialized in the internal {@link Kryo} of an {@link EndPoint}.
 * 
 * @author raik.bieniek
 */
class KryoInitializer {
    private final List<ClassSerializer> classesToSerialize = new LinkedList<>();

    /**
     * @see KryoNetMessageHandler#registerSerializableClass(Class, Serializer)
     * @param <T> see {@link KryoNetMessageHandler#registerSerializableClass(Class, Serializer)}
     * @param clazz see {@link KryoNetMessageHandler#registerSerializableClass(Class, Serializer)}
     * @param serializer see {@link KryoNetMessageHandler#registerSerializableClass(Class, Serializer)}
     */
    <T> void registerSerializableClass(final Class<T> clazz, final Serializer<T> serializer) {
        classesToSerialize.add(new ClassSerializer(clazz, serializer));
    }

    /**
     * Set's up the internal {@link Kryo} serializer for a KryoNet {@link EndPoint}.
     * 
     * That means that all classes are registered that may get send over the network.
     * 
     * @param connection The connection that's internal {@link Kryo} should be set up.
     */
    void setupKryo(final EndPoint connection) {
        final Kryo kryo = connection.getKryo();
        kryo.register(HashMap.class, new MapSerializer());

        Serializer<List<?>> listSerialzer = new ListSerializer();
        kryo.register(LinkedList.class, listSerialzer);
        kryo.register(SubList.class, listSerialzer);

        kryo.register(UUID.class, new UUIDSerializer());

        kryo.register(SetPropertyValue.class);
        kryo.register(AddToList.class);
        kryo.register(RemoveFromList.class);
        kryo.register(CreateObservableObject.class);
        kryo.register(RemoveFromMap.class);
        kryo.register(PutToMap.class);
        kryo.register(SetRootElement.class);
        kryo.register(ClearReferences.class);

        for (final ClassSerializer serializer : classesToSerialize) {
            if (serializer.serializer != null) {
                kryo.register(serializer.clazz, serializer.serializer);
            } else {
                kryo.register(serializer.clazz);
            }
        }
    }

    /**
     * A simple data structure to save a class and it's serializer together.
     * 
     * @author raik.bieniek
     * 
     */
    private static class ClassSerializer {
        private final Class<?> clazz;
        private final Serializer<?> serializer;

        public ClassSerializer(final Class<?> clazz, final Serializer<?> serializer) {
            this.clazz = clazz;
            this.serializer = serializer;
        }
    }

}
