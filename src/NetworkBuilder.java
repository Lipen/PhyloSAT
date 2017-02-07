import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Vladimir Ulyantsev
 * Date: 30.04.13
 * Time: 17:09
 */
public class NetworkBuilder {
    private static String replicate(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private static int[] getParams(String s, int numParams) {
        Pattern pattern = Pattern.compile("[a-z]+" + replicate("_([0-9]+)", numParams));
        Matcher matcher = pattern.matcher(s);
        if (!matcher.find()) {
            throw new RuntimeException("No match");
        }
        int[] result = new int[numParams];
        for (int i = 0; i < numParams; i++) {
            result[i] = Integer.parseInt(matcher.group(i + 1));
        }
        return result;
    }
    
    private static void reverseArray(int[] array, int n) {
        int temp;
        for (int i = 0; i < n / 2; i++) {
            temp = array[i];
            array[i] = array[n - 1 - i];
            array[n - 1 - i] = temp;
        }
    }
    
    private static String makeIntValue(String name, String oldName, int numParams) {
        int[] params = getParams(oldName, numParams + 1);
        reverseArray(params, numParams);
        return name + Arrays.stream(params)
                .limit(numParams)
                .mapToObj(x -> "_" + x)
                .collect(Collectors.joining()) + " = " + params[numParams];
    }

    private static String makeBoolValue(String name, String oldName, int numParams, boolean value) {
        int[] params = getParams(oldName, numParams);
        reverseArray(params, numParams);
        return name + Arrays.stream(params)
                .limit(numParams)
                .mapToObj(x -> "_" + x)
                .collect(Collectors.joining()) + " = " + value;
    }

    public static PhylogeneticNetwork gvNetwork(Map<String, Integer> m, boolean[] solution, List<PhylogeneticTree> trees, int k) {
        try {
            PrintWriter pw = new PrintWriter("sat.solution");
            m.forEach((s, i) -> {
                if (s.startsWith("left_")) {
                    boolean value = solution[i - 1];
                    if (value) {
                        pw.println(makeIntValue("l", s, 1));
                    }
                } else if (s.startsWith("right_")) {
                    boolean value = solution[i - 1];
                    if (value) {
                        pw.println(makeIntValue("r", s, 1));
                    }
                } else if (s.startsWith("parent_")) {
                    boolean value = solution[i - 1];
                    if (value) {
                        pw.println(makeIntValue("p", s, 1));
                    }
                } else if (s.startsWith("lp_")) {
                    boolean value = solution[i - 1];
                    if (value) {
                        pw.println(makeIntValue("pl", s, 1));
                    }
                } else if (s.startsWith("rp_")) {
                    boolean value = solution[i - 1];
                    if (value) {
                        pw.println(makeIntValue("pr", s, 1));
                    }
                } else if (s.startsWith("ch_")) {
                    boolean value = solution[i - 1];
                    if (value) {
                        pw.println(makeIntValue("c", s, 1));
                    }
                } else if (s.startsWith("x_")) {
                    boolean value = solution[i - 1];
                    if (value) {
                        pw.println(makeIntValue("x", s, 2));
                    }
                } else if (s.startsWith("up_")) {
                    boolean value = solution[i - 1];
                    if (value) {
                        pw.println(makeIntValue("a", s, 2));
                    }
                } else if (s.startsWith("dir_")) {
                    boolean value = solution[i - 1];
                    pw.println(makeBoolValue("d", s, 2, value));
                } else if (s.startsWith("rused_")) {
                    boolean value = solution[i - 1];
                    pw.println(makeBoolValue("ur", s, 2, value));
                } else if (s.startsWith("used_")) {
                    boolean value = solution[i - 1];
                    pw.println(makeBoolValue("u", s, 2, value));
                }
            });
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        List<List<Integer>> graph = new ArrayList<>();
        boolean hasFictitiousRoot = trees.get(0).hasFictitiousRoot();
        int networkSize = trees.get(0).size() + 2 * k;
        int taxaSize = trees.get(0).getTaxaSize();
        if (hasFictitiousRoot) {
            networkSize -= 2;
            taxaSize -= 1;
        }
        for (int i = 0; i < networkSize; ++i) {
            graph.add(i, new ArrayList<>());
        }

        for (String s : m.keySet()) {
            if (solution[m.get(s) - 1]) {
                String[] splitted = s.split("_");
                if (hasFictitiousRoot && (Integer.parseInt(splitted[1]) == trees.get(0).size() + k - 1 ||
                        Integer.parseInt(splitted[2]) == trees.get(0).getTaxaSize() - 1))
                    continue;
                if (splitted[0].equals("left") || splitted[0].equals("right") || splitted[0].equals("ch")) {
                    int src = Integer.parseInt(splitted[1]);
                    int dst = Integer.parseInt(splitted[2]);
                    if (hasFictitiousRoot) {
                        if (src > trees.get(0).size() + k - 1) {
                            src -= 2;
                        } else if (src >= trees.get(0).getTaxaSize()) {
                            src -= 1;
                        }
                        if (dst > trees.get(0).size() + k - 1) {
                            dst -= 2;
                        } else if (dst >= trees.get(0).getTaxaSize()) {
                            dst -= 1;
                        }
                    }
                    graph.get(src).add(dst);
                }
            }
        }

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < taxaSize; ++i) {
            labels.add(trees.get(0).getLabel(i));
        }

        return new PhylogeneticNetwork(graph, labels, k);
    }
}
