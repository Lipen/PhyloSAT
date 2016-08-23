import com.sun.deploy.ui.UITextArea;
import util.Range;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Moklev Vyacheslav
 */
public class BEEFormulaBuilder {
    private List<PhylogeneticTree> trees;
    private int k;
    private int n;
    private StringBuilder sb;

    public BEEFormulaBuilder(List<PhylogeneticTree> trees,
                             int hybridisationNumber) {
        this.trees = trees;
        this.k = hybridisationNumber;
        this.n = trees.get(0).getTaxaSize() - 1; // TODO check
        this.sb = new StringBuilder();
    }

    public String build() {

        return sb.toString();
    }

    private Iterable<Integer> L() {
        return new Range(0, n);
    }

    private Iterable<Integer> V() {
        return new Range(n + 1, 2 * n + k);
    }

    private Iterable<Integer> R() {
        return new Range(2 * n + k + 1, 2 * (n + k));
    }

    // TODO add smart heuristics for PC, PP and PU based of input trees
    private Iterable<Integer> PC(int v) {
        return new Range(0, 2 * (n + k));
    }

    private Iterable<Integer> PP(int v) {
        return new Range(n + 1, 2 * (n + k));
    }

    private Iterable<Integer> PU(int v) {
        return new Range(n + 1, 2 * (n + k));
    }

    private String var(String prefix, int... params) {
        // TODO maybe add some runtime check: if all created vars where declared?
        return prefix + Arrays.stream(params)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining("_"));
    }

}
