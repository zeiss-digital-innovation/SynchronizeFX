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

package de.saxsys.synchronizefx.testapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;
import de.saxsys.synchronizefx.kryo.KryoSerializer;
import de.saxsys.synchronizefx.netty.NettyServer;

/**
 * 
 * A server that provides messages, which contain a simple text.
 * 
 * @author ragna-diana.steglich
 * 
 */
public class ExampleServer implements ServerCallback {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleServer.class);
    private static final int DEFAULT_PORT = 54263;

    private int port = DEFAULT_PORT;
    private KryoSerializer serializer = new KryoSerializer();
    private SynchronizeFxServer server;
    private MessageContainer container;

    /**
     * Creates a new <code>MessageContainer</code>, adds several messages to this container and starts the server.
     */
    public ExampleServer() {
        container = new MessageContainer();
        addNewMessage("this is a message");
        addNewMessage("it's a new message");
        addNewMessage("can it be a message?");

        startSynchronizeFX();
    }

    /**
     * Shuts down the {@link SynchronizeFXServer}.
     */
    public void shutdownServer() {
        server.shutdown();
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        LOG.error("A SynchronizeFX error occured. Terminating the server now.", error);
        System.exit(-1);
    }

    private void addNewMessage(final String text) {
        Message message = new Message();
        message.setText(text);

        this.container.getMessages().add(message);
    }

    private void startSynchronizeFX() {
        NettyServer netty = new NettyServer(port, serializer);
        server = new SynchronizeFxServer(container, netty, this);
        server.start();
    }
}
