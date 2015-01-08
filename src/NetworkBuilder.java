import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Vladimir Ulyantsev
 * Date: 30.04.13
 * Time: 17:09
 */
public class NetworkBuilder {
    public static PhylogeneticNetwork gvNetwork(Map<String, Integer> m, boolean[] solution, List<PhylogeneticTree> trees, int k) {
        List< List<Integer> > graph = new ArrayList<>();
        for(int i = 0; i < trees.get(0).size() + 2 * k - 2; ++i) {
            graph.add(i, new ArrayList<Integer>());
        }

        for (String s : m.keySet()) {
            if (solution[m.get(s) - 1]) {
                String[] splitted = s.split("_");
                if (Integer.parseInt(splitted[1]) == trees.get(0).size() + k - 1 ||
                        Integer.parseInt(splitted[2]) == trees.get(0).getTaxaSize() - 1)
                    continue;
                if (splitted[0].equals("left") || splitted[0].equals("right") || splitted[0].equals("ch")) {
                    int src = Integer.parseInt(splitted[1]);
                    int dst = Integer.parseInt(splitted[2]);
                    if(src > trees.get(0).size()) {
                        src -= 2;
                    } else if(src >= trees.get(0).getTaxaSize()) {
                        src -= 1;
                    }
                    if(dst > trees.get(0).size()) {
                        dst -= 2;
                    } else if(dst >= trees.get(0).getTaxaSize()) {
                        dst -= 1;
                    }
                    graph.get(src).add(dst);
                }
            }
        }

        List <String> labels = new ArrayList<>();
        for(int i = 0; i < trees.get(0).getTaxaSize() - 1; ++i) {
            labels.add(trees.get(0).getLabel(i));
        }

        return new PhylogeneticNetwork(graph, labels, k);
    }
}
