import com.beust.jcommander.JCommander;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Grass2Tests {
    private static void runTest(int expected, String name) {
        String filename = String.format("data/tests/small/Grass2/%s.tree.restrict.num", name);
        Main main = new Main();
        JCommander j = JCommander.newBuilder()
                .addObject(main)
                .build();
        j.parse("-i", filename, "-p");
        assertEquals(expected, main.run(j));
    }

    @Ignore
    @Test
    public void Grass2NdhfWaxy() {
        runTest(9, "Grass2NdhfWaxy");
    }

    @Test
    public void Grass2PhytRbcl() {
        runTest(4, "Grass2PhytRbcl");
    }

    @Test
    public void Grass2PhytRpoc() {
        runTest(4, "Grass2PhytRpoc");
    }

    @Test
    public void Grass2PhytWaxy() {
        runTest(3, "Grass2PhytWaxy");
    }

    @Test
    public void Grass2RbclWaxy() {
        runTest(4, "Grass2RbclWaxy");
    }

    @Test
    public void Grass2RpocWaxy() {
        runTest(2, "Grass2RpocWaxy");
    }

    @Test
    public void Grass2WaxyIts() {
        runTest(5, "Grass2WaxyIts");
    }
}
