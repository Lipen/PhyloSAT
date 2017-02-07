import sun.awt.image.IntegerComponentRaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Moklev Vyacheslav
 */
public class BEENetworkBuilder {
    public static PhylogeneticNetwork buildNetwork(Map<String, Object> map, List<PhylogeneticTree> trees, int k) {
        int n = trees.get(0).getTaxaSize() - 1;
        boolean hasFictitiousRoot = trees.get(0).hasFictitiousRoot(); 
        List<Integer> L = IntStream.rangeClosed(0, n).mapToObj(x -> x).collect(Collectors.toList());
        List<Integer> V = IntStream.rangeClosed(n + 1, 2 * n + k).mapToObj(x -> x).collect(Collectors.toList());
        List<Integer> R = IntStream.rangeClosed(2 * n + k + 1, 2 * (n + k)).mapToObj(x -> x).collect(Collectors.toList());
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i <= 2 * (n + k - (hasFictitiousRoot ? 1 : 0)); i++) {
            graph.add(new ArrayList<>());
        }
        List<String> labels = L.stream()
                .limit(hasFictitiousRoot ? n : n + 1)
                .map(i -> trees.get(0).getLabel(i))
                .collect(Collectors.toList());
        for (int v: V) {
            if (hasFictitiousRoot && v == 2 * n + k) 
                continue;
            int left = (int) map.get("l_" + v);
            int right = (int) map.get("r_" + v);
            graph.get(v - (hasFictitiousRoot ? 1 : 0)).add(transform(left, n, k, hasFictitiousRoot));
            graph.get(v - (hasFictitiousRoot ? 1 : 0)).add(transform(right, n, k, hasFictitiousRoot));
        }
        for (int v: R) {
            int child = (int) map.get("c_" + v);
            graph.get(v - (hasFictitiousRoot ? 2 : 0)).add(transform(child, n, k, hasFictitiousRoot));
        }
        return new PhylogeneticNetwork(graph, labels, k);
    }

    private static int transform(int v, int n, int k, boolean hasFictitiousRoot) {
        if (!hasFictitiousRoot) {
            return v;
        }
        if (v > 2 * n + k) {
            return v - 2;
        }
        if (v > n) {
            return v - 1;
        }
        return v;
    }
}
