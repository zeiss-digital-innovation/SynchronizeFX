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

package de.saxsys.synchronizefx.netty.base.client;

import java.util.concurrent.TimeUnit;

import de.saxsys.synchronizefx.core.clientserver.CommandTransferClient;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.netty.base.Codec;
import de.saxsys.synchronizefx.netty.base.CommandToBinaryByteBuf;
import de.saxsys.synchronizefx.netty.base.NonValidatingSSLEngineFactory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Initializes all channel handlers for the socket channel to the server.
 */
public class BasicChannelInitializerClient extends ChannelInitializer<SocketChannel> {

    /**
     * If no data was received from the server in this time (in milliseconds) a Ping Frame is send as keep alive.
     */
    private static final int KEEP_ALIVE = 20000;

    private NetworkToTopologyCallbackClient callback;

    private final Codec codec;
    private final boolean useSSL;
    private final Serializer serializer;

    /**
     * @param serializer The implementation for serializing and deserializing <code>byte[]</code> to SynchronizeFX
     *            commands.
     * @param codec The codec used to collect incomming data blocks to parts that can be deserialized together.
     * @param useSSL <code>true</code> when the connection should be TLS encrypted, <code>false</code> when not.
     */
    public BasicChannelInitializerClient(final Serializer serializer, final Codec codec, final boolean useSSL) {
        this.codec = codec;
        this.useSSL = useSSL;
        this.serializer = serializer;
    }

    /**
     * @see CommandTransferClient#setTopologyCallback(NetworkToTopologyCallbackClient)
     * @param callback see MessageTransferClient#setTopologyCallback(NetworkToTopologyCallbackClient)
     */
    void setTopologyCallback(final NetworkToTopologyCallbackClient callback) {
        this.callback = callback;
    }

    @Override
    protected void initChannel(final SocketChannel channel) throws Exception {
        final ChannelPipeline pipeline = channel.pipeline();

        if (useSSL) {
            pipeline.addLast("tls", NonValidatingSSLEngineFactory.createSslHandler(true));
        }
        pipeline.addLast("keep-alive", new IdleStateHandler(KEEP_ALIVE, 0, 0, TimeUnit.MILLISECONDS));

        codec.addToPipeline(pipeline);

        pipeline.addLast("message-to-command", new CommandToBinaryByteBuf(serializer));

        pipeline.addLast("command-handler", new InboundCommandHandlerClient(callback));
        pipeline.addLast("event-handler", new NetworkEventHandlerClient(callback));
    }
}
