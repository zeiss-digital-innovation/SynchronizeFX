package de.saxsys.synchronizefx.core.metamodel.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A command to create a new instance of an observable object.
 * 
 * @author raik.bieniek
 */
public class CreateObservableObject {
    private UUID objectId;
    private String className;
    private Map<String, UUID> propertyNameToId = new HashMap<>();

    /**
     * @return The id this observable object gets.
     */
    public UUID getObjectId() {
        return objectId;
    }

    /**
     * @see CreateObservableObject#getObjectId()
     * @param objectId the id
     */
    public void setObjectId(final UUID objectId) {
        this.objectId = objectId;
    }

    /**
     * @return The class name of the type of this observable object.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @see CreateObservableObject#getClassName()
     * @param className the class name
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * @return A mapping of the names of property fields to the id of the property object.
     */
    public Map<String, UUID> getPropertyNameToId() {
        return propertyNameToId;
    }

    /**
     * @see CreateObservableObject#getPropertyNameToId()
     * @param propertyNameToId the new name-object mapping
     */
    public void setPropertyNameToId(final Map<String, UUID> propertyNameToId) {
        this.propertyNameToId = propertyNameToId;
    }

    @Override
    public String toString() {
        return "CreateObservableObject [objectId=" + objectId + ", className=" + className + ", propertyNameToId="
                + propertyNameToId + "]";
    }
}
