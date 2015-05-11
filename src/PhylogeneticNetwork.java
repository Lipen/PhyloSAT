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
                        int child = otherNode.children.get(i);
                        if (child > other.root) {
                            child -= 1;
                        }
                        otherNode.children.set(i, child + nodes.size());
                    }
                }
                node.children = other.nodes.get(other.root).children;
                node.label = null;
                other.nodes.remove(other.root);
                this.nodes.addAll(other.nodes);
                this.k += other.k;
                other.nodes = null;
                other.root = -1;
                return true;
            }
        }
        return false;
    }

    public String toGVString() {
        String ans = "graph G {\n";
        ans += "  node [shape=circle width=0.3 fixedsize=true height=0.3];\n";
        ans += "  {rank = same ranksep=0.75 nodesep=0.75;";
        for (PhylogeneticNode node : nodes) {
            if (node.label != null) {
                ans += " " + node.label;
            }
        }
        ans += "}\n";
        ans += "  node [shape = square label = \"\" width=0.15 fixedsize=true height=0.15];\n ";
        boolean hasReticulationNodes = false;
        for (int i = 0; i < nodes.size(); i++) {
            if(nodes.get(i).children.size() == 1) {
                ans += " _" + i;
                hasReticulationNodes = true;
            }
        }
        if (hasReticulationNodes) {
            ans += ";\n";
        }
        ans += "  node [shape = point width=default height=default];\n";
        for (int i = 0; i < nodes.size(); i++) {
            String src = (nodes.get(i).label == null) ? '_' + Integer.toString(i) : nodes.get(i).label;
            for (int j = 0; j < nodes.get(i).children.size(); ++j) {
                int child = nodes.get(i).children.get(j);
                String dst = (nodes.get(child).label == null) ? '_' + Integer.toString(child) : nodes.get(child).label;
                ans += "  " + src + " -- " + dst + ";\n";
            }
        }

        ans += "}\n";
        return ans;
    }
}
