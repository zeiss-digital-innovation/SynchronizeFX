package de.saxsys.synchronizefx.stepbuilder;

import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;

/**
 * Optional Steps for the server and the final build step.
 */
interface OptionalServerStep extends OptionalStep<OptionalServerStep> {

	/**
	 * Creates a server instance for serving a domain model.
	 * 
	 * The returned server is not automatically started yet. You have call You
	 * have to call {@link SynchronizeFxServer#start()} to actually start it.
	 * 
	 * @return The new server instance.
	 */
	SynchronizeFxServer build();
}