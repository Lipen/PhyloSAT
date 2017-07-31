import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Runner {
    private static final Pattern PATTERN =
            Pattern.compile("^(p_\\d+(?:_\\d+)?)\\s*=\\s*(\\d+|true|false)$");

    static Map<String, Object> resolve(String inBEE, long timeLimit, long[] executionTime) throws IOException {
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
        if (timeLimit != ExecuteWatchdog.INFINITE_TIMEOUT && executionTime[0] > timeLimit) {
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
                try {
                    int value = Integer.parseInt(matcher.group(2));
                    map.put(name, value);
                } catch (NumberFormatException e) {
                    boolean value = Boolean.parseBoolean(matcher.group(2));
                    map.put(name, value);
                }
            }
        }

        return map;
    }
}
