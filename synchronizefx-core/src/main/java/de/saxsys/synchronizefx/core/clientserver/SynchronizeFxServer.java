package de.saxsys.synchronizefx.core.clientserver;

import javafx.beans.property.Property;

/**
 * This class implements a server that makes a JavaFX model available over the network. All fields of the model that
 * implement the {@link Property} interface will be synchronized between all clients.
 * 
 * @author raik.bieniek
 * 
 */
public class SynchronizeFxServer {

    private DomainModelServer impl;

    /**
     * Sets up everything that is needed to serve a domain model.
     * 
     * This method doesn't start the server. Use {@link SynchronizeFxServer#start()} for that.
     * 
     * @param model The root object of the domain model to serve.
     * @param networkLayer An object that does the network transfer and optionally the serialization of the data
     *            generated to keep models synchron.
     * @param user Used to inform the user of this class on errors. The methods in the callback are not called before
     *            you call {@link SynchronizeFxServer#start()}.
     */
    public SynchronizeFxServer(final Object model, final MessageTransferServer networkLayer,
            final UserCallbackServer user) {
        impl = new DomainModelServer(model, networkLayer, user);
    }

    /**
     * Starts the server and accepts incoming client connections.
     */
    public void start() {
        impl.start();
    }

    /**
     * Shuts down the server
     * 
     * Before the shutdown, all clients which are still connected are disconnected.
     */
    public void shutdown() {
        impl.shutdown();
    }
}
