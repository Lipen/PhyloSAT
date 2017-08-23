import org.apache.commons.exec.*;

import java.io.*;
import java.util.List;
import java.util.Map;

abstract class Solver {
    abstract List<Map<String, Object>> solve(long timeout);

    final OutputStream runSolver(CommandLine command, long timeout) {
        return runSolver(command, timeout, 0, null);
    }

    final OutputStream runSolver(CommandLine command, long timeout, int successExitValue, int[] ignoredExitValues) {
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        OutputStream outStream = new ByteArrayOutputStream();
        OutputStream errStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outStream, errStream);
        Thread t = new Thread(() -> {
            System.err.println("[!] Destroying process due to main program interrupt");
            watchdog.destroyProcess();
        });

        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValues(ignoredExitValues);
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(streamHandler);

        try {
            Runtime.getRuntime().addShutdownHook(t);
            System.out.println("[.] Executing " + command + "...");
            executor.execute(command, resultHandler);
            resultHandler.waitFor();
            Runtime.getRuntime().removeShutdownHook(t);
        } catch (InterruptedException e) {
            System.err.println("[!] Execution interrupted: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[!] Execution failed: " + e.getMessage());
        }

        if (watchdog.killedProcess()) {
            System.err.printf("[!] %s timeouted (%d)", command.getExecutable(), timeout);
            return null;
        }

        if (resultHandler.getExitValue() != successExitValue) {
            System.err.println("[!] Exitcode: " + resultHandler.getExitValue());
            return null;
        }

        try (FileWriter fw = new FileWriter("log");
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(outStream.toString());
        } catch (IOException e) {
            System.err.println("[!] So sad: " + e.getMessage());
        }

        return outStream;
    }
}
