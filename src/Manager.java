import jebl.evolution.trees.SimpleRootedTree;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
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

        Tree.collapse(trees, subtasks);
        while (Tree.clusterize(trees, subtasks) && Tree.collapse(trees, subtasks)) {
        }

        System.out.println("[+] Total number of subtasks: " + subtasks.size());
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
        for (ClusterSubtask subtask : getClusterSubtasks()) {
            System.out.println("[*] Subtask: solving...");
            long time_start = System.currentTimeMillis();
            subtask.solve(solveParameters);
            long time_total = System.currentTimeMillis() - time_start;
            if (subtask.answer != null)
                System.out.println(String.format("[+] Subtask: OK (n=%d, k=%d, time=%.3fs)", subtask.getN(), subtask.answer.getK(), time_total / 1000.));
            else
                System.out.println(String.format("[-] Subtask: no solution (n=%d, time=%.3fs)", subtask.getN(), time_total / 1000.));
        }

        for (CollapsedSubtask subtask : getCollapsedSubtasks())
            subtask.solve(solveParameters);
    }

    void solveParallel() {
        ExecutorService executor = Executors.newWorkStealingPool();

        for (Subtask subtask : subtasks)
            executor.submit(() -> subtask.solve(solveParameters));

        executor.shutdown();
        try {
            executor.awaitTermination(7, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void cookNetwork() {
        result = subtasks.get(0).answer;
        for (int i = subtasks.size() - 1; i > 0; i--)
            result.substituteSubtask(subtasks.get(i));
    }

    void printTrees(String resultFilePath, Logger logger) {
        if (resultFilePath == null)
            return;

        for (int i = 0; i < trees.size(); ++i) {
            String treeFilePath = resultFilePath + ".tree" + i + ".gv";

            try (PrintWriter gvPrintWriter = new PrintWriter(treeFilePath)) {
                logger.info(String.format("Printing tree %d to <%s>", i, treeFilePath));
                gvPrintWriter.print(trees.get(i).toGVString());
            } catch (FileNotFoundException e) {
                logger.warning("Couldn't open <" + resultFilePath + ">:\n" + e.getMessage());
            }
        }
    }

    void printNetwork(String resultFilePath, Logger logger) {
        if (resultFilePath == null)
            return;
        String networkFilePath = resultFilePath + ".gv";

        try (PrintWriter gvPrintWriter = new PrintWriter(networkFilePath)) {
            logger.info(String.format("Printing network to <%s>", networkFilePath));
            gvPrintWriter.print(result.toGVString());
        } catch (FileNotFoundException e) {
            logger.warning("Couldn't open <" + resultFilePath + ">:\n" + e.getMessage());
        }
    }
}
