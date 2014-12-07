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

package de.saxsys.synchronizefx.netty.tcp;

import java.net.InetSocketAddress;

import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.netty.base.client.BasicChannelInitializerClient;
import de.saxsys.synchronizefx.netty.base.client.NettyBasicClient;

/**
 * 
 * A client side transmitter implementation for SynchronizeFX that uses Netty and transfers messages.
 * 
 * <p>
 * This class is intended to be used as input for {@link SynchronizeFxClient}.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class NettyClient extends NettyBasicClient {

    private final Serializer serializer;

    /**
     * Takes the required informations to connect to a server but doesn't actually connect to it.
     * 
     * The opening of the connection is done by {@link SynchronizeFxClient}.
     * 
     * @param serverAdress The domain name or IP address of a server to connect to.
     * @param port The port of the server to connect to.
     * @param serializer The serializer that should be used to serialize SynchronizeFX messages.
     */
    public NettyClient(final String serverAdress, final int port, final Serializer serializer) {
        super(new InetSocketAddress(serverAdress, port));
        this.serializer = serializer;
    }

    @Override
    protected BasicChannelInitializerClient createChannelInitializer() {
        LengthFieldBasedCodec codec = new LengthFieldBasedCodec();
        return new BasicChannelInitializerClient(serializer, codec, false);
    }
}
