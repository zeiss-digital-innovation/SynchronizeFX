package de.saxsys.synchronizefx.core.metamodel.commands;

import java.util.List;
import java.util.UUID;

/**
 * A command to replace elements in a list.
 * 
 * @see {@link List#set(int, Object)}
 * 
 * @author michael.thiele
 *
 */
public class ReplaceInList implements ListCommand {

    private UUID listId;
    private int position;
    private Value value;

    @Override
    public UUID getListId() {
        return listId;
    }

    @Override
    public void setListId(final UUID listId) {
        this.listId = listId;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(final int position) {
        this.position = position;
    }

    /**
     * @return The value to add to the list.
     */
    public Value getValue() {
        return value;
    }

    /**
     * @see #getValue()
     * @param value the value
     */
    public void setValue(final Value value) {
        this.value = value;
    }
}
