package util;

import beepp.expression.IntegerConstant;

import java.util.*;

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
     * @param end   right bound, inclusive
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

    public Iterable<Integer> union(Iterable<Integer> iterable) {
        Set<Integer> used = new HashSet<>();
        for (Integer x : Range.this) {
            used.add(x);
        }
        for (Integer x : iterable) {
            used.add(x);
        }
        return used;
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
