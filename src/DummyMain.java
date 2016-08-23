import util.Range;

import java.util.Arrays;
import java.util.List;

/**
 * @author Moklev Vyacheslav
 */
public class DummyMain {
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1, 7, 8, 3, 7, 2, 3, 9, 4, 10, 2, -12, 3, 4, 6, 2, 8, 4, 12, 19);
        for (int x: new Range(3, 8).intersect(list)) {
            System.out.print(x + ", ");
        }
    }
}
