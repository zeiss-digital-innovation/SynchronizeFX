package de.saxsys.synchronizefx.core.clientserver;

import java.util.List;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.core.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.MetaModel;
import de.saxsys.synchronizefx.core.metamodel.TopologyLayerCallback;

/**
 * The internal implementation that does all the work for {@link SynchronizeFxClient}.
 * 
 * The purpose of this class is to hide methods that are meant to be used by the framework from the user.
 * 
 * @author raik.bieniek
 */
class DomainModelClient implements NetworkToTopologyCallbackClient, TopologyLayerCallback {

    private static final Logger LOG = LoggerFactory.getLogger(DomainModelClient.class);
    private UserCallbackClient user;
    private MetaModel meta = new MetaModel(this);
    private MessageTransferClient networkLayer;

    // CHECKSTYLE:OFF The signature for the other constructor is to long to fit in 120 characters
    /**
     * @see SynchronizeFxClient#SynchronizeFxClient(MessageTransferClient, Serializer, UserCallbackClient)
     * @param networkLayer see
     *            {@link SynchronizeFxClient#SynchronizeFxClient(MessageTransferClient, Serializer, UserCallbackClient)}
     * @param listener see
     *            {@link SynchronizeFxClient#SynchronizeFxClient(MessageTransferClient, Serializer, UserCallbackClient)}
     */
    // CHECKSTYLE:ON
    public DomainModelClient(final MessageTransferClient networkLayer, final UserCallbackClient listener) {
        this.user = listener;
        this.networkLayer = networkLayer;
        networkLayer.setTopologyCallback(this);
    }

    @Override
    public void recive(final List<Object> messages) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Client recived commands " + messages);
        }
        meta.execute(messages);
    }

    @Override
    public void sendCommands(final List<Object> commands) {
        networkLayer.send(commands);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Client sent commands " + commands);
        }
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        user.onError(error);
    }

    @Override
    public void domainModelChanged(final Object root) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                user.modelReady(root);
            }
        });
        meta.setDoChangesInJavaFxThread(true);
    }

    /**
     * @see SynchronizeFxClient#connect()
     */
    public void connect() {
        try {
            networkLayer.connect();
        } catch (SynchronizeFXException e) {
            user.onError(e);
        }
    }

    /**
     * @see SynchronizeFxClient#disconnect()
     */
    public void disconnect() {
        networkLayer.disconnect();
    }
}
