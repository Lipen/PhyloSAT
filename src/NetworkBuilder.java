import java.util.List;
import java.util.Map;

/**
 * Vladimir Ulyantsev
 * Date: 30.04.13
 * Time: 17:09
 */
public class NetworkBuilder {
    public static String gvNetwork(Map<String, Integer> m, boolean[] solution, List<PhylogeneticTree> trees, int k) {
        String ans = "digraph G {\n";
//        ans += "  node [shape = point]\n";
        ans += "  {rank = same;";
        for (int i = 0; i < trees.get(0).getTaxaSize() - 1; i++) {
            ans += " " + i;
        }
        ans += "}\n";
        ans += "  node [shape = box];\n ";
        for (int i = trees.get(0).size() + k; i < trees.get(0).size() + 2 * k; i++) {
            ans += " " + i;
        }
        ans += ";\n";
        ans += "  node [shape = ellipse];\n";

        for (String s : m.keySet()) {
            int var = m.get(s);

            if (solution[var - 1]) {
                String[] splitted = s.split("_");
                if (Integer.parseInt(splitted[1]) == trees.get(0).size() + k - 1 ||
                        Integer.parseInt(splitted[2]) == trees.get(0).getTaxaSize() - 1)
                    continue;
                if (splitted[0].equals("left") || splitted[0].equals("right") || splitted[0].equals("ch")) {
                    int src = Integer.parseInt(splitted[1]);
                    int dst = Integer.parseInt(splitted[2]);
                    ans += String.format("  %d -> %d;\n", src, dst);
                }
            }
        }

        ans += "}\n";
        return ans;
    }
}
