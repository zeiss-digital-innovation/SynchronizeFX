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

package de.saxsys.synchronizefx.core.exceptions;

/**
 * A exception for any kind of failures that appear in this framework.
 * 
 * @author raik.bieniek
 * 
 */
public class SynchronizeFXException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param e The exception that caused this failure.
     */
    public SynchronizeFXException(final Throwable e) {
        super(e);
    }

    /**
     * 
     * @param message A user readable message that describes the problem and optionally some advice for the user how the
     *            problem can be fixed.
     * @param e The exception that caused this failure.
     */
    public SynchronizeFXException(final String message, final Throwable e) {
        super(message, e);
    }

    /**
     * 
     * @param message A user readable message that describes the problem and optionally some advice for the user how the
     *            problem can be fixed.
     */
    public SynchronizeFXException(final String message) {
        super(message);
    }
}
