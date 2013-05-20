package de.saxsys.synchronizefx.stepbuilder;

import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;

/**
 * The initial step to choose whether to create a server or a client instance.
 */
interface InitialStep {

	/**
	 * Creates a Builder to create a {@link SynchronizeFxServer}.
	 * 
	 * @return The builder to provide a fluent API.
	 */
	ServerModelStep server();

	/**
	 * Creates a Builder to create a {@link SynchronizeFxClient}.
	 * 
	 * @return The builder to provide a fluent API.
	 */
	ClientCallbackStep client();
}