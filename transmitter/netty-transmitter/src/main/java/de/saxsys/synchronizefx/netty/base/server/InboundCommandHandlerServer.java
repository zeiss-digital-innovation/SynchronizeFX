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

package de.saxsys.synchronizefx.netty.base.server;

import java.util.List;

import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackServer;
import de.saxsys.synchronizefx.core.metamodel.commands.Command;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles messages received from clients.
 * 
 * @author Raik Bieniek
 */
class InboundCommandHandlerServer extends SimpleChannelInboundHandler<List<Command>> {

    private final NetworkToTopologyCallbackServer callback;

    /**
     * Initializes an instance with its dependencies.
     * 
     * @param callback The callback to inform on received commands.
     */
    InboundCommandHandlerServer(final NetworkToTopologyCallbackServer callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext client, final List<Command> commands) throws Exception {
        callback.recive(commands, client.channel());
    }
}
