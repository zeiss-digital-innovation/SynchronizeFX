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

package de.saxsys.synchronizefx;

import java.util.concurrent.Executor;

import de.saxsys.synchronizefx.core.clientserver.ServerCallback;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.kryo.KryoSerializer;
import de.saxsys.synchronizefx.netty.tcp.NettyServer;

import com.esotericsoftware.kryo.Serializer;

/**
 * The Builder implementation for the Server.
 */
class ServerBuilder implements ServerModelStep, ServerCallbackStep, OptionalServerStep {
    private static final int DEFAULT_PORT = 54263;

    private int port = DEFAULT_PORT;
    private final KryoSerializer serializer = new KryoSerializer();
    private ServerCallback callback;
    private Object model;

    private Executor changeExecutor;

    @Override
    public OptionalServerStep port(final int port) {
        this.port = port;
        return this;
    }

    @Override
    public <T> OptionalServerStep customSerializer(final Class<T> clazz, final Serializer<T> serializer) {
        this.serializer.registerSerializableClass(clazz, serializer);
        return this;
    }

    @Override
    public OptionalServerStep callback(final ServerCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public ServerCallbackStep model(final Object model) {
        this.model = model;
        return this;
    }

    @Override
    public OptionalServerStep modelChangeExecutor(final Executor executor) {
        this.changeExecutor = executor;
        return this;
    }

    @Override
    public SynchronizeFxServer build() {
        final NettyServer netty = new NettyServer(port, serializer);
        
        if (changeExecutor == null) {
            return new SynchronizeFxServer(model, netty, callback);
        } else {
            return new SynchronizeFxServer(model, netty, callback, changeExecutor); 
        }
    }
}
