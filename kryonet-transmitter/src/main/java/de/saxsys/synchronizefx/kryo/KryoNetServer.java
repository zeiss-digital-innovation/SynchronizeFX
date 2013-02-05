package de.saxsys.synchronizefx.kryo;

import java.io.IOException;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import de.saxsys.synchronizefx.core.SynchronizeFXException;
import de.saxsys.synchronizefx.core.clientserver.DomainModelServer;
import de.saxsys.synchronizefx.core.clientserver.MessageTransferServer;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackServer;
import de.saxsys.synchronizefx.core.clientserver.Serializer;

/**
 * A server that can send and recive objects over the network to connected clients.
 * 
 * This class is intended to be used as input for {@link DomainModelServer}.
 * 
 * This implementation does not support generic serializers. It uses it's own internal serializer.
 * 
 * @author raik.bieniek
 */
public class KryoNetServer extends KryoNetEndPoint implements MessageTransferServer {
    private NetworkToTopologyCallbackServer callbackServer;
    private int port;
    private Server connection;

    /**
     * Takes the required informations needed to start the server but doesn't actually start it.
     * 
     * The starting of the server is done by {@link DomainModelServer}.
     * 
     * @param port The port to which to listen for new connections.
     */
    public KryoNetServer(final int port) {
        this.port = port;
    }

    @Override
    public void setSerializer(final Serializer serializer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTopologyLayerCallback(final NetworkToTopologyCallbackServer callback) {
        callbackServer = callback;
    }

    @Override
    public void start() throws SynchronizeFXException {
        connection = new Server(WRITE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
        kryoInitializer.setupKryo(connection);
        connection.start();
        try {
            connection.bind(port);
        } catch (IOException e) {
            throw new SynchronizeFXException("Starting of the server failed", e);
        }

        connection.addListener(new Listener() {
            @Override
            public void connected(final Connection newClient) {
                callbackServer.onConnect(newClient);
            }
        });

        connection.addListener(new Listener() {
            @Override
            public void received(final Connection connection, final Object object) {
                List<Object> messages = filterIncommingMessages(object);
                if (messages != null) {
                    callbackServer.recive(messages, connection);
                }
            }

        });
    }

    @Override
    public void sendToAll(final List<Object> messages) {
        Object[] chunks = chunk(messages);
        for (Object chunk : chunks) {
            connection.sendToAllTCP(chunk);
        }
    }

    @Override
    public void sendToAllExcept(final List<Object> messages, final Object nonReciver) {
        Object[] chunks = chunk(messages);
        for (Object chunk : chunks) {
            connection.sendToAllExceptTCP(((Connection) nonReciver).getID(), chunk);
        }
    }

    @Override
    public void send(final List<Object> messages, final Object destination) {
        Object[] chunks = chunk(messages);
        for (Object chunk : chunks) {
            ((Connection) destination).sendTCP(chunk);
        }
    }

    @Override
    public void shutdown() {
        connection.close();
    }
}
