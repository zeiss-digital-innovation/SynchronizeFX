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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Keeps hard references to arbitrary objects for at least 1 minute to prevent them from being garbage-collected to
 * early.
 * 
 * <p>
 * To prevent usage of background threads, users of this class must call {@link #cleanReferenceCache()} to trigger
 * the clean up.
 * </p>
 * 
 * @author Raik Bieniek
 */
public class TemporaryReferenceKeeper {

    /**
     * The time to keep hard references in milliseconds.
     */
    private static final long REFERENCE_KEEPING_TIME = 60000;

    private final List<HardReference> hardReferences = new LinkedList<>();
    private final Supplier<Date> currentDateSupplier;

    /**
     * Initializes an instance with all its dependencies.
     * 
     * @param currentDateSupplier A supplier that should return the current date each time it is called.
     */
    public TemporaryReferenceKeeper(final Supplier<Date> currentDateSupplier) {
        this.currentDateSupplier = currentDateSupplier;
    }

    /**
     * Keeps a hard reference to the passed object for 1 minute.
     * 
     * <p>
     * When using this method you must call {@link #cleanReferenceCache()} to trigger a clean up.
     * </p>
     * 
     * @param object The object to keep a hard reference to.
     */
    public void keepReferenceTo(final Object object) {
        hardReferences.add(new HardReference(object, currentDateSupplier.get()));
    }

    /**
     * Checks and cleans cached references that have timed out.
     */
    public void cleanReferenceCache() {
        final long now = currentDateSupplier.get().getTime();
        final Iterator<HardReference> it = hardReferences.iterator();
        while (it.hasNext()) {
            if (it.next().keeptSince.getTime() + REFERENCE_KEEPING_TIME <= now) {
                it.remove();
            }
        }
    }

    /**
     * The list of all references that are currently kept.
     * 
     * <p>
     * This is only intended to be used by tests.
     * </p>
     * 
     * @return All references that are currently held by this instance.
     */
    Iterable<Object> getHardReferences() {
        final List<Object> references = new ArrayList<Object>(hardReferences.size());
        for (final HardReference reference : hardReferences) {
            references.add(reference.referenceToKeep);
        }
        return references;
    }

    /**
     * A task that just keeps a hard reference to an object.
     */
    private static class HardReference {

        private final Object referenceToKeep;
        private final Date keeptSince;

        HardReference(final Object referenceToKeep, final Date keeptSince) {
            this.referenceToKeep = referenceToKeep;
            this.keeptSince = keeptSince;
        }
    }
}
