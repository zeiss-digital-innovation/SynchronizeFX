package de.saxsys.synchronizefx.core.metamodel.commands;

import java.util.UUID;

/**
 * A command to set a value for some key in some map.
 * 
 * @author raik.bieniek
 */
public class PutToMap {
    private UUID mapId;
    private UUID keyObservableObjectId;
    private Object keySimpleObjectValue;
    private UUID valueObservableObjectId;
    private Object valueSimpleObjectValue;

    /**
     * @return the id of the map where a value for a key should be put.
     */
    public UUID getMapId() {
        return mapId;
    }

    /**
     * @see PutToMap#getMapId()
     * @param mapId the id
     */
    public void setMapId(final UUID mapId) {
        this.mapId = mapId;
    }

    /**
     * @return the id for the observable object that represents the key for this mapping. If this is null, than the key
     *         is a simple object and can be retrieved via {@link PutToMap#getKeySimpleObjectValue()}.
     */
    public UUID getKeyObservableObjectId() {
        return keyObservableObjectId;
    }

    /**
     * @see PutToMap#getKeyObservableObjectId()
     * @param keyObservableObjectId the id
     */
    public void setKeyObservableObjectId(final UUID keyObservableObjectId) {
        this.keyObservableObjectId = keyObservableObjectId;
    }

    /**
     * @return the object that represents the key for this mapping. If this is null than the key is an observable object
     *         whose id can be retrieved via {@link PutToMap#getKeyObservableObjectId()}.
     */
    public Object getKeySimpleObjectValue() {
        return keySimpleObjectValue;
    }

    /**
     * @see PutToMap#getKeySimpleObjectValue()
     * @param keySimpleObjectValue the key
     */
    public void setKeySimpleObjectValue(final Object keySimpleObjectValue) {
        this.keySimpleObjectValue = keySimpleObjectValue;
    }

    /**
     * 
     * @return the id for the observable object that represents the value for this mapping. If this is null, than the
     *         value either is a simple object which can be retrieved via {@link PutToMap#getValueSimpleObjectValue()}
     *         or if this method returns null too, the value is really null.
     */
    public UUID getValueObservableObjectId() {
        return valueObservableObjectId;
    }

    /**
     * 
     * @param valueObservableObjectId the id
     * @see PutToMap#getValueObservableObjectId()
     */
    public void setValueObservableObjectId(final UUID valueObservableObjectId) {
        this.valueObservableObjectId = valueObservableObjectId;
    }

    /**
     * @return the object that represents the value for this mapping. If this is null than the value either is an
     *         observable object whose id can be retrieved via {@link PutToMap#getValueObservableObjectId()} or if this
     *         method returns null too, the value is really null.
     */
    public Object getValueSimpleObjectValue() {
        return valueSimpleObjectValue;
    }

    /**
     * @param valueSimpleObjectValue the object
     * @see PutToMap#getValueSimpleObjectValue()
     */
    public void setValueSimpleObjectValue(final Object valueSimpleObjectValue) {
        this.valueSimpleObjectValue = valueSimpleObjectValue;
    }
    
    @Override
    public String toString() {
        return "PutToMap [mapId=" + mapId + ", keyObservableObjectId=" + keyObservableObjectId
                + ", keySimpleObjectValue=" + keySimpleObjectValue + ", valueObservableObjectId="
                + valueObservableObjectId + ", valueSimpleObjectValue=" + valueSimpleObjectValue + "]";
    }
}
