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

package de.saxsys.synchronizefx.netty.base.server;

import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackServer;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles all events that where triggered through inbound network events except for message received events.
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 */
class NetworkEventHandlerServer extends ChannelDuplexHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkEventHandlerServer.class);

    private final NetworkToTopologyCallbackServer userCallback;

    /**
     * Initializes the instance with its dependencies.
     * 
     * @param userCallback used to inform the user on failed client connections.
     */
    public NetworkEventHandlerServer(final NetworkToTopologyCallbackServer userCallback) {
        this.userCallback = userCallback;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        LOG.info("A client connected from the address " + ctx.channel().remoteAddress());
        userCallback.onConnect(ctx.channel());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        LOG.info("The connection to client with the address" + ctx.channel().remoteAddress() + " was closed.");
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        final String message =
                "An error occured when communicating with the client at address " + ctx.channel().remoteAddress()
                        + ". Closing the connection to this client.";
        LOG.warn(message, cause);
        ctx.channel().close();
        userCallback.onClientConnectionError(new SynchronizeFXException(message, cause));
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg,
            final ChannelPromise promise) throws Exception {
        promise.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(final Future<? super Void> future) throws Exception {
                Throwable cause = future.cause();
                if (cause != null) {
                    exceptionCaught(ctx, cause);
                }
            }
        });
        ctx.write(msg, promise);
    }
}
