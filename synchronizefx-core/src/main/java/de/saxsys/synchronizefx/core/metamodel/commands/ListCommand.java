package de.saxsys.synchronizefx.core.metamodel.commands;

import java.util.UUID;

/**
 * An interface for all commands that involve changing a list.
 * 
 * @author michael.thiele
 *
 */
public interface ListCommand extends Command {

    /**
     * @return The id of the list where an element should be added.
     */
    UUID getListId();

    /**
     * @see ListCommand#getListId()
     * @param listId the id
     */
    void setListId(final UUID listId);

    /**
     * @return The index the new element will have in the list when it's added / removed / replaced.
     *         <ul>
     *         <li>add: The index of all items in the list thats index is greater or equal to the value returned here
     *         has to be incremented by 1 to make this index available.</li>
     *         <li>remove: The index of all items in the list thats index is greater or equal to the value returned
     *         here has to be decremented by 1 to make this index available.
     *         </ul>
     */
    int getPosition();

    /**
     * @see ListCommand#getPosition()
     * @param position the position
     */
    void setPosition(final int position);
}
