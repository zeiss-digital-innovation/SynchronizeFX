package de.saxsys.synchronizefx.core.testutils;

import static org.junit.Assert.fail;

import java.util.List;

import de.saxsys.synchronizefx.core.SynchronizeFXException;
import de.saxsys.synchronizefx.core.metamodel.TopologyLayerCallback;

/**
 * An implementation that saves the parameters of the callback function so that they can be evaluated in tests.
 */
public class SaveParameterCallback implements TopologyLayerCallback {
    private Object root;
    private List<Object> commands;

    @Override
    public void sendCommands(final List<Object> commands) {
        this.commands = commands;
    }

    @Override
    public void onError(final SynchronizeFXException error) {
        fail("exception occured: " + error.getMessage());
    }

    @Override
    public void domainModelChanged(final Object root) {
        this.root = root;
    }

    /**
     * The list with commands that was received last via {@link TopologyLayerCallback#sendCommands(List)}.
     * 
     * @return The list or null if no commands where received yet.
     */
    public List<Object> getCommands() {
        return commands;
    }

    /**
     * The last root object for a domain model that was received via
     * {@link TopologyLayerCallback#domainModelChanged(Object)}.
     * 
     * @return The root object or null if {@link TopologyLayerCallback#domainModelChanged(Object)} was not called yet.
     */
    public Object getRoot() {
        return root;
    }
}