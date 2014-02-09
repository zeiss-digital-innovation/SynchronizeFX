/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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

package de.saxsys.synchronizefx;

import java.util.LinkedList;
import java.util.List;

import javafx.application.Application;

import de.saxsys.synchronizefx.testapp.DummyApplication;
import de.saxsys.synchronizefx.testapp.ExampleClient;
import de.saxsys.synchronizefx.testapp.ExampleServer;
import de.saxsys.synchronizefx.testapp.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * This class tests whether the connection between a server and a client is working correct, so that the client gets
 * correct data from the server and can work with the data. This contains adding, removing and changing of data.
 * 
 * @author ragna-diana.steglich
 * 
 */
public class NettyOneClientIT {

    private ExampleServer server;
    private ExampleClient client;

    /**
     * Initializes server and client for each test method and starts the {@link DummyApplication}, when there is no
     * running instance.
     */
    @Before
    public void setUp() {
        server = new ExampleServer();
        if (!DummyApplication.isRunning()) {
            startDummyInstance();
        }
        client = getClientInstance();
    }

    /**
     * Tests whether the client gets the data from the server.
     */
    @Test
    public void testGetMessages() {
        assertTrue(client.getMessages() != null);
    }

    /**
     * Tests whether changes at the messages by a client appear at the server.
     */
    @Test
    public void testChangeMessage() {
        List<Message> temp = copyList(client.getMessages());
        boolean different = false;

        client.editRandomMessage();

        if (temp.size() != client.getMessages().size()) {
            different = true;
        } else {
            for (int i = 0; i < temp.size(); i++) {
                String firstElem = temp.get(i).getText();
                String secondElem = client.getMessages().get(i).getText();
                if (firstElem.compareTo(secondElem) != 0) {
                    different = true;
                }
            }
        }

        assertTrue(different);
    }

    /**
     * Tests whether a new message can be added by a client.
     */
    @Test
    public void testAddNewMessage() {
        List<Message> temp = copyList(client.getMessages());
        client.addMessage();

        assertTrue(temp.size() != client.getMessages().size());
    }

    /**
     * Tests whether a random message can be removed by a client.
     */
    @Test
    public void tesRemoveMessage() {
        int size = client.getMessages().size();
        int random = (int) Math.random() * (size - 1);

        client.deleteMessage(random);

        assertTrue(size != client.getMessages().size());
    }

    /**
     * Tests whether a recently added message can be changed by a client.
     */
    @Test
    public void testAddAndChangeMessae() {
        int size = client.getMessages().size();
        String changedText = "changed";

        client.addMessage();
        client.editSpecialMessage(size, changedText);

        assertTrue(client.getMessages().get(size).getText().equals(changedText));
    }

    /**
     * Shuts down the server after a test method is done.
     */
    @After
    public void tearDown() {
        server.shutdownServer();
    }

    /**
     * Starts a {@link DummyApplication}, so that the JavaFX-Thread is started.
     */
    private void startDummyInstance() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Application.launch(DummyApplication.class);
            }
        }).start();
    }

    private ExampleClient getClientInstance() {
        ExampleClient client = new ExampleClient();
        synchronized (WaitForModelReadyLock.INSTANCE) {
            try {
                WaitForModelReadyLock.INSTANCE.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return client;
    }

    private List<Message> copyList(final List<Message> list) {
        List<Message> copy = new LinkedList<Message>();

        for (Message message : list) {
            Message temp = new Message();
            temp.setText(message.getText());

            copy.add(temp);
        }

        return copy;
    }
}
