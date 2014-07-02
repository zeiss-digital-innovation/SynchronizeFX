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

import java.util.List;

import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Passes received messages to the core parts of SynchronizeFX.
 */
class InboundCommandHandlerClient extends SimpleChannelInboundHandler<List<Object>> {

    private NetworkToTopologyCallbackClient callback;

    /**
     * Inititalizes the instance.
     * 
     * @param callback The callback to the upper layer to inform it on new commands.
     */
    public InboundCommandHandlerClient(final NetworkToTopologyCallbackClient callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final List<Object> msg) throws Exception {
        callback.recive(msg);
    }
}
