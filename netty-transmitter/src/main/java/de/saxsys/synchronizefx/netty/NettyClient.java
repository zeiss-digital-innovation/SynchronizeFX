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

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.core.clientserver.MessageTransferClient;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

/**
 * A client that can send and recive objects over the network when connected to a server.
 * 
 * This class is intended to be used as input for {@link SynchronizeFxClient}.
 * 
 * @author raik.bieniek
 */
public class NettyClient extends NettyEndPoint implements MessageTransferClient {
    private static final Logger LOG = LoggerFactory.getLogger(NettyClient.class);

    private final int port;
    private final String serverAdress;

    private NetworkToTopologyCallbackClient callbackClient;
    private ClientBootstrap client;
    private Channel clientChannel;

    private boolean disconnectByServer = true;

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
        super(serializer);
        this.serverAdress = serverAdress;
        this.port = port;
    }

    @Override
    public void setTopologyCallback(final NetworkToTopologyCallbackClient callback) {
        callbackClient = callback;
    }

    @Override
    public void connect() throws SynchronizeFXException {
        client =
                new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        client.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                        new SimpleChannelUpstreamHandler() {

                            @Override
                            public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e)
                                throws Exception {
                                LOG.info("Connected to the server");
                                ctx.getChannel().getCloseFuture().addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(final ChannelFuture future) throws Exception {
                                        if (disconnectByServer) {
                                            LOG.info("The connection was closed by the server.");
                                            callbackClient.onServerDisconnect();
                                        } else {
                                            LOG.info("Connection to server closed");
                                        }
                                    }
                                });
                            }

                            @Override
                            public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e)
                                throws Exception {
                                List<Object> messages =
                                        serializer.deserialize(((ChannelBuffer) e.getMessage()).array());
                                if (messages != null) {
                                    callbackClient.recive(messages);
                                }
                            }

                            @Override
                            public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e)
                                throws Exception {
                                callbackClient.onError(new SynchronizeFXException(e.getCause()));
                                disconnect();
                            }
                        }, new LengthFieldPrepender(4));
            }
        });

        ChannelFuture connectFuture = client.connect(new InetSocketAddress(serverAdress, port));
        connectFuture.awaitUninterruptibly();
        if (!connectFuture.isSuccess()) {
            client.releaseExternalResources();
            throw new SynchronizeFXException(connectFuture.getCause());
        }
        clientChannel = connectFuture.getChannel();
    }

    @Override
    public void send(final List<Object> messages) {
        List<Object>[] chunks = chunk(messages);
        for (List<Object> chunk : chunks) {
            try {
                clientChannel.write(ChannelBuffers.wrappedBuffer(serializer.serialize(chunk)));
            } catch (final SynchronizeFXException e) {
                callbackClient.onError(e);
                disconnect();
            }
        }
    }

    @Override
    public void disconnect() {
        disconnectByServer = false;
        clientChannel.close().awaitUninterruptibly();
        client.releaseExternalResources();
    }
}
