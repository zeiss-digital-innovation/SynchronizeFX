

package de.saxsys.synchronizefx.netty.bak;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import de.saxsys.synchronizefx.core.clientserver.MessageTransferClient;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
import de.saxsys.synchronizefx.core.clientserver.Serializer;
import de.saxsys.synchronizefx.core.clientserver.SynchronizeFxClient;
import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

/**
 * A client that can send and recive objects over the network when connected to a server.
 * 
 * 
 * @author raik.bieniek
 */
public class NettyClient extends NettyEndPoint implements MessageTransferClient {

    private final int port;
    private final String serverAdress;

    private NetworkToTopologyCallbackClient callbackClient;
    private ClientBootstrap client;
    private Channel clientChannel;

    private boolean disconnectByServer = true;


    @Override
    public void setTopologyCallback(final NetworkToTopologyCallbackClient callback) {
        callbackClient = callback;
    }

    @Override
    public void connect() throws SynchronizeFXException {
        client =
                new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        final ChannelHandlerClient handler = new ChannelHandlerClient(this, callbackClient, serializer);
        // Set up the pipeline factory.
        client.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
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

    /**
     * The information if the server initiated the disconnect or the client.
     * @return {@code true} if the server did, {@code false} otherwhise.
     */
    boolean getDisconnectByServer() {
        return disconnectByServer;
    }
}
