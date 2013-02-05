package de.saxsys.synchronizefx.core.clientserver;

import javafx.beans.property.Property;
import de.saxsys.synchronizefx.core.SynchronizeFXException;

/**
 * This class implements a client that accesses a JavaFX model made available over the network by a
 * {@link DomainModelServer}. All fields of the model that implement the {@link Property} interface will be synchronized
 * between all clients.
 * 
 * @author raik.bieniek
 * 
 */
public class DomainModelClient {

    private DomainModelClientInternal impl;

    /**
     * Connects the client to the server and request the domain model.
     * 
     * @param networkLayer An object that does the serialization and the network transfer of the data generated to keep
     *            models synchron.
     * @param serializer The serializer that serializes the messages to byte array. If a user provided serializer is
     *            supported or not depends on the implementation you pass with {@code networkLayer}. See it's JavaDoc
     *            for more informations. Passing {@code null} here means that the internal serializer of
     *            {@code networkLayer} is used. In this case you can also use
     *            {@link DomainModelClient#DomainModelClient(MessageTransferClient, UserCallbackClient)}.
     * @param listener Used to inform the user of this class on errors and when the initial transfer of the domain model
     *            is ready.
     * @throws SynchronizeFXException When the connection to the server failed.
     */
    public DomainModelClient(final MessageTransferClient networkLayer, final Serializer serializer,
            final UserCallbackClient listener) throws SynchronizeFXException {
        impl = new DomainModelClientInternal(networkLayer, serializer, listener);
    }

    /**
     * Same as {@link DomainModelClient#DomainModelClient(MessageTransferClient, Serializer, UserCallbackClient)} but it
     * relays on the internal serializer of {@code networkLayer}.
     * 
     * @param networkLayer see
     *            {@link DomainModelClient#DomainModelClient(MessageTransferClient, Serializer, UserCallbackClient)}
     * @param listener see
     *            {@link DomainModelClient#DomainModelClient(MessageTransferClient, Serializer, UserCallbackClient)}
     * @throws SynchronizeFXException see
     *             {@link DomainModelClient#DomainModelClient(MessageTransferClient, Serializer, UserCallbackClient)}
     * @see DomainModelClient#DomainModelClient(MessageTransferClient, Serializer, UserCallbackClient)
     */
    public DomainModelClient(final MessageTransferClient networkLayer, final UserCallbackClient listener)
        throws SynchronizeFXException {
        this(networkLayer, null, listener);
    }

    /**
     * Terminates the connection to the server.
     */
    public void disconnect() {
        impl.disconnect();
    }
}
