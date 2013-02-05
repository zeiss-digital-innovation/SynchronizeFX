package de.saxsys.synchronizefx.kryo.serializers;

import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializes and deserializes {@link List} instances.
 * 
 * This class can serialize all implementations of {@link List} but they are all deserialized to {@link LinkedList}.
 * 
 * @author raik.bieniek
 * 
 */
public final class ListSerializer extends Serializer<List<?>> {
    @Override
    public List<?> create(final Kryo kryo, final Input input, final Class<List<?>> type) {
        int count = input.readInt();
        List<Object> output = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            output.add(kryo.readClassAndObject(input));
        }
        return output;
    }

    @Override
    public void write(final Kryo kryo, final Output output, final List<?> object) {
        output.writeInt(object.size());
        for (Object part : object) {
            kryo.writeClassAndObject(output, part);
        }
    }
}