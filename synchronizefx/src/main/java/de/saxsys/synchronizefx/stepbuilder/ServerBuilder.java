package de.saxsys.synchronizefx.stepbuilder;

import com.esotericsoftware.kryo.Serializer;

import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.kryo.KryoSerializer;
import de.saxsys.synchronizefx.netty.NettyServer;

/**
 * The Builder implementation for the Server.
 */
class ServerBuilder implements ServerModelStep, ServerCallbackStep, OptionalServerStep {
	private static final int DEFAULT_PORT = 54263;

	private int port = DEFAULT_PORT;
	private final KryoSerializer serializer = new KryoSerializer();
	private ServerCallback callback;
	private Object model;

	@Override
	public OptionalServerStep port(final int port) {
		this.port = port;
		return this;
	}

	@Override
	public <T> OptionalServerStep customSerializer(final Class<T> clazz, final Serializer<T> serializer) {
		this.serializer.registerSerializableClass(clazz, serializer);
		return this;
	}

	@Override
	public OptionalServerStep callback(final ServerCallback callback) {
		this.callback = callback;
		return this;
	}

	@Override
	public ServerCallbackStep model(final Object model) {
		this.model = model;
		return this;
	}

	@Override
	public SynchronizeFxServer build() {
		final NettyServer netty = new NettyServer(port, serializer);
		return new SynchronizeFxServer(model, netty, callback);
	}
}