import beepp.BEEppCompiler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

class ClusterSubtask extends Subtask {
    private final List<Tree> clusters;

    ClusterSubtask(List<Tree> clusters) {
        this.clusters = clusters;
    }

    @Override
    String getLabel() {
        return String.join("+", clusters.get(0).getLabels());
    }

    void solve(int maxChildren,
               int maxParents,
               long firstTimeLimit,
               long maxTimeLimit,
               int checkFirst,
               long[] executionTime) throws IOException {
        normalize();

        if (firstTimeLimit > 0)
            firstTimeLimit *= 1000;
        if (maxTimeLimit > 0)
            maxTimeLimit *= 1000;

        for (int k = 0; k <= checkFirst; k++) {
            if (solveEx(k, maxChildren, maxParents, firstTimeLimit, executionTime))
                return;
        }

        int n = clusters.get(0).getTaxaSize();

        solveEx(n - 1, maxChildren, maxParents, INFINITE_TIMEOUT, executionTime);

        for (int k = n - 2; k >= 0; k--) {
            if (solveEx(k, maxChildren, maxParents, maxTimeLimit, executionTime))
                break;
        }

        // TODO: Maybe denormalize network here?
    }

    boolean solveEx(int k, int m1, int m2, long tl, long[] time) throws IOException {
        System.out.println("[*] solveEx() :: n=" + clusters.get(0).getTaxaSize() + ", k=" + k);

        System.out.println("[*] Building BEE++ formula...");
        String formula = new FormulaBuilder(clusters, k, m1, m2).build();
        System.out.println("[+] Building BEE++ formula: OK");
        // DUMP BEEpp FORMULA
        // System.out.println("[*] Dumping BEE++ formula...");
        try (PrintWriter out = new PrintWriter("out.beepp")) {
            out.println(formula);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
        // System.out.println("[+] Dumping BEE++ formula: OK");
        //
        System.out.println("[*] Compiling BEE++ to BEE...");
        BEEppCompiler.fastCompile(formula, new FileOutputStream("out.bee"));
        System.out.println("[+] Compiling BEE++ to BEE: OK");
        System.out.println("[*] Solving...");
        Map<String, Object> solution = Runner.resolve("out.bee", tl, time);

        if (solution != null)
            System.out.println("[+] Solving: OK");
        else
            System.out.println("[-] Solving: no solution");

        if (solution == null)
            return false;

        System.out.println("[*] Building network...");
        this.answer = new Network(clusters, k, solution);
        System.out.println("[+] Building network: OK");

        return true;
    }

    private void normalize() {
        // TODO
    }
}
