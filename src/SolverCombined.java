import org.apache.commons.exec.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SolverCombined extends Solver {
    private static final Pattern SOLUTION_PATTERN =
            Pattern.compile("^(p_\\d+(?:_\\d+)?)\\s*=\\s*(\\d+|true|false)$");

    private final String beeFileName;
    private final int numberOfSolutions;

    SolverCombined(String beeFileName, int numberOfSolutions) {
        this.beeFileName = beeFileName;
        this.numberOfSolutions = numberOfSolutions;
    }

    private static List<Map<String, Object>> parseSolutions(OutputStream outputStream) {
        List<Map<String, Object>> solutions = new ArrayList<>();
        Map<String, Object> solution = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new StringReader(outputStream.toString()))) {
            for (String line : (Iterable<String>) br.lines()::iterator) {
                if (line.equals("=====UNSATISFIABLE====="))
                    return null;

                if (line.equals("----------")) {
                    solutions.add(solution);
                    solution = new HashMap<>();
                    continue;
                }

                if (line.equals("=========="))
                    return solutions;

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

        return solutions;  // For real, this line is never reached.
    }

    @Override
    List<Map<String, Object>> solve() {
        System.out.println("[.] Using built-it solver");

        List<Map<String, Object>> solutions = solveWithBumbleBEE();
        return solutions;
    }

    private List<Map<String, Object>> solveWithBumbleBEE() {
        OutputStream outputStream = runBumbleBEE();
        if (outputStream == null)
            return null;

        List<Map<String, Object>> solutions = parseSolutions(outputStream);
        return solutions;
    }

    private OutputStream runBumbleBEE() {
        CommandLine command = new CommandLine("BumbleBEE")
                .addArgument(beeFileName);
        return runSolver(command);
    }
}
