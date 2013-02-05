package de.saxsys.synchronizefx.core.clientserver;

import java.util.List;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.core.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.MetaModel;
import de.saxsys.synchronizefx.core.metamodel.TopologyLayerCallback;

/**
 * The internal implementation that does all the work for {@link DomainModelClient}.
 * 
 * The purpose of this class is to hide methods that are meant to be used by the framework from the user.
 * 
 * @author raik.bieniek
 */
class DomainModelClientInternal implements NetworkToTopologyCallbackClient, TopologyLayerCallback {

    private static final Logger LOG = LoggerFactory.getLogger(DomainModelClientInternal.class);
    private UserCallbackClient listener;
    private MetaModel meta = new MetaModel(this);
    private MessageTransferClient networkLayer;

    /**
     * @see DomainModelClient#DomainModelClient(MessageTransferClient, Serializer, UserCallbackClient)
     * @param networkLayer see
     *            {@link DomainModelClient#DomainModelClient(MessageTransferClient, Serializer, UserCallbackClient)}
     * @param serializer see
     *            {@link DomainModelClient#DomainModelClient(MessageTransferClient, Serializer, UserCallbackClient)}
     * @param listener see
     *            {@link DomainModelClient#DomainModelClient(MessageTransferClient, Serializer, UserCallbackClient)}
     * @throws SynchronizeFXException see
     *             {@link DomainModelClient#DomainModelClient(MessageTransferClient, Serializer, UserCallbackClient)}
     */
    public DomainModelClientInternal(final MessageTransferClient networkLayer, final Serializer serializer,
            final UserCallbackClient listener) throws SynchronizeFXException {
        this.listener = listener;
        this.networkLayer = networkLayer;
        networkLayer.setTopologyCallback(this);
        if (serializer != null) {
            networkLayer.setSerializer(serializer);
        }
        networkLayer.connect();
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
        listener.onError(error);
    }

    @Override
    public void domainModelChanged(final Object root) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                listener.modelReady(root);
            }
        });
        meta.setDoChangesInJavaFxThread(true);
    }

    /**
     * Terminates the connection to the server.
     */
    public void disconnect() {
        networkLayer.disconnect();
    }
}
