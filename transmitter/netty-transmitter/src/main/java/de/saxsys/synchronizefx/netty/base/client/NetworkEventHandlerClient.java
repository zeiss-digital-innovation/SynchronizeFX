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

package de.saxsys.synchronizefx.netty.base.client;

import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
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
class NetworkEventHandlerClient extends ChannelDuplexHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkEventHandlerClient.class);
    private final NetworkToTopologyCallbackClient callback;
    private boolean clientInitiatedClose;

    /**
     * Initializes the event handler.
     * 
     * @param callback The callback to in inform of errors.
     */
    public NetworkEventHandlerClient(final NetworkToTopologyCallbackClient callback) {
        this.callback = callback;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        LOG.info("Connected to the server.");
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        if (!clientInitiatedClose) {
            callback.onServerDisconnect();
        }
        LOG.info("Connection to the server is closed now.");
        ctx.fireChannelInactive();
    }

    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise future) throws Exception {
        clientInitiatedClose = true;
        ctx.close(future);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ctx.channel().close();
        SynchronizeFXException exception =
                cause instanceof SynchronizeFXException ? (SynchronizeFXException) cause : new SynchronizeFXException(
                        "An error occured while trying to communicate with the server.", cause);
        callback.onError(exception);
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
