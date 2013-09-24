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

package de.saxsys.synchronizefx.netty;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

/**
 * Handles incoming events and messages from the server.
 */
class ChannelHandlerClient extends SimpleChannelUpstreamHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelHandlerClient.class);
    private final NettyClient parent;
    private final NetworkToTopologyCallbackClient callbackClient;
    private final Serializer serializer;

    /**
     * Initializes the handler with all its dependencies.
     * 
     * @param parent The instance that manages the connection to the server.
     * @param callbackClient The callback used to inform the upper layer on certain events.
     * @param serializer The instance that should be used to deserialize recived messages.
     */
    ChannelHandlerClient(final NettyClient parent, final NetworkToTopologyCallbackClient callbackClient,
            final Serializer serializer) {
        this.parent = parent;
        this.callbackClient = callbackClient;
        this.serializer = serializer;
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        LOG.info("Connected to the server");
        ctx.getChannel().getCloseFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                if (parent.getDisconnectByServer()) {
                    LOG.info("The connection was closed by the server.");
                    callbackClient.onServerDisconnect();
                } else {
                    LOG.info("Connection to server closed");
                }
            }
        });
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        List<Object> messages = serializer.deserialize(((ChannelBuffer) e.getMessage()).array());
        if (messages != null) {
            callbackClient.recive(messages);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
        callbackClient.onError(new SynchronizeFXException(e.getCause()));
        parent.disconnect();
    }
}
