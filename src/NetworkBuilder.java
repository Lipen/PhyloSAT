import java.util.Map;

/**
 * Vladimir Ulyantsev
 * Date: 30.04.13
 * Time: 17:09
 */
public class NetworkBuilder {
    public static String gvNetwork(Map<String, Integer> m, boolean[] solution) {
        String ans = "digraph G {\n";
        ans += "  node [shape = point]\n";

        for (String s : m.keySet()) {
            int var = m.get(s);

            if (solution[var - 1]) {
                String[] split = s.split("_");
                if (split[0].equals("left") || split[0].equals("right") || split[0].equals("ch")) {
                    int src = Integer.parseInt(split[1]);
                    int dst = Integer.parseInt(split[2]);
                    ans += String.format("  %d -> %d;\n", src, dst);
                }
            }
        }

        ans += "}\n";
        return ans;
    }
}
