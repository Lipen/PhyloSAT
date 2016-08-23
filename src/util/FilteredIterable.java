package util;

import java.io.File;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * @author Moklev Vyacheslav
 */
public class FilteredIterable implements Iterable<Integer>, Iterator<Integer> {
    private Iterator<? extends Integer> iterator;
    private Predicate<Integer> predicate;
    private Integer cachedValue;

    public FilteredIterable(Predicate<Integer> predicate, Iterable<? extends Integer> iterable) {
        this.predicate = predicate;
        this.iterator = iterable.iterator();
        updateCache();
    }

    private void updateCache() {
        while (iterator.hasNext()) {
            Integer value = iterator.next();
            if (predicate.test(value)) {
                cachedValue = value;
                return;
            }
        }
        cachedValue = null;
    }

    @Override
    public Iterator<Integer> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return cachedValue != null;
    }

    @Override
    public Integer next() {
        try {
            return cachedValue;
        } finally {
            updateCache();
        }
    }

    public static FilteredIterable fromRange(int start, int end, Iterable<Integer> iterable) {
        return new FilteredIterable(x -> x >= start && x <= end, iterable);
    }

    public static FilteredIterable notIs(int banned, Iterable<Integer> iterable) {
        return new FilteredIterable(x -> x != banned, iterable);
    }
}
