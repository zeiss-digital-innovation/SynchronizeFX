/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
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

package de.saxsys.synchronizefx;

import java.util.List;

import javafx.application.Application;

import de.saxsys.synchronizefx.testapp.DummyApplication;
import de.saxsys.synchronizefx.testapp.ExampleClient;
import de.saxsys.synchronizefx.testapp.ExampleServer;
import de.saxsys.synchronizefx.testapp.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

/**
 * This class tests whether the connection between a server and two clients is working correct, so that the data is
 * correctly synchronized between the clients. This contains adding, removing and changing of data, with focus on
 * simultaneously changes.
 * 
 * @author ragna-diana.steglich
 * 
 */
public class NettyTwoClientsIT {

    /**
     * Time in milliseconds after which JUnit should abort a tests. This prevents the {@link ExampleServer} from
     * continuing to run and blocking the TCP-Port because the user had to abort the test manually.
     */
    private static final long TEST_TIMEOUT = 10000;
    private static final int TIME_TO_WAIT = 200;
    private static final Logger LOG = LoggerFactory.getLogger(NettyTwoClientsIT.class);

    private ExampleServer server;
    private ExampleClient fstClient;
    private ExampleClient sndClient;

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
        fstClient = newClientInstance();
        sndClient = newClientInstance();
    }

    /**
     * Tests whether two client gets lists of same content from the server.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testSameModel() {
        assertTrue(!areDifferentLists(fstClient.getMessages(), sndClient.getMessages()));
    }

    /**
     * Tests whether a change of a client is recognized by another client.
     * 
     * @throws InterruptedException
     *             when the thread is interrupted
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testChangeMessage() throws InterruptedException {
        sndClient.editRandomMessage();
        Thread.sleep(TIME_TO_WAIT);

        assertTrue(!areDifferentLists(fstClient.getMessages(), sndClient.getMessages()));
    }

    /**
     * Tests whether the adding of a new message by a client is recognized by a second client.
     * 
     * @throws InterruptedException
     *             when the thread is interrupted
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testAddNewMessage() throws InterruptedException {
        sndClient.addMessage();
        Thread.sleep(TIME_TO_WAIT);

        assertTrue(!areDifferentLists(fstClient.getMessages(), sndClient.getMessages()));
    }

    /**
     * Tests whether the removing of a random message by a client is recognized by a second client.
     * 
     * @throws InterruptedException
     *             when the thread is interrupted
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testRemoveMessage() throws InterruptedException {
        int size = fstClient.getMessages().size();
        int random = (int) (Math.round(Math.random() * (size - 1)));

        sndClient.deleteMessage(random);
        Thread.sleep(TIME_TO_WAIT);

        assertTrue(!areDifferentLists(fstClient.getMessages(), sndClient.getMessages()));
    }

    /**
     * Tests whether changing a message by one client and adding a message by another client at nearly same time could
     * be handled by the server.
     * 
     * @throws InterruptedException
     *             when the thread is interrupted
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testChangeAddMessages() throws InterruptedException {
        fstClient.editRandomMessage();
        sndClient.addMessage();
        Thread.sleep(TIME_TO_WAIT);

        assertTrue(!areDifferentLists(fstClient.getMessages(), sndClient.getMessages()));
    }

    /**
     * Tests whether the server can clear the conflict, that one client changes a message and another client removes the
     * same message at nearly same time.
     * 
     * @throws InterruptedException
     *             when the thread is interrupted
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testChangeRemoveSameMessage() throws InterruptedException {
        int size = fstClient.getMessages().size();
        int random = (int) (Math.round(Math.random() * (size - 1)));

        fstClient.deleteMessage(random);
        sndClient.editSpecialMessage(random, "changed message");
        Thread.sleep(TIME_TO_WAIT);

        assertTrue(!areDifferentLists(fstClient.getMessages(), sndClient.getMessages()));
    }

    /**
     * Tests whether the server can clear the conflict, that two clients change the same message at nearly same time.
     * 
     * @throws InterruptedException
     *             when the thread is interrupted
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testChangeSameMessage() throws InterruptedException {
        int size = fstClient.getMessages().size();
        int random = (int) (Math.round(Math.random() * (size - 1)));

        ExampleClient fstTempClient = newClientInstance();

        fstClient.editSpecialMessage(random, "first change");
        sndClient.editSpecialMessage(random, "second change");
        Thread.sleep(TIME_TO_WAIT);

        LOG.debug("First client messages: " + fstClient.getMessages());
        LOG.debug("Second client messages:" + sndClient.getMessages());
        LOG.debug("Temporary client messages: " + fstTempClient.getMessages());

        assertTrue(!areDifferentLists(fstClient.getMessages(), sndClient.getMessages()));
    }

    /**
     * Stops the application, disconnect the client and shuts down the server after each test method.
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

    private ExampleClient newClientInstance() {
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

    /**
     * Compares whether the messages of the two given lists have the same text. In case there is a difference between
     * the messages, the method returns <code>true</code>, otherwise <code>false</code>.
     * 
     * @param firstList
     *            the first list, which elements should be compared
     * @param secondList
     *            the second list, which elements should be compared
     * @return <code>true</code>, when there is a difference between two elements, <code>false</code>, when there are no
     *         differences between two elements
     */
    private boolean areDifferentLists(final List<Message> firstList, final List<Message> secondList) {
        boolean different = false;
        if (firstList.size() != secondList.size()) {
            different = true;
        } else {
            for (int i = 0; i < firstList.size(); i++) {
                String firstElem = firstList.get(i).getText();
                String secondElem = secondList.get(i).getText();
                if (firstElem.compareTo(secondElem) != 0) {
                    different = true;
                }
            }
        }
        return different;
    }
}
