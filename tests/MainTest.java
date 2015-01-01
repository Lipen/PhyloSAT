import junit.framework.TestCase;
import org.apache.commons.exec.*;

import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class MainTest extends TestCase {
    private static String testsPath = "data/tests/";

    private void runTest(String testName) {
        long curTime = System.currentTimeMillis();
        int myResult = new Main().run(new String[]{"-l", "test.log", testName});
        long myTime = System.currentTimeMillis() - curTime;
        assertNotEquals(myResult, -1);
        String pirnCommand = "soft/pirn-v201 " + testName;
        CommandLine cmdLine = CommandLine.parse(pirnCommand);

        DefaultExecutor executor = new DefaultExecutor();
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        executor.setExitValue(0);
        long timeLimit = 1000000;
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeLimit);
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errStream);
        executor.setStreamHandler(streamHandler);

        curTime = System.currentTimeMillis();
        try {
            executor.execute(cmdLine, resultHandler);
            resultHandler.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            assertTrue("pirn failed!", false);
        }

        long executionTime = System.currentTimeMillis() - curTime;
        assertTrue(executionTime < timeLimit);
        System.out.println("My: " + myTime + ", Pirn: " + executionTime);
        Scanner input = new Scanner(outputStream.toString());
        String line;
        while (input.hasNextLine()) {
            line = input.nextLine();
            System.out.println(line);
            if (line.contains("The lowest number of hybridization events found so far is")
                    || line.contains("The minimum number of hybridization events")) {
                String[] tokens = line.split(" ");
                int pirnResult = Integer.parseInt(tokens[tokens.length - 1]);
                line = input.nextLine();
                System.out.println(line);
                if (line.contains("This may not be the optimal solution")) {
                    assertTrue(myResult <= pirnResult);
                } else {
                    assertEquals(myResult, pirnResult);
                }
                return;
            }
        }
        assertTrue("Not enough output from pirn", false);
    }

    public void runDirectory(String dirName){
        File dir = new File(dirName);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                runTest(dirName + "/" + child.getName());
            }
        } else {
            assertTrue(false);
        }
    }

    public void testSmallGrass2() {
        runDirectory(testsPath + "small/Grass2");
    }

    public void testSmallGrass3() {
        runDirectory(testsPath + "small/Grass3");
    }

    public void testSmallGrass4() {
        runDirectory(testsPath + "small/Grass4");
    }

    public void testSmallOthers() {
        runDirectory(testsPath + "small/Others");
    }

    public void testLarge() throws IOException {
        runDirectory(testsPath + "large");
    }
}