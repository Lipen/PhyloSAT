package util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * @author Moklev Vyacheslav
 */
public class Range implements Iterable<Integer>, Iterator<Integer> {
    private int start;
    private int end;
    private int current;

    /**
     * Creates closed range of integers
     *
     * @param start left bound, inclusive
     * @param end right bound, inclusive
     */
    public Range(int start, int end) {
        this.start = start;
        this.end = end;
        current = start;
    }

    @Override
    public Iterator<Integer> iterator() {
        return this;
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

    public FilteredIterable intersect(Iterable<? extends Integer> iterable) {
        return new FilteredIterable(x -> x >= start && x <= end, iterable);
    }
}
