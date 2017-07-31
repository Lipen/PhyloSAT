import jebl.evolution.trees.SimpleRootedTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class Manager {
    private final List<Tree> trees;
    private final List<Subtask> subtasks = new ArrayList<>(1);


    Manager(List<SimpleRootedTree> trees) {
        this.trees = trees.stream()
                .map(Tree::new)
                .collect(Collectors.toList());
        this.subtasks.add(new ClusterSubtask(this.trees));
    }


    void preprocess() {
        int collapsedCounter = 0;

        if (Tree.collapse(trees, subtasks))
            collapsedCounter++;

        while (Tree.clusterize(trees, subtasks) && Tree.collapse(trees, subtasks))
            collapsedCounter++;
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

    Network cookNetwork() {
        Network ans = subtasks.get(0).answer;

        for (int i = subtasks.size() - 1; i > 0; i--) {
            ans.substituteSubtask(subtasks.get(i));
        }

        return ans;
    }

    void printTrees(String resultFilePath, Logger logger) {
        for (int i = 0; i < trees.size(); ++i) {
            String treeFilePath = resultFilePath + ".tree" + i + ".gv";
            try (PrintWriter gvPrintWriter = new PrintWriter(new File(treeFilePath))) {
                gvPrintWriter.print(trees.get(i).toGVString());
            } catch (FileNotFoundException e) {
                logger.warning("Couldn't open <" + resultFilePath + ">:\n" + e.getMessage());
            }
        }
    }
}
