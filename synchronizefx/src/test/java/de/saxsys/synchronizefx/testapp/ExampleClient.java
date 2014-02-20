/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

package de.saxsys.synchronizefx.testapp;

import java.util.List;
import java.util.Random;

import de.saxsys.synchronizefx.WaitForModelReadyLock;
import de.saxsys.synchronizefx.core.clientserver.ClientCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.kryo.KryoSerializer;
import de.saxsys.synchronizefx.netty.NettyClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client, who can receive messages with a simple text. Changes at the messages are synchronized so that other clients
 * get these changes and all clients have the same messages.
 * 
 * @author ragna-diana.steglich
 * 
 */
public class ExampleClient implements ClientCallback {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleClient.class);
    private static final String SERVERADRESS = "localhost";
    private static final int DEFAULT_PORT = 54263;

    private KryoSerializer serializer = new KryoSerializer();
    private int port = DEFAULT_PORT;

    private SynchronizeFxClient client;
    private List<Message> messages;

    /**
     * Starts the {@link SynchronizeFXClient}, which tries to connect to the server, when an instance of this class is
     * created.
     */
    public ExampleClient() {
        startSynchronizeFX();
    }

    /**
     * Returns the list with the synchronized messages, which the client gets from the server.
     * 
     * @return the list with the synchronized messages
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Adds a new message to the container.
     */
    public void addMessage() {
        Message message = new Message();
        message.setText("a new message");

        messages.add(message);
    }

    /**
     * Deletes the message with the specified ID from the container.
     * 
     * @param msgID
     *            the ID of the message within the list to remove
     */
    public void deleteMessage(final int msgID) {
        messages.remove(msgID);
    }

    /**
     * Edits the text of a randomly selected message of the container.
     */
    public void editRandomMessage() {
        int random = new Random().nextInt(messages.size() - 1);

        messages.get(random).textProperty().set("changed message");
    }

    /**
     * Sets the text of the message with the specified ID to the given text.
     * 
     * @param messageIndex
     *            the ID of the message within the list
     * @param newText
     *            the new text of the message
     */
    public void editSpecialMessage(final int messageIndex, final String newText) {
        messages.get(messageIndex).textProperty().set(newText);
    }

    private void startSynchronizeFX() {
        NettyClient netty = new NettyClient(SERVERADRESS, port, serializer);
        client = new SynchronizeFxClient(netty, this);
        client.connect();
    }

    @Override
    public void modelReady(final Object model) {
        MessageContainer container = (MessageContainer) model;
        messages = container.getMessages();
        synchronized (WaitForModelReadyLock.INSTANCE) {
            WaitForModelReadyLock.INSTANCE.notifyAll();
        }
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        LOG.error("A SynchronizeFX error occured. Terminating the client now.", error);
        System.exit(-1);
    }

    @Override
    public void onServerDisconnect() {
    }
}
