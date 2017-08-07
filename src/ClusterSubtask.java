import beepp.BEEppCompiler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            solveEx(p.hybridizationNumber, p.maxTimeLimit, p);
        } else {
            for (int k = 0; k <= p.checkFirst; k++) {
                if (solveEx(k, p.firstTimeLimit, p))
                    return;
            }

            int n = clusters.get(0).getTaxaSize();

            solveEx(n - 1, INFINITE_TIMEOUT, p);

            for (int k = n - 2; k >= 0; k--)
                if (!solveEx(k, p.maxTimeLimit, p))
                    return;
        }
    }

    private boolean solveEx(int k, long tl, SolveParameters p) {
        int n = clusters.get(0).getTaxaSize();
        System.out.println("[*] solveEx() :: n=" + n + ", k=" + k);

        String beeppFileName = p.prefix + uuid.toString() + "_out.beepp";
        String beeFileName = p.prefix + uuid.toString() + "_out.bee";
        String dimacsFileName = p.prefix + uuid.toString() + "_out.dimacs";
        String mapFileName = p.prefix + uuid.toString() + "_out.map";

        System.out.println("[*] Building BEE++ formula...");
        int m1 = p.maxChildren;
        int m2 = p.maxParents;
        Formula formula = new FormulaBuilder(clusters, k, m1, m2).build();
        System.out.println("[+] Building BEE++ formula: OK");

        if (p.isDumping) {
            formula.dump(beeppFileName);
        }

        System.out.println("[*] Compiling BEE++ to BEE: <" + beeFileName + ">");
        BEEppCompiler.fastCompile(formula.toString(), beeFileName);
        System.out.println("[+] Compiling BEE++ to BEE: OK");

        System.out.println("[*] Solving...");
        long[] time_solve = new long[1];
        long time_total = System.currentTimeMillis();
        Solver solver;
        if (p.isExternal)
            solver = new SolverCryptominisat(beeFileName, dimacsFileName, mapFileName, p.threads);
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

        if (!p.isDumping) {
            deleteFile(beeFileName);
            if (p.isExternal) {
                deleteFile(dimacsFileName);
                deleteFile(mapFileName);
            }
        }

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

    private static void deleteFile(String filename) {
        System.out.println("[*] Deleting <" + filename + ">...");
        try {
            Files.delete(Paths.get(filename));
            System.out.println("[+] Deleting <" + filename + ">: OK");
        } catch (FileNotFoundException e) {
            System.err.println("[-] No such file: <" + filename + ">");
        } catch (IOException e) {
            System.err.println("[!] So sad: " + e.getMessage());
        }
    }
}
