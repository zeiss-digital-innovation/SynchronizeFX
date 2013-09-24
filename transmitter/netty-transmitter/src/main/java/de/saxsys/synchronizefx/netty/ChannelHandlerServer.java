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

import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackServer;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

/**
 * Handles incoming events and messages from a single client.
 */
class ChannelHandlerServer extends SimpleChannelUpstreamHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ChannelHandlerServer.class);
    private NetworkToTopologyCallbackServer callbackServer;
    private Serializer serializer;
    private NettyServer parent;

    /**
     * Initializes the handler with all its dependencies.
     * 
     * @param parent The instance that manages the connection to the clients.
     * @param callbackServer The callback used to inform the upper layer on certain events.
     * @param serializer The instance that should be used to deserialize recived messages.
     */
    ChannelHandlerServer(final NettyServer parent, final NetworkToTopologyCallbackServer callbackServer,
            final Serializer serializer) {
        this.parent = parent;
        this.callbackServer = callbackServer;
        this.serializer = serializer;
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        LOG.info("A client connected");
        ctx.getChannel().getCloseFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                LOG.info("A client disconnected");
                parent.clientDisconnectFinished(future.getChannel());
            }
        });
        callbackServer.onConnect(ctx.getChannel());
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        List<Object> messages = serializer.deserialize(((ChannelBuffer) e.getMessage()).array());
        if (messages != null) {
            callbackServer.recive(messages, ctx.getChannel());
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
        callbackServer.onClientConnectionError(new SynchronizeFXException(e.getCause()));
        e.getChannel().close();
    }
}
