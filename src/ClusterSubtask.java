import beepp.BEEppCompiler;

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

    @Override
    void solve(SolveParameters p) {
        normalize();

        long[] executionTime = new long[1];

        if (p.hybridizationNumber >= 0) {
            solveEx(p.hybridizationNumber, p.maxChildren, p.maxParents, p.maxTimeLimit, executionTime);
        } else {
            for (int k = 0; k <= p.checkFirst; k++) {
                if (solveEx(k, p.maxChildren, p.maxParents, p.firstTimeLimit, executionTime))
                    return;
            }

            int n = clusters.get(0).getTaxaSize();

            solveEx(n - 1, p.maxChildren, p.maxParents, INFINITE_TIMEOUT, executionTime);

            for (int k = n - 2; k >= 0; k--) {
                if (solveEx(k, p.maxChildren, p.maxParents, p.maxTimeLimit, executionTime))
                    break;
            }
        }
    }

    private boolean solveEx(int k, int m1, int m2, long tl, long[] time) {
        System.out.println("[*] solveEx() :: n=" + clusters.get(0).getTaxaSize() + ", k=" + k);

        System.out.println("[*] Building BEE++ formula...");
        String formula = new FormulaBuilder(clusters, k, m1, m2).build();
        System.out.println("[+] Building BEE++ formula: OK");

        // DUMP BEEpp FORMULA
        // System.out.println("[*] Dumping BEE++ formula...");
        try (PrintWriter out = new PrintWriter("out.beepp")) {
            out.println(formula);
        } catch (IOException e) {
            System.err.println("[!] So sad: " + e.getMessage());
            e.printStackTrace();
        }
        // System.out.println("[+] Dumping BEE++ formula: OK");
        //

        System.out.println("[*] Compiling BEE++ to BEE...");
        BEEppCompiler.fastCompile(formula, "out.bee");
        System.out.println("[+] Compiling BEE++ to BEE: OK");

        System.out.println("[*] Solving...");
        Map<String, Object> solution = Runner.resolve("out.bee", tl, time);
        System.out.println("[.] Execution time: " + time[0] + " / " + tl + " (ms)");
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
