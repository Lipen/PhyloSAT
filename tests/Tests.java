import com.beust.jcommander.JCommander;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

class Tests {
    private static void runTest(int expected, String filename) {
        Main main = new Main();
        JCommander j = JCommander.newBuilder()
                .addObject(main)
                .build();
        j.parse("-i", filename, "-p");
        assertEquals(expected, main.run(j));
    }

    public static class MainTests {
        @Test
        public void testEasiest() {
            runTest(1, "test_easiest.trees");
        }

        @Test
        public void testEasy() {
            runTest(1, "test_easy.trees");
        }

        @Test
        public void testFolded() {
            runTest(3, "test_folded.trees");
        }

        @Test
        public void qwerty4() {
            runTest(1, "qwerty4.trees");
        }

        @Test
        public void qwerty5() {
            runTest(2, "qwerty5.trees");
        }

        @Test
        public void testSimple() {
            runTest(3, "test_simple.trees");
        }

        @Test
        public void testIntermediate() {
            runTest(2, "test_intermediate.trees");
        }
    }

    public static class Grass2Tests {
        private final String dir = "data/tests/small/Grass2/";

        @Test
        public void Grass2NdhfWaxy() {
            runTest(9, dir + "Grass2NdhfWaxy.tree.restrict.num");
        }

        @Test
        public void Grass2PhytRbcl() {
            runTest(4, dir + "Grass2PhytRbcl.tree.restrict.num");
        }

        @Test
        public void Grass2PhytRpoc() {
            runTest(4, dir + "Grass2PhytRpoc.tree.restrict.num");
        }

        @Test
        public void Grass2PhytWaxy() {
            runTest(3, dir + "Grass2PhytWaxy.tree.restrict.num");
        }

        @Test
        public void Grass2RbclWaxy() {
            runTest(4, dir + "Grass2RbclWaxy.tree.restrict.num");
        }

        @Test
        public void Grass2RpocWaxy() {
            runTest(5, dir + "Grass2RpocWaxy.tree.restrict.num");
        }

        @Test
        public void Grass2WaxyIts() {
            runTest(5, dir + "Grass2WaxyIts.tree.restrict.num");
        }
    }
}
