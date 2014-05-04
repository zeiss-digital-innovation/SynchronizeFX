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

package de.saxsys.synchronizefx.core.metamodel;

import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

import de.saxsys.synchronizefx.core.exceptions.ObjectToIdMappingException;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceIdentityMap;
import org.apache.commons.collections.map.ReferenceMap;

/**
 * Maps arbitrary objects to {@link UUID} without storing hard references to them.
 * 
 * <p>
 * Objects in this registry that have no hard references to them in some other place may be removed from this registry
 * at any time.
 * </p>
 */
class WeakObjectRegistry {

    private static final String SYNCRONISM_LOST = "In most cases this means that synchronism with other peers "
            + "has been lost.";
    // Apache commons collections are not generic
    @SuppressWarnings("unchecked")
    private Map<Object, UUID> objectToId = new ReferenceIdentityMap(AbstractReferenceMap.WEAK,
            AbstractReferenceMap.HARD);
    @SuppressWarnings("unchecked")
    private Map<UUID, Object> idToObject = new ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.WEAK);

    /**
     * Returns the object that is identified by an id.
     * 
     * @param id
     *            The id of the object that should be returned.
     * @return The object if one is registered by this id or an empty {@link Optional} if not.
     */
    public Optional<Object> getById(final UUID id) {
        return Optional.ofNullable(idToObject.get(id));
    }

    /**
     * Returns the object that is identified by an id.
     * 
     * @param id
     *            The id of the object that should be returned.
     * @return The object if one is registered by this id.
     * @throws ObjectToIdMappingException
     *             When no object with the given id is registered.
     */
    public Object getByIdOrFail(final UUID id) throws ObjectToIdMappingException {
        Optional<Object> object = getById(id);
        if (!object.isPresent()) {
            throw new ObjectToIdMappingException(format(
                    "An object with the id [%s] was expected to be known but it was not.  %s", id.toString(),
                    SYNCRONISM_LOST));
        }
        return object.get();
    }

    /**
     * Returns the id for an object.
     * 
     * @param object
     *            The object thats id should be returned.
     * @return The id of this object if it has an id assigned and an empty {@link Optional} if not.
     */
    public Optional<UUID> getId(final Object object) {
        return Optional.ofNullable(objectToId.get(object));
    }

    /**
     * Returns the id for an object.
     * 
     * @param object
     *            The object thats id should be returned.
     * @return The id of this object if it has an id assigned.
     * @throws ObjectToIdMappingException
     *             When no id for this object has been assigned.
     */
    public UUID getIdOrFail(final Object object) throws ObjectToIdMappingException {
        Optional<UUID> id = getId(object);
        if (!id.isPresent()) {
            throw new ObjectToIdMappingException(format(
                    "An id for the object [%s] was expected to be known but it was not. %s", object.toString(),
                    SYNCRONISM_LOST));
        }
        return id.get();
    }

    /**
     * Registers an object in the meta model if it is not already registered.
     * 
     * @param object
     *            The object to register.
     * @return The id of the object. It doesn't matter if the object was just registered or already known.
     */
    public UUID registerIfUnknown(final Object object) {
        final Optional<UUID> id = getId(object);
        if (!id.isPresent()) {
            return registerObject(object);
        }
        return id.get();
    }

    /**
     * Registers an object in this model identified by a pre existing id.
     * 
     * @param object
     *            The object to register.
     * @param id
     *            The id by which this object is identified.
     */
    public void registerObject(final Object object, final UUID id) {
        objectToId.put(object, id);
        idToObject.put(id, object);
    }

    /**
     * Registers an object in this model identified by a newly created id.
     * 
     * @param object
     *            The object to register.
     * @return The generated id.
     */
    private UUID registerObject(final Object object) {
        UUID id = UUID.randomUUID();
        registerObject(object, id);
        return id;
    }
}
