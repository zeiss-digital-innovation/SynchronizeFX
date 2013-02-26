package de.saxsys.synchronizefx.core.clientserver;

import java.util.List;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

import javafx.beans.property.Property;

/**
 * <p>
 * This is the interface to the underlying network library that serializes and transfers the messages produced by
 * this framework.
 * </p>
 * 
 * <p>
 * All methods defined in here are intended to be used by {@link SynchronizeFxClient} and not by the user.
 * </p>
 * 
 * <p>
 * Any implementation must be capable of transferring all classes in the package
 * {@link de.saxsys.synchronizefx.core.metamodel.commands}, the class {@code java.util.UUID} and all classes of the
 * user domain model that doesn't contain any Fields that extend {@link Property}. You may wan't the user to provide
 * them manually if your implementation hasn't any generic system to handle any {@link Object} that is thrown on it.
 * </p>
 * 
 * <p>
 * It is guaranteed that {@link MessageTransferClient#setTopologyCallback(NetworkToTopologyCallbackClient)} is only
 * called before {@link MessageTransferClient#connect()} and {@link MessageTransferClient#send(List)} only after
 * {@link MessageTransferClient#connect()} (maybe multiple times). It is also guaranteed that no more methods are
 * called after {@link MessageTransferClient#disconnect()} was called.
 * </p>
 * 
 * <p>
 * If you support user supplied serializers, please use the {@link Serializer} interface for them. This way
 * serializers are usable for multiple {@link MessageTransferClient} implementations.
 * </p>
 * 
 * <p>
 * This is the client side. For the server side see {@link NetworkLayerCallbackServer}.
 * </p>
 * 
 * @author raik.bieniek
 */
public interface MessageTransferClient {

    /**
     * Sets the callback to the topology layer for events like incoming messages or errors.
     * 
     * @param callback The callback
     */
    void setTopologyCallback(NetworkToTopologyCallbackClient callback);

    /**
     * Open the connection to the server.
     * 
     * @throws SynchronizeFXException When the connection to the server failed.
     */
    void connect() throws SynchronizeFXException;

    /**
     * Sends messages to the server.
     * 
     * @param messages the messages to send. The messages don't need to be send all at once but their order must not
     *            be changed.
     */
    void send(List<Object> messages);

    /**
     * Closes the connection to the server.
     */
    void disconnect();
}
