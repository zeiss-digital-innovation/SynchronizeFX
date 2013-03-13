/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

package de.saxsys.synchronizefx.core.metamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Merges the list of commands that are used to reproduce the domain model with the list of change commands that
 * occurred while the former list was created.
 */
public final class CommandListMerger {

    private List<Object> reproduceCommands;
    private List<Object> changeCommands;

    private CommandListMerger(final List<Object> reproduceCommands, final List<Object> changeCommands) {
        this.reproduceCommands = reproduceCommands;
        this.changeCommands = changeCommands;
    }

    @SuppressWarnings("serial")
    private List<Object> merge() {
        // TODO strip reproduceCommands of the SetPropertyValue & Co. messages that are made obsolete by changeCommands
        // to avoid double changes.
        return new ArrayList<Object>(reproduceCommands.size() + changeCommands.size()) {
            {
                addAll(reproduceCommands);
                addAll(changeCommands);
            }
        };
    }

    /**
     * See the class Javadoc of {@link CommandListCreator}.
     * 
     * @param reproduceCommands The commands that can be used to reproduce the domain model.
     * @param changeCommands The change commands that occurred while {@code reproduceCommands} was created.
     * @return the merged list
     */
    public static List<Object> merge(final List<Object> reproduceCommands, final List<Object> changeCommands) {
        return new CommandListMerger(reproduceCommands, changeCommands).merge();
    }
}
