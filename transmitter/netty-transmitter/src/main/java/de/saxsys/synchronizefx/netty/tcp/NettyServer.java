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

package de.saxsys.synchronizefx.netty.tcp;

import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.netty.base.server.BasicChannelInitializerServer;
import de.saxsys.synchronizefx.netty.base.server.NettyBasicServer;

/**
 * 
 * A server side transmitter implementation for SynchronizeFX that uses Netty and transfers messages.
 * 
 * <p>
 * This class is intended to be used as input for {@link SynchronizeFxServer}.
 * </p>
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 */
public class NettyServer extends NettyBasicServer {

    private Serializer serializer;

    /**
     * Takes the required informations needed to start the server but doesn't actually start it.
     * 
     * The starting of the server is done by {@link SynchronizeFxServer}.
     * 
     * @param port The port to which to listen for new connections.
     * @param serializer The serializer that should be used to serialize SynchronizeFX messages.
     */
    public NettyServer(final int port, final Serializer serializer) {
        super(port);
        this.serializer = serializer;
    }

    @Override
    protected BasicChannelInitializerServer createChannelInitializer() {
        return new BasicChannelInitializerServer(serializer, new LengthFieldBasedCodec(), false);
    }
    
}
