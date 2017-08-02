import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

class Runner {
    private static class MyExecutor extends DefaultExecutor implements AutoCloseable {
        @Override
        public void close() {
            this.getWatchdog().destroyProcess();
        }
    }

    private static final Pattern PATTERN =
            Pattern.compile("^(p_\\d+(?:_\\d+)?)\\s*=\\s*(\\d+|true|false)$");

    static Map<String, Object> resolve(String filename, long timeLimit, long[] executionTime) {
        CommandLine command = new CommandLine("BumbleBEE").addArgument(filename);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeLimit);
        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
        Thread t = new Thread(() -> {
            System.err.println("[!] Destroying process due to main program interrupt");
            watchdog.destroyProcess();
        });

        try (MyExecutor executor = new MyExecutor()) {
            executor.setWatchdog(watchdog);
            executor.setStreamHandler(streamHandler);

            long curTime = System.currentTimeMillis();
            try {
                Runtime.getRuntime().addShutdownHook(t);
                executor.execute(command, resultHandler);
                resultHandler.waitFor();
                Runtime.getRuntime().removeShutdownHook(t);
            } catch (InterruptedException e) {
                System.err.println("[!] Execution interrupted: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("[!] Execution failed: " + e.getMessage());
                e.printStackTrace();
            }

            executionTime[0] = System.currentTimeMillis() - curTime;
            if (timeLimit != INFINITE_TIMEOUT && executionTime[0] > timeLimit) {
                executionTime[0] = -1;
            }

            if (resultHandler.getExitValue() != 0) {
                System.err.println("[!] BumbleBEE exitcode: " + resultHandler.getExitValue());
                // System.err.println(outputStream.toString());
                // System.err.println(errorStream.toString());
                return null;
            }

            if (watchdog.killedProcess()) {
                System.err.println("[!] BumbleBEE timeouted.");
                return null;
            }

            Map<String, Object> solution = new HashMap<>();
            Scanner input = new Scanner(outputStream.toString());
            String line;
            while (input.hasNextLine()) {
                line = input.nextLine();
                if (line.equals("=====UNSATISFIABLE====="))
                    return null;

                Matcher matcher = PATTERN.matcher(line);

                if (matcher.find()) {
                    String name = matcher.group(1);
                    try {
                        int value = Integer.parseInt(matcher.group(2));
                        solution.put(name, value);
                    } catch (NumberFormatException e) {
                        boolean value = Boolean.parseBoolean(matcher.group(2));
                        solution.put(name, value);
                    }
                }
            }

            return solution;
        }
    }
}
