import com.beust.jcommander.JCommander;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainHardTests {
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
    public void testMedium() {
        runTest(5, "test_medium");
    }

    @Test
    public void testHard() {
        runTest(7, "test_hard");
    }
}
