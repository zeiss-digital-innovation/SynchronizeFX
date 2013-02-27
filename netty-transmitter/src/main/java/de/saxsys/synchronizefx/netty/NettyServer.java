/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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

package de.saxsys.synchronizefx.netty;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

import de.saxsys.synchronizefx.core.SynchronizeFXException;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxServer;
import de.saxsys.synchronizefx.core.clientserver.MessageTransferServer;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackServer;
import de.saxsys.synchronizefx.core.clientserver.Serializer;

/**
 * A server that can send and recive objects over the network to connected clients.
 * 
 * This class is intended to be used as input for {@link SynchronizeFxServer}.
 *  
 * @author raik.bieniek
 */
public class NettyServer extends NettyEndPoint implements MessageTransferServer {
    private NetworkToTopologyCallbackServer callbackServer;
    private int port;

    private ServerBootstrap server;
    private Channel serverChannel;
    private ChannelGroup clients = new DefaultChannelGroup();

    /**
     * Takes the required informations needed to start the server but doesn't actually start it.
     * 
     * The starting of the server is done by {@link SynchronizeFxServer}.
     * 
     * @param port The port to which to listen for new connections.
     * @param serializer The serializer that should be used to serialize SynchronizeFX messages.
     */
    public NettyServer(final int port, final Serializer serializer) {
        super(serializer);
        this.port = port;
    }

    @Override
    public void setTopologyLayerCallback(final NetworkToTopologyCallbackServer callback) {
        callbackServer = callback;
    }

    @Override
    public void start() throws SynchronizeFXException {
        server =
                new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        server.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                        new SimpleChannelUpstreamHandler() {

                            @Override
                            public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e)
                                throws Exception {
                                clients.add(ctx.getChannel());
                                callbackServer.onConnect(ctx.getChannel());
                            }

                            @Override
                            public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e)
                                throws Exception {
                                List<Object> messages =
                                        serializer.deserialize(((ChannelBuffer) e.getMessage()).array());
                                if (messages != null) {
                                    callbackServer.recive(messages, ctx.getChannel());
                                }
                            }

                            @Override
                            public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e)
                                throws Exception {
                                callbackServer.onClientConnectionError(new SynchronizeFXException(e.getCause()));
                                e.getChannel().close();
                            }
                        }, new LengthFieldPrepender(4));
            }
        });
        server.setOption("child.tcpNoDelay", true);
        server.setOption("child.keepAlive", true);

        serverChannel = server.bind(new InetSocketAddress(port));
    }

    @Override
    public void sendToAll(final List<Object> messages) {
        List<Object>[] chunks = chunk(messages);
        for (List<Object> chunk : chunks) {
            clients.write(ChannelBuffers.wrappedBuffer(serialize(chunk)));
        }
    }

    @Override
    public void sendToAllExcept(final List<Object> messages, final Object nonReciver) {
        List<Object>[] chunks = chunk(messages);
        for (List<Object> chunk : chunks) {
            ChannelBuffer msg = ChannelBuffers.wrappedBuffer(serialize(chunk));
            for (Channel channel : clients) {
                if (channel != nonReciver) {
                    channel.write(msg);
                }
            }
        }
    }

    @Override
    public void send(final List<Object> messages, final Object destination) {
        List<Object>[] chunks = chunk(messages);
        for (List<Object> chunk : chunks) {
            ((Channel) destination).write(ChannelBuffers.wrappedBuffer(serialize(chunk)));
        }
    }

    @Override
    public void shutdown() {
        serverChannel.close().awaitUninterruptibly();
        clients.close().awaitUninterruptibly();
        server.releaseExternalResources();
    }

    private byte[] serialize(final List<Object> messages) {
        try {
            return serializer.serialize(messages);
        } catch (SynchronizeFXException e) {
            callbackServer.onFatalError(e);
        }
        return new byte[0];
    }
}
