package util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * @author Moklev Vyacheslav
 */
public class Range implements Iterable<Integer> {
    private int start;
    private int end;

    /**
     * Creates closed range of integers
     *
     * @param start left bound, inclusive
     * @param end right bound, inclusive
     */
    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new RangeIterator();
    }

    public FilteredIterable intersect(Iterable<? extends Integer> iterable) {
        return new FilteredIterable(x -> x >= start && x <= end, iterable);
    }

    public boolean contains(int x) {
        return x >= start && x <= end;
    }

    private class RangeIterator implements Iterator<Integer> {
        private int current;

        public RangeIterator() {
            current = start;
        }

        @Override
        public boolean hasNext() {
            return current <= end;
        }

        @Override
        public Integer next() {
            if (current <= end) {
                return current++;
            }
            throw new NoSuchElementException();
        }
    }
}
