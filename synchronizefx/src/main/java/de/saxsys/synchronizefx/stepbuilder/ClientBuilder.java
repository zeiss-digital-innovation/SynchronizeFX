package de.saxsys.synchronizefx.stepbuilder;

import com.esotericsoftware.kryo.Serializer;

import de.saxsys.synchronizefx.core.clientserver.ClientCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.kryo.KryoSerializer;
import de.saxsys.synchronizefx.netty.NettyClient;

/**
 * The Builder implementation for the Client.
 */
class ClientBuilder implements ClientCallbackStep, OptionalClientStep {
	private static final int DEFAULT_PORT = 54263;

	private int port = DEFAULT_PORT;
	private String address = "localhost";
	private final KryoSerializer serializer = new KryoSerializer();
	private ClientCallback callback;

	@Override
	public OptionalClientStep port(final int port) {
		this.port = port;
		return this;
	}

	@Override
	public <T> OptionalClientStep customSerializer(final Class<T> clazz, final Serializer<T> serializer) {
		this.serializer.registerSerializableClass(clazz, serializer);
		return this;
	}

	@Override
	public OptionalClientStep server(final String address) {
		this.address = address;
		return this;
	}

	@Override
	public OptionalClientStep callback(final ClientCallback callback) {
		this.callback = callback;
		return this;
	}

	@Override
	public SynchronizeFxClient build() {
		final NettyClient netty = new NettyClient(address, port, serializer);
		return new SynchronizeFxClient(netty, callback);
	}
}