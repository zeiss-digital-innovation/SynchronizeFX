package de.saxsys.synchronizefx.core.clientserver;

import javafx.beans.property.Property;
import de.saxsys.synchronizefx.core.SynchronizeFXException;

/**
 * This class implements a server that makes a JavaFX model available over the network. All fields of the model that
 * implement the {@link Property} interface will be synchronized between all clients.
 * 
 * @author raik.bieniek
 * 
 */
public class DomainModelServer {

    private DomainModelServerInternal impl;

    /**
     * Starts the serving of the domain model to client.
     * 
     * @param model The root object of the domain model to serve.
     * @param networkLayer An object that does the network transfer and optionally the serialization of the data
     *            generated to keep models synchron.
     * @param serializer The serializer that serializes the messages to byte array. If a user provided serializer is
     *            supported or not depends on the implementation you pass with {@code networkLayer}. See it's JavaDoc
     *            for more informations. Passing {@code null} here means that the internal serializer of
     *            {@code networkLayer} is used. In this case you can also use
     *            {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, UserCallbackServer)}.
     * @param user Used to inform the user of this class on errors.
     * @throws SynchronizeFXException When the startup of the server failed.
     */
    public DomainModelServer(final Object model, final MessageTransferServer networkLayer, final Serializer serializer,
            final UserCallbackServer user) throws SynchronizeFXException {
        impl = new DomainModelServerInternal(model, networkLayer, serializer, user);
    }

    // CHECKSTYLE:OFF The signature for the other constructor is to long to fit in 120 characters
    /**
     * Same as
     * {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer)} but it
     * relays on the internal serializer of {@code networkLayer}.
     * 
     * @param model see
     *            {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer)}
     * @param networkLayer {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer)}
     * @param user {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer)}
     * @throws SynchronizeFXException
     *             {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer)}
     * @see DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer);
     */
    public DomainModelServer(final Object model, final MessageTransferServer networkLayer, final UserCallbackServer user)
    // CHECKSTYLE:ON
        throws SynchronizeFXException {
        this(model, networkLayer, null, user);
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
