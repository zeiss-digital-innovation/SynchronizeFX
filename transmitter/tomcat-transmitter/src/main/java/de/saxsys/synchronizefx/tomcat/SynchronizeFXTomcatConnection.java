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

package de.saxsys.synchronizefx.tomcat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Objects of this class represent a single connection between the server and a client.
 * 
 * @author raik.bieniek
 * 
 */
class SynchronizeFXTomcatConnection extends MessageInbound {

    private static final Logger LOG = LoggerFactory.getLogger(SynchronizeFXTomcatConnection.class);

    private final SynchronizeFXTomcatServlet parent;

    /**
     * 
     * @param synchronizeFXTomcatServlet the connection management instance that accepted this connection. This
     *            instance get's informed when messages are received from the client or the client closed the
     *            connection.
     */
    SynchronizeFXTomcatConnection(final SynchronizeFXTomcatServlet synchronizeFXTomcatServlet) {
        this.parent = synchronizeFXTomcatServlet;
    }

    @Override
    protected void onOpen(final WsOutbound outbound) {
        parent.clientConnectionReady(this);
    }

    @Override
    protected void onClose(final int status) {
        parent.connectionCloses(this);
    }

    @Override
    protected void onBinaryMessage(final ByteBuffer message) throws IOException {
        parent.recivedMessage(message, this);
    }

    @Override
    protected void onTextMessage(final CharBuffer message) throws IOException {
        LOG.warn("Recived text. This was not expected.");
    }
}
