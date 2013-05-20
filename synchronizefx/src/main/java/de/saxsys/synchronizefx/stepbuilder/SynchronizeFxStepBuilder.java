package de.saxsys.synchronizefx.stepbuilder;



/**
 * This is a Step Builder Pattern implementation to create client and server
 * instances for SynchronizeFX.
 * 
 * @author manuel.mauky
 * 
 */
public class SynchronizeFxStepBuilder {
	private SynchronizeFxStepBuilder() {
	}

	/**
	 * Initial step to create the builder instance.
	 * 
	 * @return the builder.
	 */
	public static InitialStep create() {
		return new Builder();
	}

	private static class Builder implements InitialStep {
		@Override
		public ServerModelStep server() {
			return new ServerBuilder();
		}

		@Override
		public ClientCallbackStep client() {
			return new ClientBuilder();
		}
	}
}
