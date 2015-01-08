import java.util.*;


/**
 * Mikhail Melnik
 * Date: 08.01.15
 */

public class PhylogeneticNetwork {
    private class PhylogeneticNode {
        List<Integer> children;

        String label;

        PhylogeneticNode(List<Integer> children) {
            this.children = children;
        }

        Set <String> getTaxaSet() {
            if(label != null) {
                return new HashSet<>(Arrays.asList(label.split("\\+")));
            } else {
                return new HashSet<>();
            }
        }
    }

    private List <PhylogeneticNode> nodes = new ArrayList<>();
    private int root;
    private int k;

    public PhylogeneticNetwork(List< List<Integer> > graph, List<String> labels, int k) {
        this.nodes = new ArrayList<>();
        for (List<Integer> children : graph) {
            this.nodes.add(new PhylogeneticNode(children));
        }
        for(int i = 0; i < labels.size(); ++i) {
            this.nodes.get(i).label = labels.get(i);
        }
        this.root = graph.size() - k - 1;
        this.k = k;
    }

    public int getK() {
        return k;
    }

    public Set <String> getTaxaSet() {
        Set <String> ans = new HashSet<>();
        for(PhylogeneticNode node : nodes) {
            if(node.label != null) {
                ans.addAll(Arrays.asList(node.label.split("\\+")));
            }
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
                node.label = null;
                other.nodes.remove(other.root);
                nodes.addAll(other.nodes);
                other.nodes = null;
                other.root = -1;
                return true;
            }
        }
        return false;
    }

    public String toGVString() {
        String ans = "digraph G {\n";
        ans += "  node [shape = ellipse]\n";
        ans += "  {rank = same;";
        int taxaSize = (nodes.size() - 2 * k) / 2 + 1;
        for (int i = 0; i < taxaSize; i++) {
            ans += " " + nodes.get(i).label;
        }
        ans += "}\n";
        ans += "  node [shape = box];\n ";
        for (int i = nodes.size() - k; i < nodes.size(); i++) {
            ans += " " + i;
        }
        ans += ";\n";
        ans += "  node [shape = ellipse];\n";
        for (int i = 0; i < nodes.size(); i++) {
            String src = (nodes.get(i).label == null) ? Integer.toString(i) : nodes.get(i).label;
            for (int j = 0; j < nodes.get(i).children.size(); ++j) {
                int child = nodes.get(i).children.get(j);
                String dst = (nodes.get(child).label == null) ? Integer.toString(child) : nodes.get(child).label;
                ans += "  " + src + " -> " + dst + ";\n";
            }
        }

        ans += "}\n";
        return ans;
    }
}
