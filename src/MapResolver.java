import beepp.expression.IntegerConstant;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Moklev Vyacheslav
 */
public class MapResolver {
    private static final Pattern BOOL_PATTERN =
            Pattern.compile("^\\(([a-zA-Z_0-9]+),bool,(-?[0-9]+)\\)\\.$");
    private static final String LIST_REGEX = "((-?[0-9]+,)*-?[0-9]+)?";
    private static final Pattern INT_PATTERN =
            Pattern.compile("^\\(([a-zA-Z_0-9]+),int,order\\(min\\((-?[0-9]+)\\),\\[(" + LIST_REGEX + ")]\\)\\).$");
    private static final Pattern END_PATTERN =
            Pattern.compile("^end_bee_map.$");

    public static Map<String, Object> resolve(File file, boolean[] solution) throws FileNotFoundException {
        Map<String, Object> map = new TreeMap<>();
        List<String> lines = new BufferedReader(new FileReader(file))
                .lines()
                .collect(Collectors.toList());
        for (String line : lines) {
            line = line.trim();
            Matcher boolMatcher = BOOL_PATTERN.matcher(line);
            Matcher intMatcher = INT_PATTERN.matcher(line);
            Matcher endMatcher = END_PATTERN.matcher(line);
            if (boolMatcher.find()) {
                String name = boolMatcher.group(1);
                String var = boolMatcher.group(2);
                if (var.startsWith("-")) {
                    Object prev = map.put(name, !solution[Integer.parseInt(var.substring(1)) - 1]);
                    if (prev != null) {
                        System.err.println("Redefining \"" + name + "\": was " + prev + ", now is" + map.get(name));
                    }
                } else {
                    Object prev = map.put(name, solution[Integer.parseInt(var) - 1]);
                    if (prev != null) {
                        System.err.println("Redefining \"" + name + "\": was " + prev + ", now is" + map.get(name));
                    }
                }
            } else if (intMatcher.find()) {
                String name = intMatcher.group(1);
                int min = Integer.parseInt(intMatcher.group(2));
                String[] orderList = intMatcher.group(3).trim().split(",");
                if (orderList[0].isEmpty()) {
                    orderList = new String[0];
                }
                List<Boolean> order = Arrays.stream(orderList).map(s -> {
                    if (s.startsWith("-")) {
                        return !solution[Integer.parseInt(s.substring(1)) - 1];
                    } else {
                        return solution[Integer.parseInt(s) - 1];
                    }
                }).collect(Collectors.toList());
                boolean lastValue = true;
                for (boolean value : order) {
                    if (!lastValue && value) {
                        System.err.println("Invalid order array: " + order);
                    }
                    lastValue = value;
                }
                int value = min + order.lastIndexOf(true) + 1;
                Object prev = map.put(name, value);
                if (prev != null) {
                    System.err.println("Redefining \"" + name + "\": was " + prev + ", now is" + map.get(name));
                }
            } else if (endMatcher.find()) {
                break;
            } else {
                System.err.println("Not parsed: \"" + line + "\"");
            }
        }
        return map;
    }
}
