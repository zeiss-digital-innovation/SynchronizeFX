package de.saxsys.synchronizefx.kryo;

import java.io.IOException;
import java.util.List;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import de.saxsys.synchronizefx.core.SynchronizeFXException;
import de.saxsys.synchronizefx.core.clientserver.DomainModelClient;
import de.saxsys.synchronizefx.core.clientserver.MessageTransferClient;
import de.saxsys.synchronizefx.core.clientserver.NetworkToTopologyCallbackClient;
import de.saxsys.synchronizefx.core.clientserver.Serializer;

/**
 * A client that can send and recive objects over the network when connected to a server.
 * 
 * This class is intended to be used as input for {@link DomainModelClient}.
 * 
 * This implementation does not support generic serializers. It uses it's own internal serializer.
 * 
 * @author raik.bieniek
 */
public class KryoNetClient extends KryoNetEndPoint implements MessageTransferClient {

    /**
     * The timeout in milliseconds after which a connection attempt will be aborted.
     */
    private static final int CONNECTION_TIMEOUT = 5000;
    private final int port;
    private final String serverAdress;

    private NetworkToTopologyCallbackClient callbackClient;
    private Client connection;

    /**
     * Takes the required informations to connect to a server but doesn't actually connect to it.
     * 
     * The opening of the connection is done by {@link DomainModelClient}.
     * 
     * @param serverAdress The domain name or IP address of a server to connect to.
     * @param port The port of the server to connect to.
     */
    public KryoNetClient(final String serverAdress, final int port) {
        this.serverAdress = serverAdress;
        this.port = port;
    }

    @Override
    public void setSerializer(final Serializer serializer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTopologyCallback(final NetworkToTopologyCallbackClient callback) {
        callbackClient = callback;
    }

    @Override
    public void connect() throws SynchronizeFXException {
        connection = new Client(WRITE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
        kryoInitializer.setupKryo(connection);
        connection.start();
        try {
            connection.connect(CONNECTION_TIMEOUT, serverAdress, port);
        } catch (IOException e) {
            throw new SynchronizeFXException("Connection to the server failed", e);
        }

        connection.addListener(new Listener() {
            @Override
            public void received(final Connection connection, final Object object) {
                List<Object> messages = filterIncommingMessages(object);
                if (messages != null) {
                    callbackClient.recive(messages);
                }
            }

        });
    }

    @Override
    public void send(final List<Object> messages) {
        Object[] chunks = chunk(messages);
        for (Object chunk : chunks) {
            connection.sendTCP(chunk);
        }
    }

    @Override
    public void disconnect() {
        connection.close();
    }
}
