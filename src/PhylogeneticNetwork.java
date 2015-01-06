import java.util.*;


/**
 * Mikhail Melnik
 * Date: 08.01.15
 */

public class PhylogeneticNetwork {
    private class PhylogeneticNode {
        List<Integer> children;

        String label;

        Set <String> getTaxaSet() {
            return new HashSet<>(Arrays.asList(label.split("+")));
        }
    }

    private List <PhylogeneticNode> nodes = new ArrayList<>();
    private int root;

    public PhylogeneticNetwork(List< List<Integer> > graph, List<String> labels, int k) {
        nodes = new ArrayList<>(graph.size());
        for(int i = 0; i < graph.size(); ++i) {
            nodes.get(i).children = graph.get(i);
        }
        for(int i = 0; i < labels.size(); ++i) {
            nodes.get(i).label = labels.get(i);
        }
        root = graph.size() - k - 1;
    }

    public Set <String> getTaxaSet() {
        Set <String> ans = new HashSet<>();
        for(PhylogeneticNode node : nodes) {
            ans.addAll(Arrays.asList(node.label.split("+")));
        }
        return ans;
    }

    public boolean substituteSubtask(PhylogeneticNetwork other) {
        Set<String> otherTaxaSet = other.getTaxaSet();
        for(PhylogeneticNode node : nodes) {
            if(node.getTaxaSet().equals(otherTaxaSet)) {
                for(PhylogeneticNode otherNode : other.nodes) {
                    for(int i = 0; i < otherNode.children.size(); ++i) {
                        otherNode.children.set(i, otherNode.children.get(i) + nodes.size());
                    }
                }
                node.children = other.nodes.get(root).children;
                node.label = "";
                other.nodes.remove(other.root);
                nodes.addAll(other.nodes);
                other.nodes = null;
                other.root = -1;
                return true;
            }
        }
        return false;
    }
}
