package de.saxsys.synchronizefx.kryo.serializers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializes and deserializes {@link Map} instances.
 * 
 * This class can serialize all implementations of {@link Map} but they are all deserialized to {@link HashMap}.
 * 
 * @author raik.bieniek
 * 
 */
public final class MapSerializer extends Serializer<Map<?, ?>> {
    @Override
    public Map<?, ?> create(final Kryo kryo, final Input input, final Class<Map<?, ?>> type) {
        final int size = input.readInt();

        final HashMap<Object, Object> map = new HashMap<>((int) (size / 0.75) + 1);
        for (int i = 0; i < size; i++) {
            final Object key = kryo.readClassAndObject(input);
            final Object value = kryo.readClassAndObject(input);
            map.put(key, value);
        }
        return map;
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Map<?, ?> object) {
        // casting ? to Object doesn't fail
        @SuppressWarnings("unchecked")
        final Map<Object, Object> map = (Map<Object, Object>) object;
        output.writeInt(map.size());

        final Iterator<Entry<Object, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<Object, Object> pair = it.next();
            kryo.writeClassAndObject(output, pair.getKey());
            kryo.writeClassAndObject(output, pair.getValue());
        }
    }
}