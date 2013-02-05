package de.saxsys.synchronizefx.core.clientserver;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.saxsys.synchronizefx.core.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.MetaModel;
import de.saxsys.synchronizefx.core.metamodel.TopologyLayerCallback;

/**
 * The internal implementation that does all the work for {@link DomainModelServer}.
 * 
 * The purpose of this class is to hide methods that are meant to be used by the framework from the user.
 * 
 * @author raik.bieniek
 * 
 */
class DomainModelServerInternal implements NetworkToTopologyCallbackServer, TopologyLayerCallback {
    private static final Logger LOG = LoggerFactory.getLogger(DomainModelServerInternal.class);

    private MessageTransferServer networkLayer;
    private MetaModel meta;
    private UserCallbackServer user;

    // CHECKSTYLE:OFF The signature for the other constructor is to long to fit in 120 characters
    /**
     * @see DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer);
     * @param model see
     *            {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer)}
     * @param networkLayer see
     *            {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer)}
     * @param serializer see
     *            {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer)}
     * @param user {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer)}
     * @throws SynchronizeFXException
     *             {@link DomainModelServer#DomainModelServer(Object, MessageTransferServer, Serializer, UserCallbackServer)}
     */
    // CHECKSTYLE:ON
    public DomainModelServerInternal(final Object model, final MessageTransferServer networkLayer,
            final Serializer serializer, final UserCallbackServer user) throws SynchronizeFXException {
        this.networkLayer = networkLayer;
        this.user = user;
        this.meta = new MetaModel(this, model);
        networkLayer.setTopologyLayerCallback(this);
        if (serializer != null) {
            networkLayer.setSerializer(serializer);
        }
        networkLayer.start();
    }

    @Override
    public void recive(final List<Object> messages, final Object sender) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Server recived commands " + messages);
        }

        meta.execute(messages);
        networkLayer.sendToAllExcept(messages, sender);
    }

    @Override
    public void sendCommands(final List<Object> commands) {
        networkLayer.sendToAll(commands);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Server sent commands " + commands);
        }
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        user.onError(error);
    }

    @Override
    public void domainModelChanged(final Object root) {
        user.onError(new SynchronizeFXException("Domain model has changed on the server side. "
                + "This is not supported. "
                + "If you want to serve a new domain model, consider creating an a new Server "
                + "or create a meta root that holds the real root object of your domain model "
                + "wich than can be exchanged without problems."));
    }

    /**
     * Sends the current domain model to a newly connecting client.
     * 
     * @param newClient An object that represent the new client that connected.
     * @see IncommingEventHandlerServer#onConnect(Object)
     */
    @Override
    public void onConnect(final Object newClient) {
        networkLayer.send(meta.commandsForDomainModel(), newClient);
    }

    /**
     * Logs the unexpected disconnection of an client.
     * 
     * Connection errors to single clients are usually non fatal. The server can still work correctly for the other
     * clients. Because of that this type of error is just logged here and not passed to the user.
     * 
     * @param e an exception that describes the problem.
     * 
     * @see NetworkToTopologyCallbackServer#onClientConnectionError(SynchronizeFXException)
     */
    @Override
    public void onClientConnectionError(final SynchronizeFXException e) {
        LOG.warn("Client connected unexpectetly", e);
    }

    @Override
    public void onFatalError(final SynchronizeFXException e) {
        user.onError(e);
    }
    
    /**
     * Shuts down the server
     * 
     * Before the shutdown, all clients which are still connected are disconnected.
     */
    public void shutdown() {
        networkLayer.shutdown();
    }
}
