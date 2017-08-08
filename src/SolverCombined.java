import com.beust.jcommander.Parameter;
import jebl.evolution.align.Output;
import org.apache.commons.exec.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

final class SolverCombined extends Solver {
    private static final Pattern SOLUTION_PATTERN =
            Pattern.compile("^(p_\\d+(?:_\\d+)?)\\s*=\\s*(\\d+|true|false)$");

    private String beeFileName;

    SolverCombined(String beeFileName) {
        this.beeFileName = beeFileName;
    }

    @Override
    Map<String, Object> solve() {
        System.out.println("[.] Using built-it solver");

        Map<String, Object> solution = solveWithBumbleBEE();
        return solution;
    }

    private Map<String, Object> solveWithBumbleBEE() {
        OutputStream outputStream = runSolver();
        if (outputStream == null)
            return null;

        Map<String, Object> solution = parseSolution(outputStream);
        return solution;
    }

    private OutputStream runSolver() {
        CommandLine command = new CommandLine("BumbleBEE")
                .addArgument(beeFileName);
        return runSolver(command);
    }

    private static Map<String, Object> parseSolution(OutputStream outputStream) {
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
