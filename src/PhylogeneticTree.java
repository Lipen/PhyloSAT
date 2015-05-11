import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

import java.util.*;

/**
 * Vladimir Ulyantsev
 * Date: 09.05.13
 * Time: 9:00
 */
public class PhylogeneticTree {
    private class PhylogeneticNode {
        int parent;

        List<Integer> children;

        String label;

        PhylogeneticNode(PhylogeneticNode other) {
            this.parent = other.parent;
            this.label = other.label;
            this.children = new ArrayList<>(other.children);
        }

        PhylogeneticNode(int parent, List<Integer> children, String label) {
            this.parent = parent;
            this.children = children;
            this.label = label;
        }
    }

    private List<PhylogeneticNode> nodes;

    private boolean fictitiousRoot;

    private PhylogeneticTree() {
        this.nodes = new ArrayList<>(); 
        fictitiousRoot = false;
    }

    public boolean hasFictitiousRoot() { return fictitiousRoot; }

    public PhylogeneticTree(PhylogeneticTree other) {
        this();
        for (PhylogeneticNode node : other.nodes) {
            this.nodes.add(new PhylogeneticNode(node));
        }
        fictitiousRoot = other.fictitiousRoot;
    }

    public PhylogeneticTree(SimpleRootedTree tree) {
        this();
        int treeSize = tree.getNodes().size();

        Map<Node, Integer> m = new HashMap<>();

        List<Taxon> taxa = new ArrayList<>(tree.getTaxa());
        Collections.sort(taxa, new Comparator<Taxon>() {
            @Override
            public int compare(Taxon o1, Taxon o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (Taxon taxon : taxa) {
            PhylogeneticNode newNode = new PhylogeneticNode(-1, new ArrayList<Integer>(), taxon.getName());
            m.put(tree.getNode(taxon), nodes.size());
            nodes.add(newNode);
        }

        while (nodes.size() < treeSize) {
            for (Node node : tree.getNodes()) {
                if (m.containsKey(node)) {
                    continue;
                }
                PhylogeneticNode newNode = new PhylogeneticNode(-1, new ArrayList<Integer>(), null);

                boolean canAddToNodes = true;
                for (Node child : tree.getChildren(node)) {
                    if (!m.containsKey(child)) {
                        canAddToNodes = false;
                        break;
                    }
                    newNode.children.add(m.get(child));
                }

                if (canAddToNodes) {
                    m.put(node, nodes.size());
                    nodes.add(newNode);
                }
            }
        }

        for (Node node : tree.getNodes()) {
            PhylogeneticNode phNode = nodes.get(m.get(node));
            if (!tree.isRoot(node)) {
                phNode.parent = m.get(tree.getParent(node));
            }
        }
    }

    public void addFictitiousRoot() {
        if(fictitiousRoot) {
            throw new RuntimeException("It is bad idea to add the second fictitious root!");
        }

        fictitiousRoot = true;

        int taxaSize = getTaxaSize();
        for(PhylogeneticNode node : nodes) {
            if(node.parent == -1) {
                node.parent = nodes.size() + 1;
            } else {
                node.parent++;
            }

            for(int i = 0; i < node.children.size(); ++i) {
                int child = node.children.get(i);
                if(child >= taxaSize) {
                    node.children.set(i, child + 1);
                }
            }
        }
        nodes.add(taxaSize, new PhylogeneticNode(nodes.size() + 1, new ArrayList<Integer>(), ""));
        PhylogeneticNode newRoot = new PhylogeneticNode(-1, new ArrayList<Integer>(), null);
        newRoot.children.add(taxaSize);
        newRoot.children.add(nodes.size() - 1);
        nodes.add(newRoot);
    }

    public void removeFictitiousRoot() {
        if(!fictitiousRoot) {
            throw new RuntimeException("Tree does not have any fictitious root!");
        }

        fictitiousRoot = false;

        int fictitiousTaxaNum = getTaxaSize() - 1;
        nodes.remove(fictitiousTaxaNum);
        nodes.remove(nodes.size() - 1);

        for(PhylogeneticNode node : nodes) {
            if(node.parent == nodes.size() + 1) {
                node.parent = -1;
            } else {
                node.parent--;
            }

            for(int i = 0; i < node.children.size(); ++i) {
                int child = node.children.get(i);
                if(child >= fictitiousTaxaNum) {
                    node.children.set(i, child - 1);
                }
            }
        }
    }

    public PhylogeneticTree buildSubtree(int nodeNum) {
        PhylogeneticTree ans = new PhylogeneticTree();
        Map<Integer, Integer> oldToNew = new HashMap<>();

        for (int oldNodeNum : this.getSubtreeNodes(nodeNum)) {
            PhylogeneticNode oldNode = this.nodes.get(oldNodeNum);
            PhylogeneticNode newNode = new PhylogeneticNode(-1, new ArrayList<Integer>(), oldNode.label);
            int newNodeNumber = oldToNew.size();
            oldToNew.put(oldNodeNum, newNodeNumber);

            for (int oldChildNum : this.getChildren(oldNodeNum)) {
                int childNum = oldToNew.get(oldChildNum);
                newNode.children.add(childNum);
                ans.nodes.get(childNum).parent = newNodeNumber;
            }

            ans.nodes.add(newNode);
        }
        return ans;
    }

    public PhylogeneticTree compressedTree(int nodeNum, String label) {
        if (this.getParent(nodeNum) == -1) {
            throw new RuntimeException("It is bad idea to compress tree on root");
        }
        List<Integer> subtree = this.getSubtreeNodes(nodeNum);
        int minLeafNumber = subtree.get(0);

        PhylogeneticTree ans = new PhylogeneticTree();
        Map<Integer, Integer> oldToNew = new HashMap<>();

        for (int oldNodeNum = 0; oldNodeNum < this.size(); oldNodeNum++) {
            if (oldNodeNum == minLeafNumber) {
                PhylogeneticNode newNode = new PhylogeneticNode(-1, new ArrayList<Integer>(), label);
                ans.nodes.add(newNode);
                continue;
            }
            if (!subtree.contains(oldNodeNum)) {
                PhylogeneticNode oldNode = this.nodes.get(oldNodeNum);
                PhylogeneticNode newNode = new PhylogeneticNode(-1, new ArrayList<Integer>(), oldNode.label);
                int newNodeNumber = ans.nodes.size();
                oldToNew.put(oldNodeNum, newNodeNumber);

                for (int oldChildNum : this.getChildren(oldNodeNum)) {
                    int childNum = (oldChildNum == nodeNum) ? minLeafNumber : oldToNew.get(oldChildNum);
                    newNode.children.add(childNum);
                    ans.nodes.get(childNum).parent = newNodeNumber;
                }

                ans.nodes.add(newNode);
            }
        }

        return ans;
    }

    public int size() {
        return this.nodes.size();
    }

    public int getParent(int nodeNum) {
        return this.nodes.get(nodeNum).parent;
    }

    public List<Integer> getChildren(int nodeNum) {
        return this.nodes.get(nodeNum).children;
    }

    public int getDepth(int nodeNum) {
        int ans = -1;
        while (nodeNum != -1) {
            nodeNum = this.getParent(nodeNum);
            ans++;
        }
        return ans;
    }

    public List<Integer> getSubtreeNodes(int nodeNum) {
        List<Integer> ans = new ArrayList<>();
        ans.add(nodeNum);
        for (int childNum : this.getChildren(nodeNum)) {
            ans.addAll(this.getSubtreeNodes(childNum));
        }
        Collections.sort(ans);
        return ans;
    }

    public int getSubtreeSize(int nodeNum) {
        return this.getSubtreeNodes(nodeNum).size();
    }

    public String getLabel(int nodeNum) {
        if (nodeNum < 0 || this.getTaxaSize() <= nodeNum) {
            throw new RuntimeException("Taxon index out of bounds: " + nodeNum);
        }
        return this.nodes.get(nodeNum).label;
    }

    public List<Integer> getTaxa(int nodeNum) {
        List<Integer> ans = new ArrayList<>();
        for (int node : this.getSubtreeNodes(nodeNum)) {
            if (isLeaf(node)) {
                ans.add(node);
            }
        }
        return ans;
    }

    public int getTaxaSize() {
        return this.size() / 2 + 1;
    }

    public boolean isLeaf(int nodeNum) {
        return this.nodes.get(nodeNum).label != null;
    }

    public static boolean isTaxaEquals(PhylogeneticTree t1, int n1, PhylogeneticTree t2, int n2) {
        List<Integer> taxa1 = t1.getTaxa(n1), taxa2 = t2.getTaxa(n2);
        if (taxa1.size() != taxa2.size()) {
            return false;
        }
        for (int taxon1 : taxa1) {
            if (!taxa2.contains(taxon1)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSubtreesEquals(PhylogeneticTree t1, int n1, PhylogeneticTree t2, int n2) {
        if (t1.isLeaf(n1) && t2.isLeaf(n2) && t1.getLabel(n1).equals(t2.getLabel(n2))) {
            return true;
        }
        if (!isTaxaEquals(t1, n1, t2, n2)) {
            return false;
        }
        for (int ch1 : t1.getChildren(n1)) {
            boolean hasEquals = false;
            for (int ch2 : t2.getChildren(n2)) {
                if (isSubtreesEquals(t1, ch1, t2, ch2)) {
                    hasEquals = true;
                    break;
                }
            }
            if (!hasEquals) {
                return false;
            }
        }
        return true;
    }

    private String repr(int nodeNum) {
        if (isLeaf(nodeNum)) {
            return getLabel(nodeNum);
        }
        String ans = "(";
        for (int childNum = 0; childNum < getChildren(nodeNum).size(); childNum++) {
            if (childNum > 0) {
                ans += ",";
            }
            ans += repr(getChildren(nodeNum).get(childNum));
        }
        return ans + ")";
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

    public String toString() {
        return repr(size() - 1);
    }
}
