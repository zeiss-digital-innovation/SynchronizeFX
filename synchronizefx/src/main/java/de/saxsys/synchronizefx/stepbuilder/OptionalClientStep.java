package de.saxsys.synchronizefx.stepbuilder;

import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;

/**
 * Optional Steps for the client and the final build step.
 */
interface OptionalClientStep extends OptionalStep<OptionalClientStep> {

	/**
	 * @param address
	 *            The server address to connect to. This can be a DNS name or an
	 *            IP address.
	 * 
	 * @return The builder to provide a fluent API.
	 */
	OptionalClientStep server(String address);

	/**
	 * Creates a client instance to request a domain model from a server.
	 * 
	 * The returned client does not automatically connect. You have to call
	 * {@link SynchronizeFxClient#connect()} to do so.
	 * 
	 * @return The new client instance.
	 */
	SynchronizeFxClient build();
}