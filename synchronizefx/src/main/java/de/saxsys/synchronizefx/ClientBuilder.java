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

package de.saxsys.synchronizefx;

import java.util.concurrent.Executor;

import de.saxsys.synchronizefx.core.clientserver.ClientCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.kryo.KryoSerializer;
import de.saxsys.synchronizefx.netty.tcp.NettyClient;

import com.esotericsoftware.kryo.Serializer;

/**
 * The Builder implementation for the Client.
 */
class ClientBuilder implements ClientCallbackStep, OptionalClientStep, ClientAddressStep {
    private static final int DEFAULT_PORT = 54263;

    private int port = DEFAULT_PORT;
    private String address = "localhost";
    private final KryoSerializer serializer = new KryoSerializer();
    private ClientCallback callback;
    private Executor changeExecutor;


    @Override
    public ClientCallbackStep address(final String address) {
        this.address = address;
        return this;
    }
    @Override
    public OptionalClientStep callback(final ClientCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public OptionalClientStep port(final int port) {
        this.port = port;
        return this;
    }

    @Override
    public <T> OptionalClientStep customSerializer(final Class<T> clazz, final Serializer<T> serializer) {
        this.serializer.registerSerializableClass(clazz, serializer);
        return this;
    }
    
    @Override
    public OptionalClientStep modelChangeExecutor(final Executor executor) {
        this.changeExecutor = executor;
        return this;
    }

    @Override
    public SynchronizeFxClient build() {
        final NettyClient netty = new NettyClient(address, port, serializer);
        if (changeExecutor == null) {
            return new SynchronizeFxClient(netty, callback);
        } else {
            return new SynchronizeFxClient(netty, callback, changeExecutor);
        }
    }
}
