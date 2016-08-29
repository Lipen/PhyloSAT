import beepp.util.RangeUnion;
import util.FilteredIterable;
import util.Range;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Moklev Vyacheslav
 */
public class BEEFormulaBuilder {
    private List<PhylogeneticTree> trees;
    private int k;
    private int n;
    private boolean enableReticulationConnection;
    private StringBuilder sb;
    private int tempVarCounter;

    public BEEFormulaBuilder(List<PhylogeneticTree> trees,
                             int hybridisationNumber,
                             boolean enableReticulationConnection) {
        this.trees = trees;
        this.k = hybridisationNumber;
        this.enableReticulationConnection = enableReticulationConnection;
        this.n = trees.get(0).getTaxaSize() - 1; // TODO check
        this.sb = new StringBuilder();
        this.tempVarCounter = 0;
    }

    public String build() {
        declareVariables();
        declareConstraints();
        return sb.toString();
    }

    private void declareConstraints() {
        declareNetworkStructureConstraints();
        // TODO add more constraints
    }

    private void declareNetworkStructureConstraints() {
        declareChildrenOrder();
        declareParentsOrder();
        declareParentChildrenConnection();
        declareParentChildrenOrderR();
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
            for (int u : V().intersect(PC(v))) {
                printlnf("%s = %d => %s = %d", var("l", v), u, var("p", u), v);
                printlnf("%s = %d => %s = %d", var("r", v), u, var("p", u), v);
                printlnf("%s = %d => (%s = %d | %s = %d)", var("p", u), v, var("l", v), u, var("r", v), u);
            }
        }
    }

    private void declareParentChildrenConnectionVR() {
        for (int v: V()) {
            for (int u: R().intersect(PC(v))) {
                printlnf("%s = %d => (%s = %d | %s = %d)", var("l", v), u, var("pl", u), v, var("pr", u), v);
                printlnf("%s = %d => (%s = %d | %s = %d)", var("r", v), u, var("pl", u), v, var("pr", u), v);
                printlnf("%s = %d => (%s = %d | %s = %d)", var("pl", u), v, var("l", v), u, var("r", v), u);
                printlnf("%s = %d => (%s = %d | %s = %d)", var("pr", u), v, var("l", v), u, var("r", v), u);
            }
        }
    }

    private void declareParentChildrenConnectionRV() {
        for (int v: R()) {
            for (int u: V().intersect(PC(v))) {
                printlnf("%s = %d <=> %s = %d", var("c", v), u, var("p", u), v);
            }
        }
    }

    private void declareParentChildrenConnectionRR() {
        for (int v: R()) {
            for (int u: R().intersect(PC(v))) {
                printlnf("%s = %d => (%s = %d | %s = %d)", var("c", v), u, var("pl", u), v, var("pr", u), v);
                printlnf("%s = %d => %s = %d", var("pl", u), v, var("c", v), u);
                printlnf("%s = %d => %s = %d", var("pr", u), v, var("c", v), u);
            }
        }
    }

    private void declareParentChildrenOrderR() {
        for (int v: R()) {
            println(var("c", v), " < ", var("pl", v));
            println(var("c", v), " < ", var("pr", v));
        }
    }

    private void println(Object... parts) {
        for (Object part : parts) {
            sb.append(part);
        }
        sb.append("\n");
    }

    private void printlnf(String format, Object... args) {
        sb.append(String.format(format + "\n", args));
    }

    private String tempVar() {
        return "temp" + tempVarCounter++;
    }

    private void declareBool(String name) {
        println("bool ", name);
    }

    private void declareInt(String name, RangeUnion domain) {
        //println("int ", name, ": ", domain.toBEEppString()); TODO uncomment when bug will be fixed
        println("int ", name, ": ", domain.lowerBound(), "..", domain.upperBound());
    }

    private void declareInt(String name, int min, int max) {
        if (min > max)
            throw new IllegalArgumentException("Trying to declare int with min > max (" + min + " > " + max + ")");
        declareInt(name, new RangeUnion(min, max));
    }

    private void declareInt(String name, Iterable<Integer> domain) {
        if (!domain.iterator().hasNext()) // stands for domain.empty()
            throw new IllegalArgumentException("Trying to declare int with an empty domain");
        RangeUnion ranges = new RangeUnion();
        // TODO improve performance
        for (int x: domain) {
            ranges.addRange(x, x);
        }
        declareInt(name, ranges);
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
        for (int vt: Vt()) {
            for (int t: T()) {
                declareInt(var("x", vt, t), V());
            }
        }
        for (int v: R()) {
            for (int t: T()) {
                declareBool(var("d", v, t));
                declareBool(var("ur", v, t));
            }
        }
        for (int v: V()) {
            for (int t : T()) {
                declareBool(var("u", v, t));
                declareInt(var("a", v, t), PU(v));
            }
        }
        // TODO add more
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

    private Range Vt() {
        return new Range(n + 1, 2 * n); // TODO check
    }

    private Range T() {
        return new Range(0, trees.size() - 1);
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

    // TODO add smart heuristics for PC, PP and PU based of input trees
    private Iterable<Integer> PC(int v) {
        if (enableReticulationConnection) {
            if (!LVR().contains(v))
                throw new IllegalArgumentException("v is not in range [0, 2 * (n + k)]: v = " + v + ", range is [0, " + 2 * (n + k) + "]");
            return FilteredIterable.notIs(root(), LVR());
        } else {
            if (L().contains(v)) {
                return Collections.emptyList();
            } else if (V().contains(v)) {
                return FilteredIterable.notIs(root(), VR());
            } else { // holds that v ∈ R (v ∈ L ∪ V ∪ R, v ∉ L, v ∉ V ⇒ v ∈ R)
                return FilteredIterable.notIs(root(), V());
            }
        }
    }

    private Iterable<Integer> PP(int v) {
        if (!LVR().contains(v))
            throw new IllegalArgumentException("v is not in range [0, 2 * (n + k)]: v = " + v + ", range is [0, " + 2 * (n + k) + "]");
        if (enableReticulationConnection) {
            return VR();
        } else {
            if (v == root()) {
                return Collections.emptyList();
            } else if (V().contains(v)) { // if v ∈ V \ {ρ}
                return VR();
            } else if (R().contains(v)) {
                return V();
            } else { // holds that v ∈ L (v ∈ L ∪ V ∪ R, v ∉ V, v ∉ R ⇒ v ∈ L)
                return VR();
            }
        }
    }

    private Iterable<Integer> PU(int v) {
        if (!LVR().contains(v))
            throw new IllegalArgumentException("v is not in range [0, 2 * (n + k)]: v = " + v + ", range is [0, " + 2 * (n + k) + "]");
        return VR();
    }

    private String var(String prefix, int... params) {
        // TODO maybe add some runtime check: if all created vars where declared?
        return prefix + Arrays.stream(params)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining("_"));
    }

}
