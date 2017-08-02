import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class Tree extends Graph {
    private abstract class TreeNode extends Node {
        private TreeNode parent;
        private final List<TreeNode> children = new ArrayList<>();


        TreeNode(TreeNode parent, String label) {
            super(label);
            this.parent = parent;
        }


        TreeNode getParent() {
            return parent;
        }

        void setParent(TreeNode newParent) {
            parent = newParent;
        }

        List<TreeNode> getChildren() {
            return children;
        }

        void addChild(TreeNode newChild) {
            children.add(newChild);
        }

        List<TreeNode> getLeaves() {
            return getChildren().stream()
                    .flatMap(child -> child.getLeaves().stream())
                    .collect(Collectors.toList());
        }
    }

    private class LeafNode extends TreeNode {
        LeafNode(String label) {
            this(null, label);
        }

        LeafNode(TreeNode parent, String label) {
            super(parent, label);
        }

        @Override
        List<TreeNode> getLeaves() {
            return Stream.of(this).collect(Collectors.toList());
        }
    }

    private class InternalNode extends TreeNode {
        InternalNode() {
            this(null);
        }

        InternalNode(TreeNode parent) {
            super(parent, null);
        }
    }

    private class CollapsedNode extends LeafNode {
        final Tree subtree;

        CollapsedNode(Tree subtree, TreeNode parent, String label) {
            super(parent, label);
            this.subtree = subtree;
        }
    }

    private class ClusterNode extends LeafNode {
        final Subtask subtask;

        ClusterNode(Subtask subtask, TreeNode parent, String label) {
            super(parent, label);
            this.subtask = subtask;
        }
    }


    private TreeNode root;
    private int n;  // Number of taxa
    private boolean hasFictitiousRoot = false;


    private Tree(int n) {
        this.n = n;
    }

    private Tree(TreeNode root) {
        this(root.getLeaves().size());
        this.root = root;
    }

    Tree(SimpleRootedTree tree) {
        this(tree.getTaxa().size());

        Map<jebl.evolution.graphs.Node, TreeNode> m = new HashMap<>();

        List<Taxon> taxa = new ArrayList<>(tree.getTaxa());
        taxa.sort(Comparator.comparing(Taxon::getName));

        for (Taxon taxon : tree.getTaxa()) {
            TreeNode newNode = new LeafNode(taxon.getName());
            m.put(tree.getNode(taxon), newNode);
        }

        for (jebl.evolution.graphs.Node node : tree.getInternalNodes()) {
            TreeNode newNode = new InternalNode();
            m.put(node, newNode);
        }

        for (jebl.evolution.graphs.Node node : tree.getInternalNodes()) {
            for (jebl.evolution.graphs.Node child : tree.getChildren(node)) {
                m.get(node).addChild(m.get(child));
                m.get(child).setParent(m.get(node));
            }
        }

        this.root = m.get(tree.getRootNode());
    }


    int getTaxaSize() {
        return n;
    }

    boolean hasFictitiousRoot() {
        return hasFictitiousRoot;
    }

    int getParent(int nodeNum) {
        if (nodeNum == getRootNum())
            throw new IllegalArgumentException("Node " + nodeNum + " is a root and has no parent");

        List<TreeNode> dinosaur = traverseBottomUp(true, true);
        TreeNode node = dinosaur.get(nodeNum - (hasFictitiousRoot ? 0 : 1));
        return dinosaur.indexOf(node.getParent()) + (hasFictitiousRoot ? 0 : 1);
    }

    List<Integer> getChildren(int nodeNum) {
        if (0 <= nodeNum && nodeNum <= n)
            throw new IllegalArgumentException("Node " + nodeNum + " is a leaf and has no children");

        List<Integer> ans = new ArrayList<>();
        List<TreeNode> dinosaur = traverseBottomUp(true, true);

        for (TreeNode child : dinosaur.get(nodeNum - (hasFictitiousRoot ? 0 : 1)).getChildren()) {
            ans.add(dinosaur.indexOf(child) + (hasFictitiousRoot ? 0 : 1));
        }

        return ans;
    }

    Map<Integer, Integer> getParentMapping() {
        Map<Integer, Integer> m = new HashMap<>();

        LV_().forEach(v -> m.put(v, getParent(v)));

        return m;
    }

    String getLabel(int nodeNum) {
        return getLabels().get(nodeNum - (hasFictitiousRoot ? 0 : 1));
    }

    List<String> getLabels() {
        // Maybe with, maybe without fictitious leaf label
        return getTaxa().stream()
                .map(Node::getLabel)
                .collect(Collectors.toList());
    }

    private List<TreeNode> getTaxa() {
        List<TreeNode> taxa = root.getLeaves();
        // TODO: remove assert
        if (taxa.size() != (n + (hasFictitiousRoot ? 1 : 0)))
            throw new RuntimeException("Maybe getTaxa is broken.");
        taxa.sort(Comparator.comparing(Node::getLabel));
        return taxa;
    }

    // static boolean collapse(List<Tree> trees, List<Tree> subtrees_external) {
    static boolean collapse(List<Tree> trees, List<Subtask> subtasks_external) {
        /* Replaces equal subtrees (equal in all trees) with Collapsed Nodes */
        Tree firstTree = trees.get(0);

        for (TreeNode first : firstTree.traverseTopDown()) {
            List<TreeNode> anchors = new ArrayList<>();
            anchors.add(first);

            for (int t = 1; t < trees.size(); t++) {
                Tree secondTree = trees.get(t);
                boolean hasEqualSubtrees = false;

                for (TreeNode second : secondTree.traverseTopDown()) {
                    if (Tree.isSubtreesEquals(first, second)) {
                        anchors.add(second);
                        hasEqualSubtrees = true;
                        break;
                    }
                }

                if (!hasEqualSubtrees)
                    break;
            }

            if (anchors.size() == trees.size()) {
                List<TreeNode> leaves = first.getLeaves();
                leaves.sort(Comparator.comparing(Node::getLabel));
                String label = leaves.stream()
                        .map(Node::getLabel)
                        .collect(Collectors.joining("+"));
                System.out.println("[.] Collapsed " + leaves.size() + " leaves into " + label);
                Tree subtree = firstTree.buildSubtree(first);

                // subtrees_external.add(subtree);
                subtasks_external.add(new CollapsedSubtask(subtree));

                for (int t = 0; t < trees.size(); t++) {
                    TreeNode anchor = anchors.get(t);
                    Tree tree = trees.get(t);
                    tree.collapseNode(anchor, subtree, label);
                }

                return true;
            }
        }

        return false;
    }

    static boolean clusterize(List<Tree> trees, List<Subtask> subtasks_external) {
        /* Replaces clusters (subtrees with equal taxa) with Cluster Nodes */
        Tree firstTree = trees.get(0);

        for (TreeNode first : firstTree.traverseBottomUp()) {
            List<TreeNode> anchors = new ArrayList<>();
            anchors.add(first);

            for (int t = 1; t < trees.size(); t++) {
                Tree secondTree = trees.get(t);
                boolean hasEqualTaxa = false;

                for (TreeNode second : secondTree.traverseBottomUp()) {
                    if (Tree.isTaxaEquals(first, second)) {
                        anchors.add(second);
                        hasEqualTaxa = true;
                        break;
                    }
                }

                if (!hasEqualTaxa)
                    break;
            }

            if (anchors.size() == trees.size()) {
                List<TreeNode> leaves = first.getLeaves();
                leaves.sort(Comparator.comparing(Node::getLabel));
                String label = leaves.stream()
                        .map(Node::getLabel)
                        .collect(Collectors.joining("+"));
                System.out.println("[.] Clusterized " + leaves.size() + " leaves into " + label);
                List<Tree> clusters = new ArrayList<>();

                for (int t = 0; t < trees.size(); t++) {
                    TreeNode anchor = anchors.get(t);
                    Tree tree = trees.get(t);
                    clusters.add(tree.buildSubtree(anchor));
                }

                Subtask subtask = new ClusterSubtask(clusters);
                subtasks_external.add(subtask);

                for (int t = 0; t < trees.size(); t++) {
                    TreeNode anchor = anchors.get(t);
                    Tree tree = trees.get(t);
                    tree.clusterizeNode(anchor, subtask, label);
                }

                return true;
            }
        }

        return false;
    }

    private List<TreeNode> traverseTopDown() {
        return traverseTopDown(false, false);
    }

    private List<TreeNode> traverseTopDown(boolean addRoot, boolean addLeaves) {
        List<TreeNode> traversedNodes = new ArrayList<>();
        Set<TreeNode> visited = new HashSet<>(root.getLeaves());
        Queue<TreeNode> queue_BFS = new LinkedList<>();
        if (addRoot)
            queue_BFS.add(root);
        else
            queue_BFS.addAll(root.getChildren());

        while (!queue_BFS.isEmpty()) {
            TreeNode current = queue_BFS.remove();
            if (visited.add(current)) {
                traversedNodes.add(current);
                queue_BFS.addAll(current.getChildren());
            }
        }

        if (addLeaves)
            traversedNodes.addAll(getTaxa());

        return traversedNodes;
    }

    private List<TreeNode> traverseBottomUp() {
        return traverseBottomUp(false, false);
    }

    private List<TreeNode> traverseBottomUp(boolean addRoot, boolean addLeaves) {
        Stack<TreeNode> traversedNodes = new Stack<>();
        Set<TreeNode> visited = new HashSet<>(root.getLeaves());
        Queue<TreeNode> queue_BFS = new LinkedList<>();
        if (addRoot)
            queue_BFS.add(root);
        else
            queue_BFS.addAll(root.getChildren());

        while (!queue_BFS.isEmpty()) {
            TreeNode current = queue_BFS.remove();
            if (visited.add(current)) {
                traversedNodes.add(current);
                queue_BFS.addAll(current.getChildren());
            }
        }

        List<TreeNode> ans = new ArrayList<>();
        if (addLeaves)
            ans.addAll(getTaxa());
        while (!traversedNodes.isEmpty())
            ans.add(traversedNodes.pop());

        return ans;
    }

    private static boolean isSubtreesEquals(TreeNode first, TreeNode second) {
        if (first instanceof LeafNode && second instanceof LeafNode && first.getLabel().equals(second.getLabel()))
            return true;

        if (!isTaxaEquals(first, second))
            return false;

        for (TreeNode firstChild : first.getChildren()) {
            boolean hasEquals = false;
            for (TreeNode secondChild : second.getChildren()) {
                if (isSubtreesEquals(firstChild, secondChild)) {
                    hasEquals = true;
                    break;
                }
            }
            if (!hasEquals)
                return false;
        }
        return true;
    }

    private static boolean isTaxaEquals(TreeNode first, TreeNode second) {
        Set<String> firstTaxa = first.getLeaves().stream()
                .map(Node::getLabel)
                .collect(Collectors.toSet());
        Set<String> secondTaxa = second.getLeaves().stream()
                .map(Node::getLabel)
                .collect(Collectors.toSet());
        return firstTaxa.equals(secondTaxa);
    }

    private Tree buildSubtree(TreeNode anchor) {
        return new Tree(anchor);
    }

    private void collapseNode(TreeNode anchor, Tree subtree, String label) {
        TreeNode newNode = new CollapsedNode(subtree,
                anchor.getParent(),
                label);
        replaceNode(anchor, newNode);
    }

    private void clusterizeNode(TreeNode anchor, Subtask subtask, String label) {
        TreeNode newNode = new ClusterNode(subtask,
                anchor.getParent(),
                label);
        replaceNode(anchor, newNode);
    }

    private void replaceNode(TreeNode anchor, TreeNode newNode) {
        if (anchor.getParent() == null)
            throw new IllegalArgumentException("Please do not replace root!");

        Collections.replaceAll(anchor.getParent().getChildren(), anchor, newNode);
        this.n = root.getLeaves().size();
    }

    String toGVString() {
        StringBuilder ans = new StringBuilder("graph {\n");
        List<TreeNode> vertices = traverseBottomUp(true, true);

        ans.append("  /* Leaves */\n");
        ans.append("  { node [shape=invtriangle] rank=sink\n");
        L().forEach(v -> ans.append(String.format("    %d [label=\"%s\"]\n", v, getLabel(v))));
        ans.append("  }\n\n");

        ans.append("  /* Vertices */\n");
        ans.append("  { node [shape=circle]\n   ");
        V().forEach(v -> ans.append(" ").append(v));
        ans.append(";\n  }\n\n");

        ans.append("  /* Edges */\n");
        for (int i = 0; i < vertices.size() - 1; i++) {
            TreeNode node = vertices.get(i);
            int parentNum = vertices.indexOf(node.getParent());
            // ans.append(String.format("    %d -- %d;\n", i+1, parentNum+1));
            ans.append(String.format("    %d -- %d\n", parentNum + 1, i + 1));
        }

        ans.append("}\n");

        return ans.toString();
    }

    private IntStream L() {
        /* Leaves */
        if (hasFictitiousRoot)
            return IntStream.rangeClosed(0, n);
        else
            return IntStream.rangeClosed(1, n);
    }

    private IntStream V() {
        /* Vertices */
        return IntStream.rangeClosed(n + 1, getRootNum());
    }

    private IntStream V_() {
        /* Vertices without Root */
        return IntStream.range(n + 1, getRootNum());
    }

    private IntStream LV_() {
        /* Leaves and Vertices without Root */
        return IntStream.concat(L(), V_());
    }

    private int getRootNum() {
        /* Root */
        if (hasFictitiousRoot())
            return 2 * n;
        else
            return 2 * n - 1;
    }

    @Override
    public String toString() {
        return String.format("{Tree@%s :: %s}",
                Integer.toHexString(hashCode()),
                traverseBottomUp(true, true));
    }
}
