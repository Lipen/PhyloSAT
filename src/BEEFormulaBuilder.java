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
        // TODO implement
        declareParentsOrder();
        declareParentChildrenConnection();
//        declareParentChildrenOrderR();
    }

    // ∀v ∈ V. l_v < r_v
    private void declareChildrenOrder() {
        for (int v : V()) {
            println("int_lt(", var("l", v), ", ", var("r", v), ")");
        }
    }

    private void declareParentsOrder() {
        for (int v : R()) {
            println("int_lt(", var("pl", v), ", ", var("pr", v), ")");
        }
    }

    private void declareParentChildrenConnection() {
        // TODO implement
        declareParentChildrenConnectionVV();
//        declareParentChildrenConnectionVR();
//        declareParentChildrenConnectionRV();
//        declareParentChildrenConnectionRR();
    }

    private void println(Object... parts) {
        for (Object part : parts) {
            sb.append(part);
        }
        sb.append("\n");
    }

    private void declareParentChildrenConnectionVV() {
        for (int v : V()) {
            for (int u : V().intersect(PC(v))) {
                /* (l_v = u) ⇒ (p_u = v) */
                {
                    String temp1 = tempVar();
                    String temp2 = tempVar();
                    declareBool(temp1);
                    declareBool(temp2);
                    println("int_eq_reif(", var("l", v), ", ", u, ", ", temp1, ")");
                    println("int_eq_reif(", var("p", u), ", ", v, ", ", temp2, ")");
                    println("bool_ite(", temp1, ", ", temp2, ", true)");
                }
                /* (r_v = u) ⇒ (p_u = v) */
                {
                    String temp1 = tempVar();
                    String temp2 = tempVar();
                    declareBool(temp1);
                    declareBool(temp2);
                    println("int_eq_reif(", var("r", v), ", ", u, ", ", temp1, ")");
                    println("int_eq_reif(", var("p", u), ", ", v, ", ", temp2, ")");
                    println("bool_ite(", temp1, ", ", temp2, ", true)");
                }
                /* (p_u = v) ⇒ (l_v = u || r_v = u) */
                {
                    // TODO optimize count of temp variables?
                    String temp1 = tempVar();
                    String temp2 = tempVar();
                    String temp3 = tempVar();
                    String temp4 = tempVar();
                    declareBool(temp1);
                    declareBool(temp2);
                    println("int_eq_reif(", var("p", u), ", ", v, ", ", temp1, ")");
                    println("int_eq_reif(", var("l", v), ", ", u, ", ", temp2, ")");
                    println("int_eq_reif(", var("r", v), ", ", u, ", ", temp3, ")");
                    println("bool_or_reif(", temp2, ", ", temp3, ", ", temp4, ")");
                    println("bool_ite(", temp1, ", ", temp4, ", true)");
                }
            }
        }
    }

    private String tempVar() {
        return "temp" + tempVarCounter++;
    }

    private void declareBool(String name) {
        println("new_bool(", name, ")");
    }

    private void declareInt(String name, int min, int max) {
        if (min > max)
            throw new IllegalArgumentException("Trying to declare int with min > max (" + min + " > " + max + ")");
        println("new_int(", name, ", ", min, ", ", max, ")");
    }

    private void declareInt(String name, Iterable<Integer> domain) {
        // stands for domain.empty()
        if (!domain.iterator().hasNext())
            throw new IllegalArgumentException("Trying to declare int with an empty domain");
        declareInt(name, min(domain), max(domain)); // TODO if domain is not range -- use "make_int(X, D)"
    }

    private void declareVariables() {
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
