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

package de.saxsys.synchronizefx.netty;

import java.util.AbstractList;
import java.util.List;

/**
 * Provides list semantic to a sub list of another list.
 * 
 * This class works only for read only operations. The behavior of this class is only defined as long the backing list
 * is not modified.
 * 
 * @author raik.bieniek
 * 
 * @param <T> the concrete type of the elements in this list.
 */
public class SubList<T> extends AbstractList<T> {
    private final int start;
    private final int size;
    private final List<T> real;

    /**
     * Creates a view on a sub list of an other list.
     * 
     * @param start the first index in the backing list that is included in this sub list.
     * @param end the last index in the backing list that is not included in this sub list. This must be a valid index
     *            greater 0 or {@link List#size()} + 1.
     * @param list the backing list
     */
    public SubList(final int start, final int end, final List<T> list) {
        this.start = start;
        this.size = end - start;
        this.real = list;
    }

    @Override
    public T get(final int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return real.get(start + index);
    }

    @Override
    public int size() {
        return size;
    }
}