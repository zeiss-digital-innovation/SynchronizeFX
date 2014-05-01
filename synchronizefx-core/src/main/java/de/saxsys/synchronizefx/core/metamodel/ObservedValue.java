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

import javafx.beans.property.Property;

/**
 * A value for an observable which can be an <em>observable object</em> if it contains {@link Property} fields or a
 * <em>simple object</em> if it does not.
 * 
 * <p>
 * The {@link Property}s of <em>observable object</em> can be monitored for changes too while <em>simple objects</em>
 * cannot.
 * </p>
 */
class ObservedValue {

    private boolean observable;
    private Object value;

    /**
     * Initializes this value class.
     * 
     * @param value
     *            The value to wrap.
     * @param isObservable
     *            <code>true</code> when <code>value</code> is an <em>observable object</em>, <code>false</code> if it
     *            is a <em>simple objects</em>.
     */
    public ObservedValue(final Object value, final boolean isObservable) {
        this.observable = isObservable;
        this.value = value;
    }

    /**
     * The value that is wrapped.
     * 
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Whether this value is an observable object or a simple object.
     * 
     * @return <code>true</code> if this instance contains an <em>observable object</em> and <code>false</code> if it
     *         contains a <em>simple object</em>.
     */
    public boolean isObservable() {
        return observable;
    }
}
