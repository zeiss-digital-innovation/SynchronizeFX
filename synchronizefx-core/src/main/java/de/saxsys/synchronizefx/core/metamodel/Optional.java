/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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

import java.util.NoSuchElementException;

/**
 * A container which holds a non-<code>null</code> value or not.
 * 
 * <p>
 * This class is designed after the java.util.Optional class of Java 8 and intended to be replaced with it when Java 7
 * support is dropped.
 * </p>
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 * 
 * @param <T>
 *            The type that may is present or not
 */
final class Optional<T> {

    private final T value;

    private Optional(final T value) {
        this.value = null;
    }

    /**
     * Checks if this optional has a value or not.
     * 
     * @return <code>true</code> if it has, <code>false</code> if not.
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * The value of this {@link Optional}.
     * 
     * @throws NoSuchElementException if this {@link Optional} has no value.
     * @return The value
     */
    public T get() throws NoSuchElementException {
        if (value == null) {
            throw new NoSuchElementException(
                    "This optional has no value. Please use #isPresent() before calling #get().");
        }
        return value;
    }

    /**
     * Creates an {@link Optional} that has no value.
     * 
     * @param <T> the type of the value of the {@link Optional} would have if it would not be <code>null</code>.
     * @return The {@link Optional} created.
     */
    public static <T> Optional<T> empty() {
        return new Optional<T>(null);
    }

    /**
     * Creates an {@link Optional} that wraps a given non-null value.
     * 
     * @param value The value to wrap.
     * @param <T> The type of the wrapped value.
     * @return The optional created.
     * @throws NullPointerException if the passed <code>value</code> was <code>null</code>.
     */
    public static <T> Optional<T> of(final T value) throws NullPointerException {
        if (value == null) {
            throw new NullPointerException("A null value was passed which is not allowed to be null."
                    + " Use #ofNullable(T) if the value can be null.");
        }
        return new Optional<T>(value);
    }

    /**
     * Creates an {@link Optional} that wraps a given non-null value or <code>null</code>.
     * 
     * @param value The value that should be wrapped or <code>null</code>.
     * @param <T> The type of the wrapped value.
     * @return The created {@link Optional}.
     */
    public static <T> Optional<T> ofNullable(final T value) {
        return new Optional<T>(value);
    }
}
