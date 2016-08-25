package beepp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Moklev Vyacheslav
 */
public class RangeUnion {
    private List<AtomicRange> ranges;

    public RangeUnion() {
        ranges = new ArrayList<>();
    }

    public RangeUnion(int... bounds) {
        this();
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
        AtomicRange newRange = new AtomicRange(left, right);
        int leftIntersectionId = -1;
        int rightIntersectionId = -1;
        for (int i = 0; i < ranges.size(); i++) {
            AtomicRange range = ranges.get(i);
            if (range.contains(newRange)) {
                return;
            }
            if (newRange.contains(range)) {
                ranges.set(i, null);
            } else {
                if (range.contains(left))
                    leftIntersectionId = i;
                if (range.contains(right))
                    rightIntersectionId = i;
            }
        }
        if (leftIntersectionId >= 0) {
            newRange.left = ranges.get(leftIntersectionId).left;
            ranges.set(leftIntersectionId, null);
        }
        if (rightIntersectionId >= 0) {
            newRange.right = ranges.get(rightIntersectionId).right;
            ranges.set(rightIntersectionId, null);
        }
        ranges.add(newRange);
        ranges = ranges.stream().filter(r -> r != null).collect(Collectors.toList());
        Collections.sort(ranges);
    }

    public boolean isAtomicRange() {
        return ranges.size() == 1;
    }

    private static boolean ascending(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i] < a[i - 1])
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ranges.toString();
    }

    private static class AtomicRange implements Comparable<AtomicRange> {
        private int left;
        private int right;

        public AtomicRange(int left, int right) {
            if (left > right)
                throw new IllegalArgumentException("left > right: " + left + " > " + right);
            this.left = left;
            this.right = right;
        }

        public boolean contains(int x) {
            return x >= left && x <= right;
        }

        public boolean contains(AtomicRange another) {
            return another.left >= left && another.right <= right;
        }

        public boolean intersects(AtomicRange another) {
            return contains(another.left) || contains(another.right);
        }

        @Override
        public int compareTo(AtomicRange o) {
            if (o == null)
                return -1;
            return left - o.left;
        }

        @Override
        public String toString() {
            return "[" + left + ", " + right + "]";
        }
    }
}