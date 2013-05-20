package de.saxsys.synchronizefx.stepbuilder;

import de.saxsys.synchronizefx.core.clientserver.ClientCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;

/**
 * Mandatory step to set the callback for the client.
 */
interface ClientCallbackStep {

	/**
	 * @param callback
	 *            As the SynchronizeFx framework works asynchronously, you must
	 *            provide this callback instance for the framework to be able to
	 *            inform you when the initial transfer of the domain model from
	 *            the server has completed and of errors that have occurred. The
	 *            methods in the callback are not called before you call
	 *            {@link SynchronizeFxClient#connect()}.
	 * @return The builder to provide a fluent API.
	 */
	OptionalClientStep callback(ClientCallback callback);
}