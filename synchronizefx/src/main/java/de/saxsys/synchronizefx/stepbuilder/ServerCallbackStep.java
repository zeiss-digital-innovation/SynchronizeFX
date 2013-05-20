package de.saxsys.synchronizefx.stepbuilder;

import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;

/**
 * Mandatory Step to set the callback for the server.
 */
interface ServerCallbackStep {

	/**
	 * @param callback
	 *            As the SynchronizeFx framework works asynchronously, you must
	 *            provide this callback instance for the framework to be able to
	 *            inform you of errors than occurred. The methods in the
	 *            callback are not called before you call
	 *            {@link SynchronizeFxServer#start()}.
	 * @return The builder to provide a fluent API.
	 */
	OptionalServerStep callback(ServerCallback callback);
}