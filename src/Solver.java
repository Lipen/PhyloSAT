import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

abstract class Solver {
    abstract Map<String, Object> solve();

    protected final OutputStream runSolver(CommandLine command) {
        return runSolver(command, 0, null);
    }

    protected final OutputStream runSolver(CommandLine command, int successExitValue) {
        return runSolver(command, successExitValue, null);
    }

    protected final OutputStream runSolver(CommandLine command, int successExitValue, int[] ignoredExitValues) {
        ExecuteWatchdog watchdog = new ExecuteWatchdog(INFINITE_TIMEOUT);
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

        if (resultHandler.getExitValue() != successExitValue) {
            System.err.println("[!] Exitcode: " + resultHandler.getExitValue());
            return null;
        }

        return outStream;
    }
}
