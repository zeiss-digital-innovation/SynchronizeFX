/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

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
 * It is guaranteed that {@link MessageTransferServer#setTopologyCallback(NetworkToTopologyCallbackServer)} is only
 * called before {@link MessageTransferServer#start()} and the send methods only after
 * {@link MessageTransferServer#start()} (maybe multiple times). It is also guaranteed that no more methods are
 * called after {@link MessageTransferServer#shutdown()} was called.
 * </p>
 * 
 * <p>
 * If you support user supplied serializers, please use the {@link Serializer} interface for them. This way
 * serializers are usable for multiple {@link MessageTransferServer} implementations.
 * </p>
 * 
 * <p>
 * This is the server side. For the client side see {@link NetworkLayerCallbackClient}.
 * </p>
 * 
 * @author raik.bieniek
 */
public interface MessageTransferServer {

    /**
     * Sets the callback to the topology layer for events like incoming messages, new connections or errors.
     * 
     * @param callback The callback
     */
    void setTopologyLayerCallback(NetworkToTopologyCallbackServer callback);

    /**
     * Starts the server and make it listen for new client connections.
     * 
     * @throws SynchronizeFXException when the start up of the server failed.
     */
    void start() throws SynchronizeFXException;

    /**
     * Sends messages to all connected peers.
     * 
     * @param messages the messages to send. The messages don't need to be send all at once but their order must not
     *            be changed.
     */
    void sendToAll(List<Object> messages);

    /**
     * Sends messages.
     * 
     * @param messages the messages to send. The messages don't need to be send all at once but their order must not
     *            be changed.
     * @param destination the destination to which this method should be send. Only objects returned by
     *            {@link IncommingEventHandlerServer#recive(List, Object)} as sender are valid destinations.
     */
    void send(List<Object> messages, Object destination);

    /**
     * Sends a message to all peers except to {@code nonReciver}.
     * 
     * @param messages The messages to be send. The messages don't need to be send all at once but their order must
     *            not be changed.
     * 
     * @param nonReciver The client that should <em>not</em> receive the message.
     */
    void sendToAllExcept(List<Object> messages, Object nonReciver);

    /**
     * Disconnect all clients that are still connected an shut down the Server.
     */
    void shutdown();
}
