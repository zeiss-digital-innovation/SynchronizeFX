package de.saxsys.synchronizefx.core.metamodel;

import java.util.List;

import de.saxsys.synchronizefx.core.SynchronizeFXException;

/**
 * This is an callback interface for the meta model.
 * 
 * The meta model uses it to inform the underlying layer on several events like new commands that have to be transfered
 * to other peers or that an error occurred in the meta model layer.
 * 
 * @author raik.bieniek
 * 
 */
public interface TopologyLayerCallback {

    /**
     * Called when the meta model layer has produced commands that need to be shared with other meta models.
     * 
     * @param commands The commands that need to be shared.
     */
    void sendCommands(List<Object> commands);

    /**
     * Called when the domain model has changed.
     * 
     * This method is most probably called only once. That is when the initial domain model has been transfered
     * completely to a peer.
     * 
     * @param root The root object of the new domain model.
     */
    void domainModelChanged(Object root);

    /**
     * Called when an error occurred in the meta model layer.
     * 
     * If an error occurred, synchronicity between clients can no longer be guaranteed. In some cases the meta model can
     * still be used and the error be ignored.
     * 
     * @param error The exception that caused the error.
     */
    void onError(SynchronizeFXException error);
}
