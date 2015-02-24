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

/**
 * Keeps hard references to arbitrary objects for 1 minute to prevent them from being garbage-collected to early.
 * 
 * @author Raik Bieniek
 */
public class TemporaryReferenceKeeper {

    /**
     * Keeps a hard reference to the passed object for 1 minute.
     * 
     * @param object
     *            The object to keep a hard reference to.
     */
    public void keepReferenceTo(final Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not implemented yet");
    }

}
