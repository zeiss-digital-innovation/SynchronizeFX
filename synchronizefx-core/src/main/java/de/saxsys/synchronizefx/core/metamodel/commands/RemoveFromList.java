package de.saxsys.synchronizefx.core.metamodel.commands;

import java.util.UUID;

/**
 * Command to remove an element in a list.
 * 
 * @author raik.bieniek
 * 
 */
public class RemoveFromList {
    private UUID listId;
    private int position;

    /**
     * @return The id of the list where an element should be removed.
     */
    public UUID getListId() {
        return listId;
    }

    /**
     * 
     * @see RemoveFromList#getListId()
     * @param listId the id
     */
    public void setListId(final UUID listId) {
        this.listId = listId;
    }

    /**
     * @return The index of the element that should be removed in the list.
     */
    public int getPosition() {
        return position;
    }

    /**
     * 
     * @see RemoveFromList#getPosition()
     * @param position the position
     */
    public void setPosition(final int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "RemoveFromList [listId=" + listId + ", position=" + position + "]";
    }
}
