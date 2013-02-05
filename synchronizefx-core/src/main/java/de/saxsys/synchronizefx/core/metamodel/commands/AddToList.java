package de.saxsys.synchronizefx.core.metamodel.commands;

import java.util.UUID;

/**
 * A command that states that an element should be added to a list.
 * 
 * @author raik.bieniek
 * 
 */
public class AddToList {

    private UUID listId;
    private UUID observableObjectId;
    private Object simpleObjectValue;
    private int position;

    /**
     * @return The id of the list where an element should be added.
     */
    public UUID getListId() {
        return listId;
    }

    /**
     * @see AddToList#getListId()
     * @param listId the id
     */
    public void setListId(final UUID listId) {
        this.listId = listId;
    }

    /**
     * @return The id of the observable object that should be added to the list. If this is null, than the value is a
     *         simple object an can be retrieved through through {@link #getSimpleObjectValue()}.
     */
    public UUID getObservableObjectId() {
        return observableObjectId;
    }

    /**
     * @see AddToList#getObservableObjectId()
     * @param observableObjectId the id
     */
    public void setObservableObjectId(final UUID observableObjectId) {
        this.observableObjectId = observableObjectId;
    }

    /**
     * @return The simple object that should be added to the list. The returned value is only valid if
     *         {@link #getObservableObjectId()} returns null.
     */
    public Object getSimpleObjectValue() {
        return simpleObjectValue;
    }

    /**
     * @see AddToList#getSimpleObjectValue()
     * @param simpleObjectValue the value
     */
    public void setSimpleObjectValue(final Object simpleObjectValue) {
        this.simpleObjectValue = simpleObjectValue;
    }

    /**
     * @return The index the new element will have in the list when it's added. The index of all items in the list thats
     *         index is greater or equal to the value returned here has to be incremented by 1 to make this index
     *         available.
     */
    public int getPosition() {
        return position;
    }

    /**
     * @see AddToList#getPosition()
     * @param position the position
     */
    public void setPosition(final int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "AddToList [listId=" + listId + ", observableObjectId=" + observableObjectId + ", simpleObjectValue="
                + simpleObjectValue + ", position=" + position + "]";
    }
}
