/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

package de.saxsys.synchronizefx.core.metamodel.commands;

import java.util.UUID;

/**
 * An interface for all commands that involve changing a list.
 * 
 * @author michael.thiele
 * @author Raik Bieniek
 */
public abstract class ListCommand implements Command {

    private final UUID listId;
    private final int listVersion;

    /**
     * Initializes the command with all values that all list commands have in common.
     * 
     * @param listId
     *            see {@link #getListId()}
     * @param listVersion
     *            see {@link #getListVersion()}
     */
    protected ListCommand(final UUID listId, final int listVersion) {
        this.listId = listId;
        this.listVersion = listVersion;
    }

    /**
     * The id of the list where a elements should be modified.
     * 
     * @return The id
     */
    public UUID getListId() {
        return listId;
    };

    /**
     * The version of the list on which this command can be applied.
     * 
     * <p>
     * After the command has been applied on the list its version must be increased by one. Integer overflows of version
     * numbers are allowed.
     * </p>
     * 
     * @return The version.
     */
    public int getListVersion() {
        return listVersion;
    }
}
