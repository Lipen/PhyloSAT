import org.apache.commons.exec.*;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

final class SolverCryptominisat extends Solver {
    private static final Pattern BOOL_PATTERN = Pattern.compile("\\((\\w+),bool,(-?\\d+)\\)\\.");
    private static final Pattern INT_PATTERN = Pattern.compile("\\((\\w+),int,order\\(min\\((-?\\d+)\\),\\[((?:(?:-?\\d+),)*(?:-?\\d+)?)\\]\\)\\)\\.");
    private static final Pattern END_PATTERN = Pattern.compile("end_bee_map\\.");

    private final String beeFileName;
    private final String dimacsFileName;
    private final String mapFileName;
    private final int threads;


    SolverCryptominisat(String beeFileName, String dimacsFileName, String mapFileName) {
        this(beeFileName, dimacsFileName, mapFileName, 4);
    }

    SolverCryptominisat(String beeFileName, String dimacsFileName, String mapFileName, int threads) {
        this.beeFileName = beeFileName;
        this.dimacsFileName = dimacsFileName;
        this.mapFileName = mapFileName;
        this.threads = threads;
    }


    @Override
    Map<String, Object> solve() {
        System.out.println("[.] Using external solver");

        if (!convertBEEtoDIMACS())
            return null;

        Map<String, Object> solution = solveWithCryptominisat();
        return solution;
    }

    private boolean convertBEEtoDIMACS() {
        try {
            new PrintWriter(dimacsFileName).close();
            new PrintWriter(mapFileName).close();
        } catch (FileNotFoundException e) {
            System.err.println("[!] So sad: " + e.getMessage());
            return false;
        }

        if (!execCommand(String.format("BumbleBEE %s -dimacs %s %s", beeFileName, dimacsFileName, mapFileName)))
            return false;

        if (new File(dimacsFileName).length() == 0) {
            System.err.println("[!] Dimacs file is empty => maybe pre-considered UNSAT");
            return false;
        }

        return true;
    }

    private static boolean execCommand(String command) {
        try {
            Runtime.getRuntime().exec(command).waitFor();
        } catch (InterruptedException e) {
            System.err.println("[!] Execution of <" + command + "> interrupted");
            return false;
        } catch (IOException e) {
            System.err.println("[!] Execution of <" + command + "> failed");
            return false;
        }
        return true;
    }

    private Map<String, Object> solveWithCryptominisat() {
        OutputStream outputStream = runCryptominisat();
        if (outputStream == null)
            return null;

        Map<Integer, Boolean> variables = parseVariables(outputStream);
        if (variables == null)
            return null;

        Map<String, Object> solution = mapSolution(variables);
        return solution;
    }

    private OutputStream runCryptominisat() {
        CommandLine command = new CommandLine("cryptominisat")
                .addArgument("--threads=" + threads)
                .addArgument(dimacsFileName);
        return runSolver(command, 20);
    }

    private static Map<Integer, Boolean> parseVariables(OutputStream outputStream) {
        Map<Integer, Boolean> variables = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new StringReader(outputStream.toString()))) {
            for (String line : (Iterable<String>) br.lines()::iterator) {
                if (line.startsWith("v")) {
                    for (String token : line.substring(2).split(" ")) {
                        int x = Integer.parseInt(token);
                        variables.put(Math.abs(x), x > 0);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[!] So sad: " + e.getMessage());
            return null;
        }

        return variables;
    }

    private Map<String, Object> mapSolution(Map<Integer, Boolean> variables) {
        Map<String, Object> solution = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(mapFileName))) {
            for (String line : (Iterable<String>) br.lines()::iterator) {
                Matcher boolMatcher = BOOL_PATTERN.matcher(line);
                Matcher intMatcher = INT_PATTERN.matcher(line);
                Matcher endMatcher = END_PATTERN.matcher(line);

                if (boolMatcher.matches()) {
                    String name = boolMatcher.group(1);
                    int x = Integer.parseInt(boolMatcher.group(2));
                    Object prev;
                    if (x > 0)
                        prev = solution.put(name, variables.get(x));
                    else
                        prev = solution.put(name, !variables.get(-x));

                    if (prev != null)
                        System.err.println("[!] Redefining \"" + name + "\": was " + prev + ", now is" + solution.get(name));

                } else if (intMatcher.matches()) {
                    String name = intMatcher.group(1);
                    int min = Integer.parseInt(intMatcher.group(2));
                    String[] orderList = intMatcher.group(3).split(",");

                    if (orderList[0].isEmpty())
                        orderList = new String[0];

                    List<Boolean> order = Arrays.stream(orderList).map(s -> {
                        int x = Integer.parseInt(s);
                        if (x > 0)
                            return variables.get(x);
                        else
                            return !variables.get(-x);
                    }).collect(Collectors.toList());

                    boolean lastValue = true;
                    for (boolean value : order) {
                        if (!lastValue && value)
                            System.err.println("[!] Invalid order array: " + order);
                        lastValue = value;
                    }
                    int value = min + order.lastIndexOf(true) + 1;
                    Object prev = solution.put(name, value);

                    if (prev != null)
                        System.err.println("[!] Redefining \"" + name + "\": was " + prev + ", now is" + solution.get(name));

                } else if (endMatcher.matches()) {
                    break;
                } else {
                    System.err.println("[!] Not parsed: \"" + line + "\"");
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("[!] Couldn't open <" + mapFileName + ">: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("[!] So sad: " + e.getMessage());
            return null;
        }

        return solution;
    }
}
