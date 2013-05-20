package de.saxsys.synchronizefx.stepbuilder;

import java.util.UUID;

import com.esotericsoftware.kryo.Serializer;

/**
 * Base interface for all steps that are optional and can be used for both the
 * server and the client.
 * 
 * @param <K>
 *            The return type for the step methods. This has to be the extending
 *            interface itself.
 */
interface OptionalStep<K> {

	/**
	 * Sets a custom port that differs from the default port 54263.
	 * 
	 * @param port
	 *            The port to connect if used for the client or the port on
	 *            listen when used on server side.
	 * @return The builder to provide a fluent API.
	 */
	K port(int port);

	/**
	 * Sets a custom serializer for some {@link Class}.
	 * 
	 * <p>
	 * Internally Kryo is used for the serialization and deserialization of
	 * objects. You do not need to register serializers for every class you use
	 * in your domain model but in some cases this is desirable. Registering
	 * custom serializers can be necessary when you use classes without a No-Arg
	 * constructor or to increase the performance and decrease the network
	 * usage.
	 * </p>
	 * 
	 * <p>
	 * A efficient serializer for {@link UUID} is already registered.
	 * </p>
	 * 
	 * @param clazz
	 *            The class for that the serializer should be registered.
	 * @param <T>
	 *            same as clazz.
	 * @param serializer
	 *            The serializer to register.
	 * @return The builder to provide a fluent API.
	 */
	<T> K customSerializer(final Class<T> clazz, final Serializer<T> serializer);
}