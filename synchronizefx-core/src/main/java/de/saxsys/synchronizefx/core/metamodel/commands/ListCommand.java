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
 * Common information for all commands that involve changing a list.
 * 
 * @author michael.thiele
 * @author Raik Bieniek
 */
public abstract class ListCommand implements Command {

    private final UUID listId;
    private final ListVersionChange listVersionChange;

    /**
     * Initializes the command with all values that all list commands have in common.
     * 
     * @param listId
     *            see {@link #getListId()}
     * @param listVersionChange
     *            see {@link #getListVersionChange()}
     */
    protected ListCommand(final UUID listId, final ListVersionChange listVersionChange) {
        this.listId = listId;
        this.listVersionChange = listVersionChange;
    }

    /**
     * The id of the list where a elements should be modified.
     * 
     * @return The id
     */
    public UUID getListId() {
        return listId;
    }

    /**
     * The version the list had before this change and the version the list will have after this change.
     * 
     * @return The versions
     */
    public ListVersionChange getListVersionChange() {
        return listVersionChange;
    }

    /**
     * Describes a change of the version of a list.
     */
    public static class ListVersionChange {
        private final UUID fromVersion;
        private final UUID toVersion;

        /**
         * Creates an instance that describes the change from one specific version to a specific other version.
         * 
         * @param fromVersion
         *            see {@link #getFromVersion()}
         * @param toVersion
         *            see {@link #getToVersion()}
         */
        public ListVersionChange(final UUID fromVersion, final UUID toVersion) {
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
        }

        /**
         * The version the list had before the change.
         * 
         * @return The version
         */
        public UUID getFromVersion() {
            return fromVersion;
        }

        /**
         * The version the list has after the change.
         * 
         * @return The version
         */
        public UUID getToVersion() {
            return toVersion;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fromVersion == null) ? 0 : fromVersion.hashCode());
            result = prime * result + ((toVersion == null) ? 0 : toVersion.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ListVersionChange other = (ListVersionChange) obj;
            if (fromVersion == null) {
                if (other.fromVersion != null) {
                    return false;
                }
            } else if (!fromVersion.equals(other.fromVersion)) {
                return false;
            }
            if (toVersion == null) {
                if (other.toVersion != null) {
                    return false;
                }
            } else if (!toVersion.equals(other.toVersion)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "ListVersionChange [fromVersion=" + fromVersion + ", toVersion=" + toVersion + "]";
        }
    }
}
