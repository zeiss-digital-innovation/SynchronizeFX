package de.saxsys.synchronizefx.stepbuilder;


/**
 * Mandatory Step to set the model for the server.
 */
interface ServerModelStep {

	/**
	 * @param model
	 *            The root object of the domain model that should be used.
	 * @return The builder to provide a fluent API.
	 */
	ServerCallbackStep model(Object model);
}