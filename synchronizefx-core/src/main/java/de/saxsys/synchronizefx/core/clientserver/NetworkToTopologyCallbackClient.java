package de.saxsys.synchronizefx.core.clientserver;

import java.util.List;

import de.saxsys.synchronizefx.core.SynchronizeFXException;

/**
 * This is an callback interface for the network library to inform the upper layer of incomming messages.
 * 
 * This is the interface for the client side. For the server side use {@link IncommingEventHandlerServer}.
 * 
 * @author raik.bieniek
 * 
 */
public interface NetworkToTopologyCallbackClient {

    /**
     * Messages were received.
     * 
     * @param messages The messages received.
     */
    void recive(List<Object> messages);

    /**
     * An error occurred that made the Client disconnect from the server.
     * 
     * When this method is called, the connection to the server has to be already closed.
     * 
     * This method must only be called after the client successfully connected to a server. If an error occurred while
     * trying to connect to the server, throw an exception there.
     * 
     * @param e an exception that describes the problem.
     */
    void onError(SynchronizeFXException e);
}
