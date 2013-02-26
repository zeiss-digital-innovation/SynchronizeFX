package de.saxsys.synchronizefx.core.clientserver;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

/**
 * This interface is used to inform the user about different events like errors that occurred in the library.
 * 
 * @author raik.bieniek
 * 
 */
public interface UserCallbackServer {

    /**
     * Called when an error occurred in the synchronization code.
     * 
     * This includes errors occurred in the {@link MessageTransferServer} implementation.
     * 
     * @param error the exception that describes the error.
     */
    void onError(SynchronizeFXException error);
}
