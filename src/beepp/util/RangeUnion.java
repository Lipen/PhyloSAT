package beepp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Moklev Vyacheslav
 */
public class RangeUnion {
    private List<AtomicRange> ranges;

    public RangeUnion() {
        ranges = new ArrayList<>();
    }

    public RangeUnion(int... bounds) {
        if (bounds.length % 2 == 1) {
            throw new IllegalArgumentException("Odd number of bounds");
        }
        if (!ascending(bounds)) {
            throw new IllegalArgumentException("Bounds are not sorted in the ascending order");
        }
        for (int i = 0; i < bounds.length / 2; i++) {
            ranges.add(new AtomicRange(bounds[2 * i], bounds[2 * i + 1]));
        }
    }

    public void addRange(int left, int right) {
        // TODO implement
    }

    private static boolean ascending(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i] < a[i - 1])
                return false;
        }
        return true;
    }

    private static class AtomicRange {
        private int left;
        private int right;

        public AtomicRange(int left, int right) {
            if (left > right)
                throw new IllegalArgumentException("left > right: " + left + " > " + right);
            this.left = left;
            this.right = right;
        }

        public boolean contains(AtomicRange another) {
            return another.right >= left && another.left <= right;
        }

        // TODO intersect
    }
}
