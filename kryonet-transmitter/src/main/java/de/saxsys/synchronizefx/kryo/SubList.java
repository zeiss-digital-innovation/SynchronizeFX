package de.saxsys.synchronizefx.kryo;

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
class SubList<T> extends AbstractList<T> {
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