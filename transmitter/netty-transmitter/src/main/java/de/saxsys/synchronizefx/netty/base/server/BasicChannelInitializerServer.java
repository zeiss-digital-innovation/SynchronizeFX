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

import de.saxsys.synchronizefx.core.clientserver.MessageTransferServer;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackServer;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.netty.base.Codec;
import de.saxsys.synchronizefx.netty.base.CommandToBinaryByteBuf;
import de.saxsys.synchronizefx.netty.base.NonValidatingSSLEngineFactory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Initializes a server side socket channel to the client.
 */
public class BasicChannelInitializerServer extends ChannelInitializer<SocketChannel> {

    private final Serializer serializer;
    private final Codec codec;
    private final boolean useSSL;

    private NetworkToTopologyCallbackServer userCallback;


    /**
     * @param serializer The implementation for serializing and deserializing <code>byte[]</code> to SynchronizeFX
     *            commands.
     * @param codec The codec used to collect incomming data blocks to parts that can be deserialized together.
     * @param useSSL <code>true</code> when the connection should be TLS encrypted, <code>false</code> when not.
     */
    public BasicChannelInitializerServer(final Serializer serializer, final Codec codec, final boolean useSSL) {
        this.serializer = serializer;
        this.codec = codec;
        this.useSSL = useSSL;
    }

    @Override
    protected void initChannel(final SocketChannel channel) throws Exception {
        final ChannelPipeline pipeline = channel.pipeline();

        if (useSSL) {
            pipeline.addLast("tls", NonValidatingSSLEngineFactory.createSslHandler(false));
        }

        codec.addToPipeline(pipeline);

        pipeline.addLast("message-to-command", new CommandToBinaryByteBuf(serializer));

        pipeline.addLast("command-handler", new InboundCommandHandlerServer(userCallback));
        pipeline.addLast("event-handler", new NetworkEventHandlerServer(userCallback));
    }

    /**
     * See MessageTransferServer#setTopologyLayerCallback(NetworkToTopologyCallbackServer).
     * 
     * @param callback the callback to set
     * @see MessageTransferServer#setTopologyLayerCallback(NetworkToTopologyCallbackServer)
     */
    void setTopologyCallback(final NetworkToTopologyCallbackServer callback) {
        this.userCallback = callback;
    }
}
