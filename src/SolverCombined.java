import org.apache.commons.exec.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

@SuppressWarnings("Duplicates")
final class SolverCombined extends Solver {
    private static final Pattern SOLUTION_PATTERN =
            Pattern.compile("^(p_\\d+(?:_\\d+)?)\\s*=\\s*(\\d+|true|false)$");

    private String inputFileName;

    SolverCombined(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    @Override
    Map<String, Object> resolve(long timeLimit, long[] executionTime) {
        System.out.println("[.] Using built-it solver");

        CommandLine command = new CommandLine("BumbleBEE").addArgument(inputFileName);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeLimit);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
        Thread t = new Thread(() -> {
            System.err.println("[!] Destroying process due to main program interrupt");
            watchdog.destroyProcess();
        });

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(streamHandler);

        long time = System.currentTimeMillis();
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

        executionTime[0] = System.currentTimeMillis() - time;
        if (timeLimit != INFINITE_TIMEOUT && executionTime[0] > timeLimit)
            executionTime[0] = -1;

        if (watchdog.killedProcess()) {
            System.err.println("[!] Timeout");
            return null;
        }

        if (resultHandler.getExitValue() != 0) {
            System.err.println("[!] Exitcode: " + resultHandler.getExitValue());
            return null;
        }

        Map<String, Object> solution = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new StringReader(outputStream.toString()))) {
            for (String line : (Iterable<String>) br.lines()::iterator) {
                if (line.equals("=====UNSATISFIABLE====="))
                    return null;

                Matcher matcher = SOLUTION_PATTERN.matcher(line);

                if (matcher.matches()) {
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
        } catch (IOException e) {
            System.err.println("[!] So sad: " + e.getMessage());
            return null;
        }

        return solution;
    }
}
