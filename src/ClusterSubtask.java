import beepp.BEEppCompiler;

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

        if (p.hybridizationNumber >= 0) {
            solveEx(p.hybridizationNumber, p.maxChildren, p.maxParents, p.maxTimeLimit, p.prefix);
        } else {
            for (int k = 0; k <= p.checkFirst; k++) {
                if (solveEx(k, p.maxChildren, p.maxParents, p.firstTimeLimit, p.prefix))
                    return;
            }

            int n = clusters.get(0).getTaxaSize();

            solveEx(n - 1, p.maxChildren, p.maxParents, INFINITE_TIMEOUT, p.prefix);

            for (int k = n - 2; k >= 0; k--)
                if (!solveEx(k, p.maxChildren, p.maxParents, p.maxTimeLimit, p.prefix))
                    return;
        }
    }

    private boolean solveEx(int k, int m1, int m2, long tl, String prefix) {
        System.out.println("[*] solveEx() :: n=" + clusters.get(0).getTaxaSize() + ", k=" + k);

        System.out.println("[*] Building BEE++ formula...");
        Formula formula = new FormulaBuilder(clusters, k, m1, m2).build();
        System.out.println("[+] Building BEE++ formula: OK");

        System.out.println("[*] Dumping BEE++ formula...");
        formula.dump(prefix + "out.beepp");
        System.out.println("[+] Dumping BEE++ formula: OK");

        System.out.println("[*] Compiling BEE++ to BEE...");
        BEEppCompiler.fastCompile(formula.toString(), prefix + "out.bee");
        System.out.println("[+] Compiling BEE++ to BEE: OK");

        System.out.println("[*] Solving...");
        long[] time_solve = new long[1];
        long time_total = System.currentTimeMillis();
        Map<String, Object> solution = new SolverCombined(prefix + "out.bee").resolve(tl, time_solve);
        // Map<String, Object> solution = new SolverCryptominisat(prefix + "out.bee", prefix + "out.dimacs", prefix + "out.map", 16).resolve(tl, time_solve);
        time_total = System.currentTimeMillis() - time_total;
        System.out.println("[.] Execution times (ms):");
        System.out.println("  > Solve: " + time_solve[0]);
        System.out.println("  > Total: " + time_total);
        System.out.println("  > Limit: " + tl);
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
