package de.saxsys.synchronizefx;

import java.util.UUID;

import com.esotericsoftware.kryo.Serializer;

import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.core.clientserver.UserCallbackClient;
import de.saxsys.synchronizefx.core.clientserver.UserCallbackServer;
import de.saxsys.synchronizefx.kryo.KryoSerializer;
import de.saxsys.synchronizefx.netty.NettyClient;
import de.saxsys.synchronizefx.netty.NettyServer;

/**
 * Creates {@link SynchronizeFxClient} and {@link SynchronizeFxServer} instances with the default implementations for
 * the network and the serialization layer.
 * 
 * This class simplifies the creation by hiding implementation specific classes for the network and serialization
 * layer from the user.
 */
public class SynchronizeFxBuilder {

    private static final int DEFAULT_PORT = 54263;

    private int port = DEFAULT_PORT;
    private KryoSerializer serializer = new KryoSerializer();

    /**
     * Sets a custom port that differs from the default port 54263.
     * 
     * @param port The port to connect if used for the client or the port on listen when used on server side.
     * @return The instance this method is called on to provide a fluent API.
     */
    public SynchronizeFxBuilder withPort(final int port) {
        this.port = port;
        return this;
    }

    /**
     * Sets a custom serializer for some {@link Class}.
     * 
     * <p>
     * Internally Kryo is used for the serialization and deserialization of objects. You do not need to register
     * serializers for every class you use in your domain model but in some cases this is desirable. Registering
     * custom serializers can be necessary when you use classes without a No-Arg constructor or to increase the
     * performance and decrease the network usage.
     * </p>
     * 
     * <p>
     * A efficient serializer for {@link UUID} is already registered.
     * </p>
     * 
     * @param clazz The class for that the serializer should be registered.
     * @param <T> same as clazz.
     * @param serializer The serializer to register.
     * @return The instance this method is called on to provide a fluent API.
     */
    public <T> SynchronizeFxBuilder withCustomSerializer(final Class<T> clazz, final Serializer<T> serializer) {
        this.serializer.registerSerializableClass(clazz, serializer);
        return this;
    }

    /**
     * Creates a server instance for serving a domain model.
     * 
     * The returned server is not automatically started yet. You have call You have to call
     * {@link SynchronizeFxServer#start()} to actually start it.
     * 
     * @param model The root object of the domain model that should be used.
     * @param callback As the SynchronizeFx framework works asynchronously, you must provide this callback instance
     *            for the framework to be able to inform you of errors than occurred. The methods in the callback are
     *            not called before you call {@link SynchronizeFxServer#start()}.
     * @return The new server instance.
     */
    public SynchronizeFxServer createServer(final Object model, final UserCallbackServer callback) {
        NettyServer netty = new NettyServer(port, serializer);
        return new SynchronizeFxServer(model, netty, callback);
    }

    /**
     * Creates a client instance to request a domain model from a server.
     * 
     * The returned client does not automatically connect. You have to call {@link SynchronizeFxClient#connect()} to
     * do so.
     * 
     * @param address The server address to connect to. This can be a DNS name or an IP address.
     * @param callback As the SynchronizeFx framework works asynchronously, you must provide this callback instance
     *            for the framework to be able to inform you when the initial transfer of the domain model from the
     *            server has completed and of errors that have occurred. The methods in the callback are not called
     *            before you call {@link SynchronizeFxClient#connect()}.
     * @return The new client instance.
     */
    public SynchronizeFxClient createClient(final String address, final UserCallbackClient callback) {
        NettyClient netty = new NettyClient(address, port, serializer);
        return new SynchronizeFxClient(netty, callback);
    }
}
