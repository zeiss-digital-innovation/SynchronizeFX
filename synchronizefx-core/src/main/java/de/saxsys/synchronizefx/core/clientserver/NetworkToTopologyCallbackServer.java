package de.saxsys.synchronizefx.core.clientserver;

import java.util.List;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

/**
 * This is an callback interface for the network library to inform the upper layer of incoming events like messages or
 * new clients.
 * 
 * This is the interface for the server side. For the client side use {@link IncommingEventHandlerClient}.
 * 
 * @author raik.bieniek
 * 
 */
public interface NetworkToTopologyCallbackServer {

    /**
     * Messages were received.
     * 
     * @param messages The messages received.
     * @param sender An object that represents the sender of the messages.
     */
    void recive(List<Object> messages, Object sender);

    /**
     * A new client connected.
     * 
     * @param newClient an object that represents the new client.
     */
    void onConnect(Object newClient);

    /**
     * An error in the connection to some client occurred.
     * 
     * When this method is called, the connection to the problematic client has to be already be terminated. The server
     * must still be working. If not use {@link NetworkToTopologyCallbackServer#onFatalError()} instead.
     * 
     * @param e an exception that describes the problem.
     */
    void onClientConnectionError(SynchronizeFXException e);

    /**
     * A fatal error that made the server shut down.
     * 
     * When this method is called, the server came across an error that made it impossible to continue the normal
     * operation. The connection to all remaining clients must already be closed and the server be shutdown.
     * 
     * This method must only be called after the server successfully started. If an error occurred while trying to start
     * up, throw an exception there.
     * 
     * @param e an exception that describes the problem.
     */
    void onFatalError(SynchronizeFXException e);
}
