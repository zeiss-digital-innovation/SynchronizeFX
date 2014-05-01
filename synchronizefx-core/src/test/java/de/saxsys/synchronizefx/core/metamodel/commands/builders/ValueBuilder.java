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

package de.saxsys.synchronizefx.core.metamodel.commands.builders;

import java.util.Random;
import java.util.UUID;

import de.saxsys.synchronizefx.core.metamodel.commands.Value;

/**
 * Builds a {@link Value} instance with dummy data for unset fields.
 */
public class ValueBuilder {

    private final Value value = new Value();

    /**
     * @see Value#getObservableObjectId().
     * @param id
     *            the observable object id
     * @return this builder
     */
    public ValueBuilder withObservableObjectId(final UUID id) {
        value.setObservableObjectId(id);
        return this;
    }

    /**
     * @see Value#getSimpleObjectValue()
     * @param simpleObjectValue
     *            the simple object
     * @return this builder
     */
    public ValueBuilder withSimpleObject(final Object simpleObjectValue) {
        value.setSimpleObjectValue(simpleObjectValue);
        return this;
    }

    /**
     * Builds the {@link Value}.
     * 
     * @return the value
     */
    public Value build() {
        if (value.getObservableObjectId() == null && value.getSimpleObjectValue() == null) {
            value.setSimpleObjectValue(createRandomObject());
        }
        return value;
    }

    /**
     * Creates a builder that will build a {@link Value} that contains a simple object.
     * 
     * @return the builder
     */
    public static ValueBuilder randomSimpleObjectMessage() {
        return new ValueBuilder().withSimpleObject(createRandomObject());
    }

    /**
     * Creates a new value builder.
     * 
     * @return The builder created
     */
    public static ValueBuilder valueMessage() {
        return new ValueBuilder();
    }

    private static Object createRandomObject() {
        return "SAMPLE SIMPLE OBJECT " + new Random().nextInt();
    }
}
