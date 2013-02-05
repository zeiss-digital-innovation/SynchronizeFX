package de.saxsys.synchronizefx.core.metamodel.commands;

import java.util.UUID;

/**
 * A message that tells which object should be treated as the root object of the domain model.
 * 
 * @author raik.bieniek
 * 
 */
public class SetRootElement {
    private UUID rootElementId;

    /**
     * @return The id of the root element.
     */
    public UUID getRootElementId() {
        return rootElementId;
    }

    /**
     * 
     * @see SetRootElement#getRootElementId()
     * @param rootElementId the id
     */
    public void setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
    }

    @Override
    public String toString() {
        return "SetRootElement [rootElementId=" + rootElementId + "]";
    }
}
