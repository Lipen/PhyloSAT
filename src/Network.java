import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class Network extends Graph {
    private final NetworkNode root;
    private int n;  // Number of taxa
    private int k;  // Hybridization number

    Network(ClusterSubtask subtask, int hybridizationNumber, Map<String, Object> solution) {
        List<Tree> trees = subtask.getClusters();
        Tree basetree = trees.get(0);
        this.n = basetree.getTaxaSize();
        this.k = hybridizationNumber;

        int rho;
        if (basetree.hasFictitiousRoot())
            rho = 2 * n + k;
        else
            rho = 2 * n + k - 1;

        List<NetworkNode> nodes = new ArrayList<>();
        nodes.add(null);
        L().forEach(i -> nodes.add(new LeafNode(i, subtask, basetree.getLabel(i))));
        V(rho).forEach(v -> nodes.add(new InternalNode(v, subtask)));
        R(rho).forEach(r -> nodes.add(new ReticulateNode(r, subtask)));

        // Just forbid 0th vertex even if it exists
        LV_(rho).forEach(v -> {
            NetworkNode vertex = nodes.get(v);
            NetworkNode parent = nodes.get((int) solution.get("p_" + v));
            vertex.setParent(parent);
            parent.addChild(vertex);
        });
        R(rho).forEach(r -> {
            NetworkNode vertex = nodes.get(r);
            V(rho).forEach(p -> {  // note: V = PP(r)
                if ((boolean) solution.get("p_" + r + "_" + p)) {
                    NetworkNode parent = nodes.get(p);
                    vertex.addParent(parent);
                    parent.addChild(vertex);
                }
            });
        });

        // Forbid fictitious root if it exists (if it is not, it's completely OK)
        int rho_tree = 2 * n + k - 1;
        this.root = nodes.get(rho_tree);
        this.root.setParent(null);
    }


    Network(CollapsedSubtask subtask) {
        /* Builds network from just one tree, so it has no reticulate nodes */
        Tree subtree = subtask.getSubtree();
        this.n = subtree.getTaxaSize();
        this.k = 0;

        Map<Integer, Integer> parentMapping = subtree.getParentMapping();
        int rho;
        if (subtree.hasFictitiousRoot()) {
            rho = 2 * n + k;
            throw new RuntimeException("Please, go to hell");
        } else
            rho = 2 * n + k - 1;

        List<NetworkNode> nodes = new ArrayList<>();
        nodes.add(null);
        L().forEach(i -> nodes.add(new LeafNode(i, subtask, subtree.getLabel(i))));
        V(rho).forEach(v -> nodes.add(new InternalNode(v, subtask)));

        LV_(rho).forEach(v -> {
            NetworkNode vertex = nodes.get(v);
            NetworkNode parent = nodes.get(parentMapping.get(v));
            vertex.setParent(parent);
            parent.addChild(vertex);
        });

        int rho_tree = 2 * n + k - 1;
        this.root = nodes.get(rho_tree);
        this.root.setParent(null);
    }

    int getN() {
        return n;
    }

    int getK() {
        return k;
    }
    // Invariant: network has NO fictitious root/leaf

    private List<NetworkNode> getTaxa() {
        List<NetworkNode> taxa = root.getLeaves();
        // TODO: remove assert
        if (taxa.size() != n)
            throw new RuntimeException("AssertionError: maybe getTaxa is broken");
        taxa.sort(Comparator.comparing(Node::getLabel));
        return taxa;
    }

    void substituteSubtask(Subtask subtask) {
        String subtaskLabel = subtask.getLabel();
        boolean flag = true;

        for (NetworkNode leaf : root.getLeaves()) {
            String label = leaf.getLabel();

            if (label.equals(subtaskLabel)) {
                System.out.println("[*] Substituting subtask: " + subtaskLabel);
                injectNetwork(subtask.answer, leaf);
                flag = false;
                break;
            }
        }

        if (flag)
            throw new RuntimeException("Couldn't substitute subtask: " + subtaskLabel);

        this.n = root.getLeaves().size();
        this.k += subtask.answer.getK();
        System.out.println("[.] Now n = " + n + ", k = " + k);
    }

    private void injectNetwork(Network injection, NetworkNode leaf) {
        if (injection == null)
            return;

        NetworkNode leafParent = leaf.getParent();
        Collections.replaceAll(leafParent.getChildren(), leaf, injection.root);
        injection.root.setParent(leafParent);
    }

    private List<NetworkNode> traverseBottomUp(boolean addRoot, boolean addLeaves) {
        Stack<NetworkNode> traversedNodes = new Stack<>();
        Set<NetworkNode> visited = new HashSet<>(root.getLeaves());
        Queue<NetworkNode> queue_BFS = new LinkedList<>();
        if (addRoot)
            queue_BFS.add(root);
        else
            queue_BFS.addAll(root.getChildren());

        while (!queue_BFS.isEmpty()) {
            NetworkNode current = queue_BFS.remove();
            if (visited.add(current)) {
                traversedNodes.add(current);
                queue_BFS.addAll(current.getChildren());
            }
        }

        List<NetworkNode> ans = new ArrayList<>();
        if (addLeaves)
            ans.addAll(getTaxa());
        while (!traversedNodes.isEmpty())
            ans.add(traversedNodes.pop());

        return ans;
    }

    String toGVString() {
        Formatter ans = new Formatter();
        Map<NetworkNode, Integer> m = new HashMap<>();
        m.put(null, 0);  // stub
        List<NetworkNode> leaves = getTaxa();

        ans.format("graph {%n");
        ans.format("  /* Leaves */%n");
        ans.format("  { node [shape=invhouse] rank=sink%n");
        for (int v = 1; v <= n; v++) {
            NetworkNode leaf = leaves.get(v - 1);
            ans.format("    %d [label=\"%s\"]\n", v, leaf.getLabel());
            if (m.size() != v)
                throw new RuntimeException("Ah!");
            m.put(leaf, m.size());
        }
        ans.format("  }\n\n");

        ans.format("  /* Internals and Reticulates */%n");
        List<NetworkNode> traversal = traverseBottomUp(true, false);

        for (NetworkNode node : traversal) {
            m.put(node, m.size());
            if (node instanceof InternalNode)
                // ans.format("    %d [shape=point label=\"\"]%n", m.get(node));
                ans.format("    %d [shape=circle label=\"%s\" fixedsize=true width=0.5]%n", m.get(node), node.getPseudoName());
            else if (node instanceof ReticulateNode)
                // ans.format("    %d [shape=square label=\"\" fixedsize=true width=0.3 height=0.3]%n", m.get(node));
                ans.format("    %d [shape=square label=\"%s\" fixedsize=true width=0.5]%n", m.get(node), node.getPseudoName());
        }
        ans.format("%n");

        ans.format("  /* Edges */%n");
        for (NetworkNode node : traversal) {
            for (NetworkNode child : node.getChildren()) {
                ans.format("    %d -- %d%n", m.get(node), m.get(child));
                // ans.format("    \"%s\" -- \"%s\"%n", node.getIndex(), child.getIndex());
            }
        }

        ans.format("}%n");

        return ans.toString();
    }

    private IntStream L() {
        /* Leaves */
        return IntStream.rangeClosed(1, n);
    }

    private IntStream V(int rho) {
        /* Vertices */
        return IntStream.rangeClosed(n + 1, rho);
    }

    private IntStream V_(int rho) {
        /* Vertices without Root */
        return IntStream.range(n + 1, rho);
    }

    private IntStream R(int rho) {
        /* Reticulate */
        return IntStream.rangeClosed(rho + 1, rho + k);
    }

    private IntStream LV_(int rho) {
        /* Leaves + Vertices without Root */
        return IntStream.concat(L(), V_(rho));
    }

    private abstract class NetworkNode extends Node {
        private final int index;
        private final Subtask subtask;
        private final List<NetworkNode> parents = new ArrayList<>();
        private final List<NetworkNode> children = new ArrayList<>();


        NetworkNode(int index, Subtask subtask, NetworkNode parent, String label) {
            super(label);
            this.index = index;
            this.subtask = subtask;
            parents.add(parent);
        }


        String getPseudoName() {
            return subtask.subprefix + index;
        }

        NetworkNode getParent() {
            return parents.get(0);
        }

        void setParent(NetworkNode newParent) {
            parents.set(0, newParent);
        }

        List<NetworkNode> getParents() {
            return parents;
        }

        void addParent(NetworkNode newParent) {
            parents.add(newParent);
        }

        List<NetworkNode> getChildren() {
            return children;
        }

        void addChild(NetworkNode newChild) {
            children.add(newChild);
        }

        List<NetworkNode> getLeaves() {
            return children.stream()
                    .flatMap(child -> child.getLeaves().stream())
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    private class LeafNode extends NetworkNode {
        LeafNode(int index, Subtask subtask, String label) {
            super(index, subtask, null, label);
        }

        @Override
        List<NetworkNode> getLeaves() {
            return Stream.of(this).collect(Collectors.toList());
        }
    }

    private class InternalNode extends NetworkNode {
        InternalNode(int index, Subtask subtask) {
            super(index, subtask, null, null);
        }
    }

    private class ReticulateNode extends NetworkNode {
        ReticulateNode(int index, Subtask subtask) {
            super(index, subtask, null, null);
        }
    }
}
