package de.saxsys.synchronizefx.kryo.serializers;

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
    public UUID create(final Kryo kryo, final Input input, final Class<UUID> type) {
        return new UUID(input.readLong(), input.readLong());
    }

    @Override
    public void write(final Kryo kryo, final Output output, final UUID object) {
        output.writeLong(object.getMostSignificantBits());
        output.writeLong(object.getLeastSignificantBits());
    }
}