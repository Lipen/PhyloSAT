import com.beust.jcommander.JCommander;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainTests {
    private static void runTest(int expected, String name) {
        String filename = String.format("%s.trees", name);
        Main main = new Main();
        JCommander j = JCommander.newBuilder()
                .addObject(main)
                .build();
        j.parse("-i", filename, "-p");
        assertEquals(expected, main.run(j));
    }

    @Test
    public void testEasiest() {
        runTest(1, "test_easiest");
    }

    @Test
    public void testEasy() {
        runTest(1, "test_easy");
    }

    @Test
    public void testFolded() {
        runTest(3, "test_folded");
    }

    @Test
    public void qwerty4() {
        runTest(1, "qwerty4");
    }

    @Test
    public void qwerty5() {
        runTest(2, "qwerty5");
    }

    @Test
    public void testNormalization() {
        runTest(1, "test_normalization");
    }

    @Test
    public void testSimple() {
        runTest(3, "test_simple");
    }

    @Test
    public void testIntermediate() {
        runTest(2, "test_intermediate");
    }
}
