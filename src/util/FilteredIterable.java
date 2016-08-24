package util;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * @author Moklev Vyacheslav
 */
public class FilteredIterable implements Iterable<Integer> {
    private Iterable<? extends Integer> iterable;
    private Predicate<Integer> predicate;

    public FilteredIterable(Predicate<Integer> predicate, Iterable<? extends Integer> iterable) {
        this.predicate = predicate;
        this.iterable = iterable;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new FilteredIterator();
    }

    public static FilteredIterable fromRange(int start, int end, Iterable<Integer> iterable) {
        return new FilteredIterable(x -> x >= start && x <= end, iterable);
    }

    public static FilteredIterable notIs(int banned, Iterable<Integer> iterable) {
        return new FilteredIterable(x -> x != banned, iterable);
    }

    private class FilteredIterator implements Iterator<Integer> {
        private Iterator<? extends Integer> iterator;
        private Integer cachedValue;

        public FilteredIterator() {
            iterator = iterable.iterator();
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
    }

}
