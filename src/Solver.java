import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

abstract class Solver {
    abstract Map<String, Object> solve();

    protected final OutputStream runSolver(CommandLine command) {
        return runSolver(command, 0);
    }

    protected final OutputStream runSolver(CommandLine command, int exitValue) {
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
        // executor.setExitValue(exitValue);
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(streamHandler);

        try {
            Runtime.getRuntime().addShutdownHook(t);
            executor.execute(command, resultHandler);
            resultHandler.waitFor();
            Runtime.getRuntime().removeShutdownHook(t);
        } catch (InterruptedException e) {
            System.err.println("[!] Execution interrupted: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[!] Execution failed: " + e.getMessage());
        }

        if (resultHandler.getExitValue() != exitValue) {
            System.err.println("[!] Exitcode: " + resultHandler.getExitValue());
            return null;
        }

        return outStream;
    }
}
