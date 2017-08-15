import beepp.BEEppCompiler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ClusterSubtask extends Subtask {
    private final List<Tree> clusters;


    ClusterSubtask(List<Tree> clusters) {
        this.clusters = clusters;
    }

    private static void deleteFile(String filename) {
        try {
            Files.delete(Paths.get(filename));
            System.out.println("[+] Removing <" + filename + ">: OK");
        } catch (FileNotFoundException e) {
            System.err.println("[-] Removing <" + filename + ">: no such file");
        } catch (IOException e) {
            System.err.println("[!] So sad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    List<Tree> getClusters() {
        return clusters;
    }

    @Override
    int getN() {
        return clusters.get(0).getTaxaSize();
    }

    @Override
    String getLabel() {
        // return String.join("+", clusters.get(0).getLabels());
        return String.join("+", clusters.get(0).getLabels());
    }

    @Override
    void solve(SolveParameters p) {
        normalize();

        if (p.hybridizationNumber >= 0) {
            solveEx(p.hybridizationNumber, p);
        } else {
            for (int k = 0; k <= p.checkFirst; k++)
                if (solveEx(k, p)) {
                    System.out.printf("[#] %s: solution found with k=%d during check-first stage\n", this, k);
                    return;
                }

            int n = clusters.get(0).getTaxaSize();
            if (!solveEx(n - 1, p))
                throw new RuntimeException("No solution at upper bound... weird!");
            for (int k = n - 2; k > p.checkFirst; k--)
                if (!solveEx(k, p)) {
                    System.out.printf("[#] %s: no solution found with k=%d during back-search stage\n", this, k);
                    return;
                }
        }
    }

    private boolean solveEx(int k, SolveParameters p) {
        long time_start = System.currentTimeMillis();
        System.out.printf("[*] %s: trying to solve with k = %d...\n", this, k);

        String beeppFileName = p.prefix + uuid.toString() + "_out.beepp";
        String beeFileName = p.prefix + uuid.toString() + "_out.bee";
        String dimacsFileName = p.prefix + uuid.toString() + "_out.dimacs";
        String mapFileName = p.prefix + uuid.toString() + "_out.map";

        System.out.printf("[*] %s: building BEE++ formula...\n", this);
        int m1 = p.maxChildren;
        int m2 = p.maxParents;
        Formula formula = new FormulaBuilder(clusters, k, m1, m2).build();
        System.out.printf("[+] %s: done building BEE++ formula\n", this);

        if (p.isDumping) {
            formula.dump(beeppFileName);
        }

        System.out.printf("[*] %s: compiling BEE++ to BEE (<%s>)...\n", this, beeFileName);
        BEEppCompiler.fastCompile(formula.toString(), beeFileName, p.numberOfSolutions);
        System.out.printf("[+] %s: done compiling BEE++ to BEE\n", this);

        System.out.printf("[*] %s: calling solver...\n", this);
        Solver solver;
        if (p.isExternal)
            solver = new SolverCryptominisat(beeFileName, dimacsFileName, mapFileName, p.threads);
        else
            solver = new SolverCombined(beeFileName, p.numberOfSolutions);
        List<Map<String, Object>> solutions = solver.solve();
        if (solutions != null)
            System.out.printf("[+] %s: solved with k=%d\n", this, k);
        else
            System.out.printf("[-] %s: no solution with k=%d\n", this, k);

        if (!p.isDumping) {
            deleteFile(beeFileName);
            if (p.isExternal) {
                deleteFile(dimacsFileName);
                deleteFile(mapFileName);
            }
        }

        long time_total = System.currentTimeMillis() - time_start;
        try (FileWriter fw = new FileWriter("subtasks.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.printf("%s,%d,%d,%.3f,%s,%s\n",
                    p.basefilename,
                    clusters.get(0).getTaxaSize(),
                    k,
                    time_total / 1000.,
                    solutions == null ? "UNSAT" : "SAT",
                    p.isExternal ? "external" : "builtin");
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        if (solutions == null)
            return false;

        System.out.printf("[*] %s: building networks...\n", this);
        this.answers = solutions.stream()
                .map(solution -> new Network(this, k, solution))
                .collect(Collectors.toList());
        this.answer = this.answers.get(0);
        System.out.printf("[+] %s: done building %d network(s)\n", this, this.answers.size());

        return true;
    }

    private void normalize() {
        for (Tree cluster : clusters) {
            cluster.addFictitiousRoot();
        }
    }
}
