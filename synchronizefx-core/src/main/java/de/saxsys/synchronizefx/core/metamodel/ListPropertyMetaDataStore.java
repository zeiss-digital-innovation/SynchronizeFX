/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import de.saxsys.synchronizefx.core.exceptions.ObjectToIdMappingException;
import de.saxsys.synchronizefx.core.metamodel.commands.ListCommand;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceIdentityMap;

/**
 * Weakly stores meta data about {@link List}s needed to keep them synchronous with other peers.
 * 
 * <p>
 * Weakly means, that when the garbage collector decides to collect a list properties its version assignment in this
 * class may be collected as well.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class ListPropertyMetaDataStore {

    // Apache commons collections are not generic
    @SuppressWarnings("unchecked")
    private final Map<List<?>, ListPropertyMetaData> listToData = new ReferenceIdentityMap(AbstractReferenceMap.WEAK,
            AbstractReferenceMap.HARD);
    private final WeakObjectRegistry objectRegistry;

    /**
     * Creates a new instance with all its dependencies.
     * 
     * @param objectRegistry
     *            Used to retrieve list by their id.
     */
    public ListPropertyMetaDataStore(final WeakObjectRegistry objectRegistry) {
        this.objectRegistry = objectRegistry;

    }

    /**
     * Returns the meta data for a given {@link List}.
     * 
     * @param list
     *            The list thats meta data should be returned.
     * @return The meta data for the list.
     */
    public ListPropertyMetaData getMetaDataOrFail(final List<?> list) {
        final ListPropertyMetaData metaData = listToData.get(list);
        if (metaData == null) {
            throw new ObjectToIdMappingException("Meta data for an unknown property was requested. "
                    + "The clients may no longer be synchron.");
        }
        return metaData;
    }

    /**
     * Returns the meta data for a {@link List} with a given id.
     * 
     * <p>
     * <em>Performance hint:</em> If a reference to the list property itself is available, the method
     * {@link #getMetaDataOrFail(Object)} should be used as this saves a lookup in a map.
     * </p>
     * 
     * @param listId
     *            The id of the {@link List}t thats meta data should be returned.
     * @return The meta data for the {@link List}t if known.
     * @throws ObjectToIdMappingException
     *             When no meta data for the list with the passed id is known.
     */
    public ListPropertyMetaData getMetaDataOrFail(final UUID listId) throws ObjectToIdMappingException {
        return getMetaDataOrFail((List<?>) objectRegistry.getByIdOrFail(listId));
    }

    /**
     * Stores meta data for a new {@link List}.
     * 
     * @param list
     *            The list that meta data should be stored for.
     * @param metaData
     *            The initial meta data for the new {@link List}.
     * @throws ObjectToIdMappingException
     *             If the list isn't. That means that there is already meta data stored for a {@link List} with
     *             <code>listId</code>
     */
    public void storeMetaDataOrFail(final List<?> list, final ListPropertyMetaData metaData)
            throws ObjectToIdMappingException {
        if (listToData.get(list) != null) {
            throw new ObjectToIdMappingException("Meta data for a known property should be registered twice. "
                    + "The clients may no longer be synchron.");
        }
        listToData.put(list, metaData);
    }

    /**
     * The meta data about a {@link List}.
     *
     */
    public static class ListPropertyMetaData {

        private final LinkedList<ListCommand> unapprovedCommands = new LinkedList<>();
        private UUID localVersion;
        private UUID approvedVersion;

        /**
         * Initializes an instance with initial values.
         * 
         * @param initialLocalVersion
         *            The initial local version of the list.
         * @param initialApprovedVersion
         *            The initial approved version of the list.
         */
        public ListPropertyMetaData(final UUID initialLocalVersion, final UUID initialApprovedVersion) {
            this.localVersion = initialLocalVersion;
            this.approvedVersion = initialApprovedVersion;
        }

        /**
         * The actual local version the list currently has.
         * 
         * <p>
         * Versions of lists are just random {@link UUID}s. When the state of a list is changed it gets a new random
         * {@link UUID} as version.
         * </p>
         * <p>
         * The local version is the "to" version of the last locally generated command in the list of unapproved
         * commands. If there are no unapproved local commands, than this version is the same as the approved version of
         * a list.
         * </p>
         * 
         * @return The local version of the list.
         */
        public UUID getLocalVersion() {
            return localVersion;
        }

        /**
         * Sets the local version for the list.
         * 
         * @see #getLocalVersion()
         * @param newVersion
         *            The new version of the list.
         */
        public void setLocalVersion(final UUID newVersion) {
            localVersion = newVersion;
        }

        /**
         * The approved version the list currently has.
         * 
         * <p>
         * Versions of lists are just random {@link UUID}s. When the state of a list is changed it gets a new random
         * {@link UUID} as version.
         * </p>
         * <p>
         * The approved version of a list is the "from" version of the oldest locally generated change command that was
         * not send back from other peers yet. It is the "to" version of the newest remotely generated change command if
         * there is no local change that wasn't send back from other peers yet.
         * </p>
         * 
         * @return The approved version of the list.
         */
        public UUID getApprovedVersion() {
            return approvedVersion;
        }

        /**
         * Sets the approved version for the list.
         * 
         * @see #getApprovedVersion()
         * @param newVersion
         *            The new approved version the list should have.
         */
        public void setApprovedVersion(final UUID newVersion) {
            approvedVersion = newVersion;
        }

        /**
         * Local commands that have not been approved by other peers yet.
         * 
         * @return the commands
         */
        public Queue<ListCommand> getUnapprovedCommands() {
            return unapprovedCommands;
        }

        /**
         * Same as {@link #getUnapprovedCommands()} but as {@link List} instead of {@link Queue}.
         * 
         * @see #getUnapprovedCommands()
         * @return the commands
         */
        public List<ListCommand> getUnapprovedCommandsAsList() {
            return unapprovedCommands;
        }
    }
}
