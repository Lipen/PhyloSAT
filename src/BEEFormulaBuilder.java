import beepp.util.RangeUnion;
import util.FilteredIterable;
import util.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Moklev Vyacheslav
 */
public class BEEFormulaBuilder {
    private final List<PhylogeneticTree> trees;
    private final int k;
    private final int n;
    private final boolean enableReticulationConnection;
    private final StringBuilder sb;

    public BEEFormulaBuilder(List<PhylogeneticTree> trees,
                             int hybridisationNumber,
                             boolean enableReticulationConnection) {
        this.trees = trees;
        this.k = hybridisationNumber;
        this.enableReticulationConnection = enableReticulationConnection;
        this.n = trees.get(0).getTaxaSize() - 1;
        this.sb = new StringBuilder();
        System.out.println("n = " + n + ", k = " + k);
        System.out.println("L = " + makeList(L()));
        System.out.println("V = " + makeList(V()));
        System.out.println("R = " + makeList(R()));
        System.out.println("T = " + makeList(T()));
    }

    private <T> List<T> makeList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for (T t : iterable) {
            list.add(t);
        }
        return list;
    }

    public String build() {
        declareVariables();     // [2/2] done
        declareConstraints();   // [2/3] TODO
        return sb.toString();
    }


    private void declareVariables() {
        // Network structure
        for (int v : V()) {
            declareInt(var("l", v), PC(v));
            declareInt(var("r", v), PC(v));
        }
        for (int v : FilteredIterable.notIs(root(), LV())) { // v ∈ L ∪ V \ {ρ}
            declareInt(var("p", v), PP(v));
        }
        for (int v : R()) {
            declareInt(var("pl", v), PP(v));
            declareInt(var("pr", v), PP(v));
            declareInt(var("c", v), PC(v));
        }

        // Trees to network mapping
        for (int vt : Vt()) {
            for (int t : T()) {
                declareInt(var("x", vt, t), V());
            }
        }
        for (int v : R()) {
            for (int t : T()) {
                declareBool(var("d", v, t));
                if (enableReticulationConnection) { // TODO check
                    declareBool(var("ur", v, t));
                }
            }
        }
        for (int v : V()) {
            for (int t : T()) {
                declareBool(var("u", v, t));
            }
        }
        for (int v : LVR()) { // allNodes() in FormulaBuilder
            for (int t : T()) {
                declareInt(var("a", v, t), PU(v));
            }
        }
    }

    private void declareInt(String name, int min, int max) {
        if (min > max)
            throw new IllegalArgumentException("Trying to declare int with min > max (" + min + " > " + max + ")");
        declareInt(name, new RangeUnion(min, max));
    }

    private void declareInt(String name, Iterable<Integer> domain) {
        if (!domain.iterator().hasNext()) // stands for domain.empty()
            throw new IllegalArgumentException("Trying to declare int with an empty domain (name: " + name + ")");
        RangeUnion ranges = new RangeUnion();
        // TODO improve performance
        for (int x : domain) {
            ranges.addRange(x, x);
        }
        declareInt(name, ranges);
    }

    private void declareInt(String name, RangeUnion domain) {
        // FIXME wisely choose dual_int / int
        println("dual_int ", name, ": ", domain.toBEEppString()); // TODO uncomment when bug will be fixed
        // println("int ", name, ": ", domain.lowerBound(), "..", domain.upperBound());
    }

    private void declareBool(String name) {
        println("bool ", name);
    }


    private void declareConstraints() {
        println("// 1. Network structure constraints");
        declareNetworkStructureConstraints(); // [4/4] done
        println("// 2. Trees to network mapping");
        declareTreesToNetworkMapping();       // [6/7] TODO
        println("// 3. Parent to children relation");
        declareParentChildrenRelation();      // [5/5] done
    }


    private void declareNetworkStructureConstraints() {
        println("// 1.1 Children order");
        declareChildrenOrder();             // done
        println("// 1.2 Parents order");
        declareParentsOrder();              // done
        println("// 1.3 Parents to children connection");
        declareParentChildrenConnection();  // [4/4] done
        println("// 1.4 R children order");
        declareParentChildrenOrderR();      // done
    }

    private void declareChildrenOrder() {
        for (int v : V()) {
            println(var("l", v), " < ", var("r", v));
        }
    }

    private void declareParentsOrder() {
        for (int v : R()) {
            println(var("pl", v), " < ", var("pr", v));
        }
    }

    private void declareParentChildrenConnection() {
        declareParentChildrenConnectionVV();
        declareParentChildrenConnectionVR();
        declareParentChildrenConnectionRV();
        declareParentChildrenConnectionRR();
    }

    private void declareParentChildrenConnectionVV() {
        for (int v : V()) {
            for (int u : LV().intersect(PC(v))) {
                printlnf("%s = %d => %s = %d", var("l", v), u, var("p", u), v);
                printlnf("%s = %d => %s = %d", var("r", v), u, var("p", u), v);
                printlnf("%s = %d => (%s = %d | %s = %d)", var("p", u), v, var("l", v), u, var("r", v), u);
            }
        }
    }

    private void declareParentChildrenConnectionVR() {
        for (int v : V()) {
            for (int u : R().intersect(PC(v))) {
                printlnf("%s = %d => (%s = %d | %s = %d)", var("l", v), u, var("pl", u), v, var("pr", u), v);
                printlnf("%s = %d => (%s = %d | %s = %d)", var("r", v), u, var("pl", u), v, var("pr", u), v);
                printlnf("%s = %d => (%s = %d | %s = %d)", var("pl", u), v, var("l", v), u, var("r", v), u);
                printlnf("%s = %d => (%s = %d | %s = %d)", var("pr", u), v, var("l", v), u, var("r", v), u);
            }
        }
    }

    private void declareParentChildrenConnectionRV() {
        for (int v : R()) {
            for (int u : LV().intersect(PC(v))) {
                printlnf("%s = %d <=> %s = %d", var("c", v), u, var("p", u), v);
            }
        }
    }

    private void declareParentChildrenConnectionRR() {
        for (int v : R()) {
            for (int u : R().intersect(PC(v))) {
                printlnf("%s = %d => (%s = %d | %s = %d)", var("c", v), u, var("pl", u), v, var("pr", u), v);
                printlnf("%s = %d => %s = %d", var("pl", u), v, var("c", v), u);
                printlnf("%s = %d => %s = %d", var("pr", u), v, var("c", v), u);
            }
        }
    }

    private void declareParentChildrenOrderR() {
        for (int v : R()) {
            println(var("c", v), " < ", var("pl", v));
            println(var("c", v), " < ", var("pr", v));
        }
    }


    private void declareTreesToNetworkMapping() {
        declareAMOx();                    // done
        declareXUConnection();            // done
        declareRootMapping();             // done
        declareLeafXAConnection();        // done
        declareVertexXAConnection();      // done
        declareMappingOrder();            // done
        declareHeuristicsConstraints();   // TODO
    }

    private void declareAMOx() {
        for (int v : V()) {
            for (int t : T()) {
                List<String> list = new ArrayList<>();
                for (int vt : Vt()) {
                    list.add(var("x", vt, t) + " = " + v);
                }
                println("AMO(" + list.stream().collect(Collectors.joining(", ")), ")");
            }
        }
    }

    private void declareXUConnection() {
        for (int t : T()) {
            for (int vt : Vt()) {
                for (int v : V()) {
                    printlnf("(%s = %d) => %s", var("x", vt, t), v, var("u", v, t));
                }
            }
        }
    }

    private void declareRootMapping() {
        for (int t : T()) {
            printlnf("%s = %d", var("x", rootT(), t), root());
        }
    }

    private void declareLeafXAConnection() {
        for (int t : T()) {
            for (int v : L()) {
                // vt = v: same numeration of leaves in the net and trees
                // noinspection OptionalGetWithoutIsPresent -- v is a leaf, so parent is always present
                int ut = parent(t, v).get();
                printlnf("%s = %s", var("x", ut, t), var("a", v, t));
            }
        }
    }

    private void declareVertexXAConnection() {
        for (int t : T()) {
            for (int v : V()) {
                for (int vt : Vt()) {
                    Optional<Integer> maybeUt = parent(t, vt);
                    if (maybeUt.isPresent()) {
                        int ut = maybeUt.get();
                        printlnf("(%s = %d) => (%s = %s)", var("x", vt, t), v, var("x", ut, t), var("a", v, t));
                    }
                }
            }
        }
    }

    private void declareMappingOrder() {
        for (int t : T()) {
            for (int v : V()) {
                for (int u : V()) {
                    for (int vt : Vt()) {
                        if (u < v) {
                            Optional<Integer> maybeUt = parent(t, vt);
                            if (maybeUt.isPresent()) {
                                int ut = maybeUt.get();
                                printlnf("(%s = %d) => (%s != %d)", var("x", vt, t), v, var("x", ut, t), u);
                            }
                        }
                    }
                }
            }
        }
    }

    private void declareHeuristicsConstraints() {
        // TODO absurdish
    }


    private void declareParentChildrenRelation() {
        println("// 3.1 PCR 1");
        declarePCR1();    // done
        println("// 3.2 PCR 2");
        declarePCR2();    // done
        println("// 3.3 PCR 3");
        declarePCR3();    // done
        if (enableReticulationConnection) {
            println("// 3.4 PCR 4");
            declarePCR4();    // done
        }
        println("// 3.5 PCR 5");
        declarePCR5();    // done
    }

    private void declarePCR1() {
        for (int t : T()) {
            for (int v : V().union(L())) {
                for (int u : V().intersect(PP(v))) {
                    printlnf("%s = %d => (%s <=> %s = %d)", var("p", v), u, var("u", u, t), var("a", v, t), u);

                    // TODO check if PP -> PU change can break something
                    // for (int w: PP(u)) {
                    //    printlnf("(%s = %d & !%s) => (%s = %d <=> %s = %d)", var("p", v), u, var("u", u, t),
                    //            var("a", u, t), w, var("a", v, t), w);
                    // }
                    printlnf("(%s = %d & !%s) => (%s = %s)", var("p", v), u, var("u", u, t),
                            var("a", u, t), var("a", v, t));

                }
            }
        }
    }

    private void declarePCR2() {
        for (int t : T()) {
            for (int v : R()) {
                for (int u : R().intersect(PP(v))) {
                    printlnf("(%s = %d & %s) => (%s = %s)",
                            var("pl", v), u, var("d", v, t), var("a", u, t), var("a", v, t));
                    printlnf("(%s = %d & !%s) => (%s = %s)",
                            var("pr", v), u, var("d", v, t), var("a", u, t), var("a", v, t));
                }
            }
        }
        for (int t : T()) {
            for (int v : R()) {
                for (int u : V().intersect(PP(v))) {
                    printlnf("(%s = %d & %s & %s) => %s = %d",
                            var("pl", v), u, var("d", v, t), var("u", u, t), var("a", v, t), u);
                    printlnf("(%s = %d & !%s & %s) => %s = %d",
                            var("pr", v), u, var("d", v, t), var("u", u, t), var("a", v, t), u);
                    printlnf("(%s = %d & %s & !%s) => %s = %s",
                            var("pl", v), u, var("d", v, t), var("u", u, t), var("a", u, t), var("a", v, t));
                    printlnf("(%s = %d & !%s & !%s) => %s = %s",
                            var("pr", v), u, var("d", v, t), var("u", u, t), var("a", u, t), var("a", v, t));
                }
            }
        }
    }

    private void declarePCR3() {
        for (int t : T()) {
            for (int v : R()) {
                if (enableReticulationConnection) { // TODO check
                    for (int u : R().intersect(PP(v))) {
                        printlnf("(%s = %d & !%s) => !%s",
                                var("pl", v), u, var("d", v, t), var("ur", u, t));
                        printlnf("(%s = %d & %s) => !%s",
                                var("pr", v), u, var("d", v, t), var("ur", u, t));
                    }
                }
                for (int u : V().intersect(PP(v))) {
                    printlnf("(%s = %d & !%s) => !%s",
                            var("pl", v), u, var("d", v, t), var("u", u, t));
                    printlnf("(%s = %d & %s) => !%s",
                            var("pr", v), u, var("d", v, t), var("u", u, t));
                }
            }
        }
    }

    private void declarePCR4() {
        for (int t : T()) {
            for (int v : R()) {
                for (int u : R().intersect(PP(v))) {
                    printlnf("(%s = %d & %s & %s) => %s",
                            var("pl", v), u, var("d", v, t), var("ur", v, t), var("ur", u, t));
                    printlnf("(%s = %d & !%s & %s) => %s",
                            var("pr", v), u, var("d", v, t), var("ur", v, t), var("ur", u, t));
                    printlnf("(%s = %d & !%s) => !%s",
                            var("c", u), v, var("ur", v, t), var("ur", u, t));
                }
                for (int u : V().intersect(PP(v))) {
                    printlnf("(%s = %d & !%s) => !%s",
                            var("pl", v), u, var("ur", v, t), var("u", u, t));
                    printlnf("(%s = %d & !%s) => !%s",
                            var("pr", v), u, var("ur", v, t), var("u", u, t));
                }
                for (int u : LV().intersect(PC(v))) {
                    printlnf("%s = %d => %s", var("c", v), u, var("ur", v, t));
                }
            }
        }
    }

    private void declarePCR5() {
        for (int t : T()) {
            for (int v : LV()) {
                for (int u : R().intersect(PP(v))) {
                    for (int w : PU(u)) {
                        if (w <= v) {
                            printlnf("%s = %d => %s != %d", var("p", v), u, var("a", u, t), w);
                        } else {
                            printlnf("(%s = %d & %s = %d) => %s = %d",
                                    var("p", v), u, var("a", u, t), w, var("a", v, t), w);
                            printlnf("(%s = %d & %s = %d) => %s = %d",
                                    var("p", v), u, var("a", v, t), w, var("a", u, t), w);
                        }
                    }
                }
            }
        }
    }


    private void println(Object... parts) {
        for (Object part : parts) {
            sb.append(part);
        }
        sb.append("\n");
    }

    private void printlnf(String format, Object... args) {
        sb.append(String.format(format, args)).append('\n');
    }

    private Integer min(Iterable<? extends Integer> iterable) {
        Integer curMin = null;
        for (int x : iterable) {
            if (curMin == null || x < curMin)
                curMin = x;
        }
        return curMin;
    }

    private Integer max(Iterable<? extends Integer> iterable) {
        Integer curMax = null;
        for (int x : iterable) {
            if (curMax == null || x > curMax)
                curMax = x;
        }
        return curMax;
    }

    private Optional<Integer> parent(int t, int vt) {
        int p = trees.get(t).getParent(vt);
        return p >= 0 ? Optional.of(p) : Optional.empty();
    }

    private Range Vt() {
        return new Range(n + 1, 2 * n);
    }

    private Range T() {
        return new Range(0, trees.size() - 1);
    }

    private int rootT() {
        return 2 * n;
    }

    private int root() {
        return 2 * n + k; // TODO really it is root?
    }

    private Range LVR() {
        return new Range(0, 2 * (n + k));
    }

    private Range LV() {
        return new Range(0, 2 * n + k);
    }

    private Range VR() {
        return new Range(n + 1, 2 * (n + k));
    }

    private Range L() {
        return new Range(0, n);
    }

    private Range V() {
        return new Range(n + 1, 2 * n + k);
    }

    private Range R() {
        return new Range(2 * n + k + 1, 2 * (n + k));
    }

    private List<Integer> PC(int v) {
        int treeNodesCount = 2 * n + 1 + k;
        if (v < 0 || v >= treeNodesCount + k) {
            throw new RuntimeException("Node number out of bounds: " + v);
        }

        List<Integer> ans = new ArrayList<>();
        if (v < n) {
            return ans;
        }
        for (int childNumber : LVR()) {
            if (v < treeNodesCount) {
                if (childNumber < v || childNumber >= treeNodesCount) {
                    ans.add(childNumber);
                }
            } else if (childNumber < treeNodesCount - 1 || (enableReticulationConnection && childNumber < v)) {
                ans.add(childNumber);
            }
        }

        return ans;
    }

    private List<Integer> PP(int v) {
        int treeNodesCount = 2 * n + 1 + k;
        if (v < 0 || v >= treeNodesCount + k) {
            throw new RuntimeException("Node number out of bounds");
        }

        List<Integer> ans = new ArrayList<>();
        if (v == treeNodesCount - 1) {
            return ans;
        }
        for (int parentNumber = n; parentNumber < treeNodesCount + k; parentNumber++) {
            if (v < n) {
                ans.add(parentNumber);
            } else if (v < treeNodesCount) {
                if (v < parentNumber) {
                    ans.add(parentNumber);
                }
            } else if (parentNumber < treeNodesCount) {
                ans.add(parentNumber);
            }
        }
        if (enableReticulationConnection && v >= treeNodesCount)
            ans.add(v + 1);

        return ans;
    }

    private Iterable<Integer> PU(int v) {
        if (!LVR().contains(v))
            throw new IllegalArgumentException("v is not in range [0, 2 * (n + k)]: v = " + v + ", range is [0, " + 2 * (n + k) + "]");
        return VR();
    }

    private String var(String prefix, int... params) {
        // TODO maybe add some runtime check: if all created vars where declared?
        return prefix + Arrays.stream(params)
                .mapToObj(x -> "_" + x)
                .collect(Collectors.joining());
    }

}
