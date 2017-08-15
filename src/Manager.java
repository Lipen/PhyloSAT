import jebl.evolution.trees.SimpleRootedTree;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class Manager {
    private final List<Tree> trees;
    private final SolveParameters solveParameters;
    private final List<Subtask> subtasks = new ArrayList<>(1);
    Network result;


    Manager(List<SimpleRootedTree> trees, SolveParameters solveParameters) {
        this.trees = trees.stream()
                .map(Tree::new)
                .collect(Collectors.toList());
        this.subtasks.add(new ClusterSubtask(this.trees));
        this.solveParameters = solveParameters;
    }


    void preprocess() {
        System.out.println("[*] Preprocessing...");
        long time_start = System.currentTimeMillis();

        boolean clusterized = true;
        boolean collapsed = Tree.collapseAll(trees, subtasks);
        while (clusterized || collapsed) {
            // Note: It is best to make sure that both operations are performed
            clusterized = Tree.clusterize(trees, subtasks);
            collapsed = Tree.collapseAll(trees, subtasks);
        }

        long time_total = System.currentTimeMillis() - time_start;
        System.out.printf("[+] Preprocessing done in %.3fs\n  > Subtasks' sizes (total = %d): %s\n", time_total / 1000., subtasks.size(), subtasks.stream().map(Subtask::getN).collect(Collectors.toList()));
    }

    List<CollapsedSubtask> getCollapsedSubtasks() {
        return subtasks.stream()
                .filter(task -> task instanceof CollapsedSubtask)
                .map(task -> (CollapsedSubtask) task)
                .collect(Collectors.toList());
    }

    List<ClusterSubtask> getClusterSubtasks() {
        return subtasks.stream()
                .filter(task -> task instanceof ClusterSubtask)
                .map(task -> (ClusterSubtask) task)
                .collect(Collectors.toList());
    }

    void solve() {
        solveEx(Executors.newSingleThreadExecutor());
    }

    void solveParallel() {
        solveEx(Executors.newWorkStealingPool());
    }

    private void solveEx(ExecutorService executor) {
        System.out.println("[*] Solving cluster subtasks...");
        for (Subtask subtask : getClusterSubtasks()) {
            executor.submit(() -> {
                System.out.println("[*] " + subtask + ": solving...");
                long time_solve = System.currentTimeMillis();
                subtask.solve(solveParameters);
                time_solve = System.currentTimeMillis() - time_solve;

                if (subtask.answer != null)
                    System.out.printf("[+] %s: solution with k=%d found in %.3fs)\n",
                            subtask, subtask.answer.getK(), time_solve / 1000.);
                else
                    System.out.printf("[-] %s: no solution found in time=%.3fs)\n",
                            subtask, time_solve / 1000.);
            });
        }

        executor.shutdown();
        try {
            System.out.println("[*] Awaiting cluster subtasks...");
            executor.awaitTermination(7, TimeUnit.DAYS);
            System.out.println("[+] All cluster subtasks solved");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[*] Expanding collapsed subtasks...");
        for (Subtask subtask : getCollapsedSubtasks()) {
            subtask.solve(solveParameters);
            if (subtask.answer != null)
                System.out.printf("[+] %s: successfully expanded\n", subtask);
            else
                System.out.printf("[-] %s: couldn't expand\n", subtask);
        }
        System.out.println("[+] All collapsed subtasks expanded");
    }

    void cookNetwork() {
        System.out.println("[*] Cooking final network...");

        result = subtasks.get(0).answer;
        for (int i = subtasks.size() - 1; i > 0; i--)
            result.substituteSubtask(subtasks.get(i));

        System.out.printf("[+] Finally, cooked network with %d reticulation nodes\n", result.getK());
    }

    void printTrees(String resultFilePath) {
        if (resultFilePath == null)
            return;

        for (int i = 0; i < trees.size(); ++i) {
            String treeFilePath = resultFilePath + ".tree" + i + ".gv";

            try (PrintWriter gvPrintWriter = new PrintWriter(treeFilePath)) {
                System.out.println("[*] Printing tree " + i + " to <" + treeFilePath + ">");
                gvPrintWriter.print(trees.get(i).toGVString());
            } catch (FileNotFoundException e) {
                System.err.println("[!] Couldn't open <" + resultFilePath + ">:\n" + e.getMessage());
            }
        }
    }

    void printNetwork(String resultFilePath) {
        if (resultFilePath == null)
            return;
        String networkFilePath = resultFilePath + ".gv";

        for (int i = 0; i < subtasks.size(); i++)
            subtasks.get(i).subprefix = i + "_";

        try (PrintWriter gvPrintWriter = new PrintWriter(networkFilePath)) {
            System.out.println("[*] Printing network to <" + networkFilePath + ">");
            gvPrintWriter.print(result.toGVString());
        } catch (FileNotFoundException e) {
            System.err.println("[!] Couldn't open <" + resultFilePath + ">:\n" + e.getMessage());
        }
    }

    void printNetworks(String resultFilePath) {
        if (resultFilePath == null)
            return;

        for (int i = 0; i < subtasks.size(); i++)
            // subtasks.get(i).subprefix = "";
            subtasks.get(i).subprefix = i + "_";

        for (int i = 0; i < subtasks.size(); i++) {
            Subtask subtask = subtasks.get(i);
            System.out.println("[*] Printing isomorphic networks for subtask " + i + ": " + subtask);
            for (int j = 0; j < subtask.answers.size(); j++) {
                Network isonetwork = subtask.answers.get(j);
                String networkFilePath = resultFilePath + ".sub" + i + ".iso" + j + ".gv";

                try (PrintWriter gvPrintWriter = new PrintWriter(networkFilePath)) {
                    System.out.println("[*] Printing isomorphic network to <" + networkFilePath + ">");
                    gvPrintWriter.print(isonetwork.toGVString());
                } catch (FileNotFoundException e) {
                    System.err.println("[!] Couldn't open <" + resultFilePath + ">:\n" + e.getMessage());
                }
            }
        }
    }
}
