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
 * A value for properties and collections which is either an <em>observable object</em> or a <em>simple object</em>.
 */
public class Value {

    private UUID observableObjectId;
    private Object simpleObjectValue;

    /**
     * @return If this instance denotes an <em>observable object</em> this returns the id of this object. If this
     *         instance denotes a <em>simple object</em> this method returns <code>null</code>.
     */
    public UUID getObservableObjectId() {
        return observableObjectId;
    }

    /**
     * Sets the id of the <em>observable object</em> this instance denotes.
     * 
     * @throws IllegalStateException
     *             If this instance already denotes a <em>simple object</em>. That is the case when
     *             {@link #getSimpleObjectValue()} does not return <code>null</code>.
     * @see Value#getObservableObjectId()
     * @param observableObjectId
     *            the id
     */
    public void setObservableObjectId(final UUID observableObjectId) throws IllegalStateException {
        if (simpleObjectValue != null) {
            throw new IllegalStateException(
                    "This value already denotes a simple object and can therefor not denote an observable object");
        }
        this.observableObjectId = observableObjectId;
    }

    /**
     * @return If this instance denotes a <em>simple object</em> this returns the value object. If this
     *         instance denotes an <em>observable object</em> this method returns <code>null</code>.
     */
    public Object getSimpleObjectValue() {
        return simpleObjectValue;
    }

    /**
     * Sets the <em>simple object</em> value this instance should wrap.
     * 
     * @throws IllegalStateException
     *             If this instance already denotes an <em>observable object</em>. That is the case when
     *             {@link #getObservableObjectId()} does not return <code>null</code>.
     * @see Value#getSimpleObjectValue()
     * @param simpleObjectValue
     *            the value
     */
    public void setSimpleObjectValue(final Object simpleObjectValue) throws IllegalStateException {
        if (observableObjectId != null) {
            throw new IllegalStateException(
                    "This value already denotes an observable object and can therefor not denote a simple object");
        }
        this.simpleObjectValue = simpleObjectValue;
    }

    @Override
    public String toString() {
        return "Value [observableObjectId=" + observableObjectId + ", simpleObjectValue=" + simpleObjectValue + "]";
    }
}
