package de.saxsys.synchronizefx.stepbuilder;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.kryo.Serializer;

import de.saxsys.synchronizefx.core.clientserver.ClientCallback;
import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.stepbuilder.SynchronizeFxStepBuilder;

/**
 * This test is used to demonstrate the use of the
 * {@link SynchronizeFxStepBuilder}.
 * 
 */
public class SynchronizeFxStepBuilderTest {
	private ClientCallback clientCallback;

	private ServerCallback serverCallback;

	private Object modelObject;

	private Serializer<Double> doubleSerializer;
	private Serializer<Integer> integerSerializer;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		clientCallback = mock(ClientCallback.class);

		serverCallback = mock(ServerCallback.class);

		modelObject = new Object();

		doubleSerializer = mock(Serializer.class);

		integerSerializer = mock(Serializer.class);
	}

	@Test
	public void testSimplestPossibleClient() {
		final SynchronizeFxClient client = SynchronizeFxStepBuilder.create().client().callback(clientCallback).build();
		Assert.assertNotNull(client);
	}


	@Test
	public void testClientWithAllPossibleValues() {
		final SynchronizeFxClient client = SynchronizeFxStepBuilder.create().client().callback(clientCallback)
				.server("192.168.0.1").port(16789).customSerializer(Double.class, doubleSerializer)
				.customSerializer(Integer.class, integerSerializer).build();
		Assert.assertNotNull(client);
	}

	@Test
	public void testSimplestPossibleServer() {
		final SynchronizeFxServer server = SynchronizeFxStepBuilder.create().server().model(modelObject)
				.callback(serverCallback).build();
		Assert.assertNotNull(server);
	}

	@Test
	public void testServerWithAllPossibleValues() {
		final SynchronizeFxServer server = SynchronizeFxStepBuilder.create().server().model(modelObject)
				.callback(serverCallback).customSerializer(Double.class, doubleSerializer).port(16789)
				.customSerializer(Integer.class, integerSerializer).build();
		Assert.assertNotNull(server);
	}
}
