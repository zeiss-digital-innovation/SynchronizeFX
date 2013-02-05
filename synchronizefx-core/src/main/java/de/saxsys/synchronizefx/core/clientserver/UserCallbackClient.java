package de.saxsys.synchronizefx.core.clientserver;

import de.saxsys.synchronizefx.core.SynchronizeFXException;

/**
 * This interface is used to inform the user about different events like errors that occurred in the library.
 * 
 * @author raik.bieniek
 * 
 */
public interface UserCallbackClient {

    /**
     * Called when the initial transfer of the model has completed.
     * 
     * @param model The root object of the synchronized object tree.
     */
    void modelReady(Object model);

    /**
     * Called when an error occurred in the synchronization code.
     * 
     * This includes errors occurred in the {@link MessageTransferClient} implementation.
     * 
     * @param error the exception that describes the error.
     */
    void onError(SynchronizeFXException error);
}
