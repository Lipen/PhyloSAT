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
            solveEx(p.hybridizationNumber, p.maxChildren, p.maxParents, p.maxTimeLimit, p.prefix, p.isExternal);
        } else {
            for (int k = 0; k <= p.checkFirst; k++) {
                if (solveEx(k, p.maxChildren, p.maxParents, p.firstTimeLimit, p.prefix, p.isExternal))
                    return;
            }

            int n = clusters.get(0).getTaxaSize();

            solveEx(n - 1, p.maxChildren, p.maxParents, INFINITE_TIMEOUT, p.prefix, p.isExternal);

            for (int k = n - 2; k >= 0; k--)
                if (!solveEx(k, p.maxChildren, p.maxParents, p.maxTimeLimit, p.prefix, p.isExternal))
                    return;
        }
    }

    private boolean solveEx(int k, int m1, int m2, long tl, String prefix, boolean isExternal) {
        System.out.println("[*] solveEx() :: n=" + clusters.get(0).getTaxaSize() + ", k=" + k);

        String beeppFileName = prefix + uuid.toString() + "_out.beepp";
        String beeFileName = prefix + uuid.toString() + "_out.bee";
        String dimacsFileName = prefix + uuid.toString() + "_out.dimacs";
        String mapFileName = prefix + uuid.toString() + "_out.map";

        System.out.println("[*] Building BEE++ formula...");
        Formula formula = new FormulaBuilder(clusters, k, m1, m2).build();
        System.out.println("[+] Building BEE++ formula: OK");

        // System.out.println("[*] Dumping BEE++ formula...");
        // formula.dump(beeppFileName);
        // System.out.println("[+] Dumping BEE++ formula: OK");

        System.out.println("[*] Compiling BEE++ to BEE...");
        BEEppCompiler.fastCompile(formula.toString(), beeFileName);
        System.out.println("[+] Compiling BEE++ to BEE: OK");

        System.out.println("[*] Solving...");
        long[] time_solve = new long[1];
        long time_total = System.currentTimeMillis();
        Solver solver;
        if (isExternal)
            solver = new SolverCryptominisat(beeFileName, dimacsFileName, mapFileName, 16);
        else
            solver = new SolverCombined(beeFileName);
        Map<String, Object> solution = solver.resolve(tl, time_solve);
        time_total = System.currentTimeMillis() - time_total;
        System.out.println("[.] Execution times (ms):");
        System.out.println("  > Solve: " + time_solve[0]);
        System.out.println("  > Total: " + time_total);
        System.out.println("  > Limit: " + tl);
        if (solution != null)
            System.out.println("[+] Solving: OK");
        else
            System.out.println("[-] Solving: no solution");

        Main.deleteFile(beeFileName);

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
