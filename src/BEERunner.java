import org.apache.commons.exec.*;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Moklev Vyacheslav
 */
public class BEERunner {
    private static final Pattern PATTERN =
            Pattern.compile("^((?:l|r|c|pl|pr)_\\d+)\\s*=\\s*(\\d+)$");

    private static int execute(String... args) {
        try {
            Process p = Runtime.getRuntime()
                .exec("BumbleBEE " + Arrays.stream(args)
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(" ")));
            int retCode = p.waitFor();
            System.out.println(p.getErrorStream().toString());
            System.out.println(p.getOutputStream().toString());
            return retCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void makeDimacs(String inBEE, String outDimacs, String outMap) {
        try {
            new PrintWriter(outDimacs).close();
            new PrintWriter(outMap).close();
            execute(inBEE, "-dimacs", outDimacs, outMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> resolve(String inBEE, long timeLimit, long[] executionTime) throws IOException {
        String command = "BumbleBEE " + inBEE;
        CommandLine cmdLine = CommandLine.parse(command);

        DefaultExecutor executor = new DefaultExecutor();
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        executor.setExitValue(20);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeLimit);
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errStream);
        executor.setStreamHandler(streamHandler);

        long curTime = System.currentTimeMillis();
        try {
            executor.execute(cmdLine, resultHandler);
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executionTime[0] = System.currentTimeMillis() - curTime;
        if (executionTime[0] > timeLimit) {
            executionTime[0] = -1;
        }

        if (resultHandler.getExitValue() != 0) {
            System.err.println("BumbleBEE exited with error code " + resultHandler.getExitValue());
            System.err.println(outputStream.toString());
            return null;
        }

        if (watchdog.killedProcess()) {
            System.err.println("BumbleBEE process was timeouted");
            return null;
        }

        Map<String, Object> map = new TreeMap<>();
        Scanner input = new Scanner(outputStream.toString());
        String line;
        while (input.hasNextLine()) {
            line = input.nextLine();

            if (line.equals("=====UNSATISFIABLE=====")) {
                return null;
            }

            Matcher matcher = PATTERN.matcher(line);

            if (matcher.find()) {
                String name = matcher.group(1);
                int value = Integer.parseInt(matcher.group(2));
                map.put(name, value);
            }
        }

        return map;
    }
}
