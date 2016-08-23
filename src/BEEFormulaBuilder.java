import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Moklev Vyacheslav
 */
public class BEEFormulaBuilder {
    private List<PhylogeneticTree> trees;
    private int k;
    private StringBuilder sb;

    public BEEFormulaBuilder(List<PhylogeneticTree> trees,
                             int hybridisationNumber) {
        this.trees = trees;
        this.k = hybridisationNumber;
        this.sb = new StringBuilder();
    }

    public String build() {
        
        return sb.toString();
    }

    private String var(String prefix, int... params) {
        return prefix + Arrays.stream(params)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining("_"));
    }

}
