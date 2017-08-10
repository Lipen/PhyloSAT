import beepp.BEEppCompiler;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

/**
 * Moklev Vyacheslav
 */
public class BEEMain {
    @Argument(usage = "paths to files with trees", metaVar = "treesPaths", required = true)
    private List<String> treesPaths = new ArrayList<>();

    @Option(name = "--log", aliases = {"-l"}, usage = "write log to this file", metaVar = "<file>")
    private String logFilePath = null;

    @Option(name = "--result", aliases = {
            "-r"}, usage = "write result network in GV format to this file", metaVar = "<GV file>")
    private String resultFilePath = "network";

    @Option(name = "--cnf", usage = "write CNF formula to this file", metaVar = "<file>")
    private String cnfFilePath = "cnf";

    @Option(name = "--solverOptions", aliases = {
            "-s"}, usage = "launch with this solver and solver options", metaVar = "<string>")
    private String solverOptions = "cryptominisat --threads=4";

    @Option(name = "--hybridizationNumber", aliases = {
            "-h", "-hn"}, usage = "hybridization number, available in -ds mode", metaVar = "<int>")
    private int hn = -1;

    @Option(name = "--enableReticulationEdges", aliases = {
            "-e"}, handler = BooleanOptionHandler.class, usage = "does reticulation-reticulation connection enabled")
    private boolean enableReticulationEdges = false;

    @Option(name = "--disableComments", aliases = {
            "-dc"}, handler = BooleanOptionHandler.class, usage = "disables comments in CNF")
    private boolean disableComments = false;

    @Option(name = "--disableSplits", aliases = {
            "-ds"}, handler = BooleanOptionHandler.class, usage = "disables splits, so it is possible to set hybridization number")
    private boolean disableSplits = false;

    @Option(name = "--firstTimeLimit", aliases = {
            "-ftl"}, usage = "time available to solve first few subtasks", metaVar = "<seconds>")
    private int firstTimeLimit = 30;

    @Option(name = "--maxTimeLimit", aliases = {
            "-tl", "-mtl"}, usage = "maximum time available to solve subtask", metaVar = "<seconds>")
    private int maxTimeLimit = 300;

    private FileHandler loggerHandler = null;

    private Logger logger = Logger.getLogger("Logger");

    private static String path(String filepath) throws IOException {
        return new File(filepath).getCanonicalPath();
    }

    private static void checkTrees(List<SimpleRootedTree> trees) {
        if (trees.size() < 2) {
            throw new RuntimeException("There are less then 2 trees");
        }

        Set<Taxon> taxa = new TreeSet<>(trees.get(0).getTaxa());
        int taxaSize = taxa.size();
        for (int t = 1; t < trees.size(); t++) {
            SimpleRootedTree tree = trees.get(t);
            Set<Taxon> treeTaxa = new TreeSet<>(tree.getTaxa());
            if (treeTaxa.size() != taxaSize) {
                String msg = String.format("Tree %d has %d taxa, but tree 0 has %d", t, treeTaxa.size(), taxaSize);
                throw new RuntimeException(msg);
            }
            if (!taxa.containsAll(treeTaxa)) {
                String msg = String.format("Tree %d and tree 0 has different taxa", t);
                throw new RuntimeException(msg);
            }
        }
    }

    public static void main(String[] args) {
        new BEEMain().run(args);
    }

    private FileHandler addLoggerHandler(String logFilePath) throws IOException {
        FileHandler fh = new FileHandler(logFilePath, false);
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        logger.setUseParentHandlers(false);
        System.out.println("Log redirected to " + logFilePath);
        return fh;
    }

    public void removeLoggerHandler(FileHandler fh) {
        logger.removeHandler(fh);
    }

    private int launcher(String[] args) throws IOException {
        Locale.setDefault(Locale.US);

        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.out.println("Constructing parsimonious hybridization network with multiple phylogenetic trees\n");
            System.out.println("Author: Vladimir Ulyantsev (ulyantsev@rain.ifmo.ru)\n");
            System.out.print("Usage: ");
            parser.printSingleLineUsage(System.out);
            System.out.println();
            parser.printUsage(System.out);
            return -1;
        }

        if (!disableSplits && hn >= 0) {
            System.out.println("Hybridization number can be set only in -ds mode");
            return -1;
        }

        if (logFilePath != null) {
            try {
                this.loggerHandler = addLoggerHandler(logFilePath);
            } catch (Exception e) {
                System.err.println("Can't work with log file " + logFilePath + ": " + e.getMessage());
                return -1;
            }
        }

        if (firstTimeLimit > 0)
            firstTimeLimit *= 1000;
        if (maxTimeLimit > 0)
            maxTimeLimit *= 1000;

        List<SimpleRootedTree> trees = new ArrayList<>();
        for (String filePath : treesPaths) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(filePath));
                NewickImporter importer = new NewickImporter(reader, false);

                for (Tree tree : importer.importTrees()) {
                    trees.add((SimpleRootedTree) tree);
                }
                reader.close();

            } catch (Exception e) {
                logger.warning("Can't load trees from file " + filePath);
                e.printStackTrace();
                return -1;
            }
        }
        checkTrees(trees);

        List<PhylogeneticTree> inputTrees = new ArrayList<>();
        for (SimpleRootedTree srt : trees) {
            PhylogeneticTree inputTree = new PhylogeneticTree(srt);
            inputTrees.add(inputTree);
        }

        long time_start = System.currentTimeMillis();

        int finalK = 0;
        List<PhylogeneticNetwork> res = new ArrayList<>();
        for (List<PhylogeneticTree> subtaskTrees : preprocessing(inputTrees)) {
            subtaskTrees = normalize(subtaskTrees);
            String loggerStr = "Normalized trees:";
            for (PhylogeneticTree subtaskTree : subtaskTrees) {
                loggerStr += "\n" + subtaskTree;
            }
            logger.info(loggerStr);

            PhylogeneticNetwork cur;
            if (hn >= 0) {
                cur = solveSubtask(subtaskTrees, hn, 1_000_000, new long[1]);
            } else {
                cur = solveSubtaskWithoutUNSAT(subtaskTrees);
            }

            if (cur == null) {
                logger.info("NO SOLUTION FOR SUBPROBLEM");
                return -1;
            } else {
                finalK += cur.getK();
                res.add(cur);
            }
        }

        while (res.size() > 1) {
            outer:
            for (int i = 0; i < res.size(); ++i) {
                for (int j = i + 1; j < res.size(); ++j) {
                    PhylogeneticNetwork firstSubtask = res.get(i);
                    PhylogeneticNetwork secondSubtask = res.get(j);
                    Set<String> firstTaxaSet = firstSubtask.getTaxaSet();
                    Set<String> secondTaxaSet = secondSubtask.getTaxaSet();
                    if (firstTaxaSet.containsAll(secondTaxaSet)) {
                        if (firstSubtask.substituteSubtask(secondSubtask)) {
                            res.remove(j);
                            break outer;
                        }
                    } else if (secondTaxaSet.containsAll(firstTaxaSet)) {
                        if (secondSubtask.substituteSubtask(firstSubtask)) {
                            res.remove(i);
                            break outer;
                        }
                    }
                }
            }
        }

        PhylogeneticNetwork resultNetwork = res.get(0);
        long time_total = System.currentTimeMillis() - time_start;
        System.out.printf("[+] Finally, network with k=%d was found in %.3fs\n", finalK, time_total / 1000.);
        try (FileWriter fw = new FileWriter("oldthing.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.printf("%s,%d,%d,%.3f\n",
                    new File(treesPaths.get(0)).getName().split("\\.")[0],
                    inputTrees.get(0).getTaxaSize(),
                    finalK,
                    time_total / 1000.);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        if (resultFilePath != null) {
            try {
                PrintWriter gvPrintWriter = new PrintWriter(new File(resultFilePath + ".gv"));
                gvPrintWriter.print(resultNetwork.toGVString());
                gvPrintWriter.close();
            } catch (FileNotFoundException e) {
                logger.warning("Can not open " + resultFilePath + " :\n" + e.getMessage());
            }
            for (int i = 0; i < inputTrees.size(); ++i) {
                try {
                    String treeFilePath = resultFilePath + ".tree" + i + ".gv";
                    PrintWriter gvPrintWriter = new PrintWriter(new File(treeFilePath));
                    gvPrintWriter.print(inputTrees.get(i).toGVString());
                    gvPrintWriter.close();
                } catch (FileNotFoundException e) {
                    logger.warning("Can not open " + resultFilePath + " :\n" + e.getMessage());
                }
            }
        }

        logger.info("Finally, there is a network with " + finalK + " reticulation nodes");
        return finalK;
    }

    private PhylogeneticNetwork solveSubtaskWithoutUNSAT(List<PhylogeneticTree> trees) throws IOException {
        int CHECK_FIRST = 3;  // Pre-check k = 0..CHECK_FIRST
        // long FIRST_TIME_LIMIT = 30 * 1000;  // milliseconds, 30sec
        // long MAX_TL = 60 * 1000;  // 1min

        int mink = 0;
        while (mink <= CHECK_FIRST) {
            long[] time = new long[1];
            PhylogeneticNetwork res = solveSubtask(trees, mink, firstTimeLimit, time);
            if (time[0] == -1) {
                break;
            }
            if (res != null) {
                return res;
            }
            mink++;
        }

        long[] time = new long[1];
        int k = calcUpperBound(trees) - 1;
        // First timeout is infinite, because you NEED any solution, otherwise everything will blow up!
        PhylogeneticNetwork last = solveSubtask(trees, k--, INFINITE_TIMEOUT, time);

        // TODO: heuristics -- descending from upper bound with step 2 (or sqrt(k - 3))
        // When found UNSAT case, roll back to the last known SAT-1 and continue with step 1 (or max(last_step/2, 1))

        while (k >= mink) {
            PhylogeneticNetwork temp = solveSubtask(trees, k, maxTimeLimit, time);
            if (temp == null) {
                break;
            }
            last = temp;
            k--;
        }

        return last;
    }

    private int calcUpperBound(List<PhylogeneticTree> trees) {
        return trees.get(0).getTaxaSize();
    }

    private PhylogeneticNetwork solveSubtask(List<PhylogeneticTree> trees, int k, long timeLimit, long[] time) throws IOException {
        long time_start = System.currentTimeMillis();

        logger.info("Building BEE++ formula...");
        String BEEFormula = new BEEFormulaBuilder(trees, k, false).build();

        logger.info("Compiling BEE++ to BEE...");
        BEEppCompiler.fastCompile(BEEFormula, new FileOutputStream("out.bee"));

        int n = trees.get(0).getTaxaSize();
        logger.info("Solving problem of size " + n + " with " + k + " reticulation nodes with BumbleBEE...");
        Map<String, Object> map = BEERunner.resolve(path("out.bee"), timeLimit, time);

        if (time[0] == -1) {
            logger.info("TIME LIMIT EXCEEDED (" + timeLimit + ")");
        } else {
            logger.info("Solver execution time: " + time[0] + " / " + timeLimit);
        }

        // === LOG EXECUTION TIME ===
        try (FileWriter fw = new FileWriter("solver_execution_time.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(n + "," + k + "," + time[0] + "," + (map == null ? "UNSAT" : "SAT"));
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
        // === LOG EXECUTION TIME ===

        double time_total = (System.currentTimeMillis() - time_start) / 1000.;
        System.out.printf("[.] Subtask with n=%d, k=%d took %.3fs\n", n, k, time_total);
        try (FileWriter fw = new FileWriter("oldtasks.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.printf("%d,%d,%.3f,%s\n", n, k, time_total, map == null ? "UNSAT" : "SAT");
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        if (map == null) {
            logger.info("NO SOLUTION with k = " + k);
            return null;
        }

        logger.info("SOLUTION FOUND with k = " + k);
        return BEENetworkBuilder.buildNetwork(map, trees, k);
    }

    private List<PhylogeneticTree> normalize(List<PhylogeneticTree> inputTrees) {
        boolean normalized = true;
        PhylogeneticTree firstTree = inputTrees.get(0);
        List<Integer> children = firstTree.getChildren(firstTree.size() - 1);
        String label = null;
        if (firstTree.isLeaf(children.get(0))) {
            label = firstTree.getLabel(children.get(0));
        } else if (firstTree.isLeaf(children.get(1))) {
            label = firstTree.getLabel(children.get(1));
        }

        if (label == null) {
            normalized = false;
        } else {
            for (PhylogeneticTree tree : inputTrees) {
                if (!isNormalized(tree, label)) {
                    normalized = false;
                    break;
                }
            }
        }
        if (normalized) {
            return inputTrees;
        } else {
            List<PhylogeneticTree> ans = new ArrayList<>();
            for (PhylogeneticTree tree : inputTrees) {
                tree.addFictitiousRoot();
                ans.add(tree);
            }
            return ans;
        }
    }

    private boolean isNormalized(PhylogeneticTree tree, String childLabel) {
        if (tree.size() < 3)
            return false;

        List<Integer> children = tree.getChildren(tree.size() - 1);

        return (tree.isLeaf(children.get(0)) && tree.getLabel(children.get(0)).equals(childLabel))
                || (tree.isLeaf(children.get(1)) && tree.getLabel(children.get(1)).equals(childLabel));
    }

    private List<List<PhylogeneticTree>> preprocessing(List<PhylogeneticTree> inputTrees) {
        List<List<PhylogeneticTree>> ans = new ArrayList<>();

        if (!disableSplits) {
            List<PhylogeneticTree> currentTrees = collapseAll(inputTrees, ans);
            while (true) {
                List<PhylogeneticTree> newTask = equalsTaxaSplit(currentTrees);
                if (newTask == null) {
                    break;
                }
                ans.add(newTask);
                currentTrees = collapseAll(currentTrees, ans);
            }
            ans.add(currentTrees);
        } else {
            ans.add(inputTrees);
        }

        return ans;
    }

    private List<PhylogeneticTree> collapseAll(List<PhylogeneticTree> inputTrees, List<List<PhylogeneticTree>> splitTrees) {
        ArrayList<PhylogeneticTree> ans = new ArrayList<>();
        for (PhylogeneticTree inputTree : inputTrees) {
            ans.add(new PhylogeneticTree(inputTree));
        }

        int collapsedCount = 0;
        while (collapseEqualsSubtrees(ans, splitTrees)) {
            collapsedCount++;
        }
        if (collapsedCount > 0) {
            logger.info(collapsedCount + " subtrees collapsed in each phylogenetic tree");
            logger.info("There were " + inputTrees.get(0).getTaxaSize() + " taxons, now it is " + ans.get(0).getTaxaSize());
        }

        return ans;
    }

    private boolean collapseEqualsSubtrees(List<PhylogeneticTree> trees, List<List<PhylogeneticTree>> splitTrees) {
        PhylogeneticTree firstTree = trees.get(0);
        // Почему не проверять на корень -
        // непонятно
        // Ведь круто же сколлапсить сразу все
        // деревья если они равны
        for (int nodeNum = firstTree.size() - 2; nodeNum >= firstTree.getTaxaSize(); nodeNum--) {
            List<Integer> equalsNodesNumbers = new ArrayList<>();

            for (PhylogeneticTree tree : trees) {
                boolean hasEqualsSubtrees = false;
                for (int otherNodeNum = tree.size() - 1; otherNodeNum >= tree.getTaxaSize(); otherNodeNum--) {
                    if (PhylogeneticTree.isSubtreesEquals(firstTree, nodeNum, tree, otherNodeNum)) {
                        equalsNodesNumbers.add(otherNodeNum);
                        hasEqualsSubtrees = true;
                        break;
                    }
                }
                if (!hasEqualsSubtrees) {
                    break;
                }
            }
            if (equalsNodesNumbers.size() == trees.size()) {
                List<Integer> taxa = firstTree.getTaxa(nodeNum);
                String label = "";
                for (int leafNumber : taxa) {
                    if (!label.isEmpty()) {
                        label += "+";
                    }
                    label += firstTree.getLabel(leafNumber);
                }
                List<PhylogeneticTree> currentEqual = new ArrayList<>();

                for (int treeNum = 0; treeNum < trees.size(); treeNum++) {
                    PhylogeneticTree tree = trees.get(treeNum);
                    int collapsedNodeNum = equalsNodesNumbers.get(treeNum);
                    currentEqual.add(tree.buildSubtree(collapsedNodeNum));
                    trees.set(treeNum, tree.compressedTree(collapsedNodeNum, label));
                }
                splitTrees.add(currentEqual);
                return true;
            }
        }

        return false;
    }

    private List<PhylogeneticTree> equalsTaxaSplit(List<PhylogeneticTree> trees) {
        PhylogeneticTree firstTree = trees.get(0);
        for (int nodeNum = firstTree.getTaxaSize(); nodeNum < firstTree.size() - 1; nodeNum++) {
            List<Integer> equalsNodesNumbers = new ArrayList<>();

            for (PhylogeneticTree tree : trees) {
                boolean hasEqualsTaxa = false;
                for (int otherNodeNum = tree.getTaxaSize(); otherNodeNum < tree.size() - 1; otherNodeNum++) {
                    if (PhylogeneticTree.isTaxaEquals(firstTree, nodeNum, tree, otherNodeNum)) {
                        equalsNodesNumbers.add(otherNodeNum);
                        hasEqualsTaxa = true;
                        break;
                    }
                }
                if (!hasEqualsTaxa) {
                    break;
                }
            }
            if (equalsNodesNumbers.size() == trees.size()) {
                List<Integer> taxa = firstTree.getTaxa(nodeNum);
                String label = "";
                for (int leafNumber : taxa) {
                    if (!label.isEmpty()) {
                        label += "+";
                    }
                    label += firstTree.getLabel(leafNumber);
                }

                List<PhylogeneticTree> ans = new ArrayList<>();
                for (int treeNum = 0; treeNum < trees.size(); treeNum++) {
                    PhylogeneticTree tree = trees.get(treeNum);
                    int collapsedNodeNum = equalsNodesNumbers.get(treeNum);
                    trees.set(treeNum, tree.compressedTree(collapsedNodeNum, label));
                    ans.add(tree.buildSubtree(collapsedNodeNum));
                }
                return ans;
            }
        }
        return null;
    }

    public int run(String[] args) {
        try {
            return launcher(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (this.loggerHandler != null) {
                this.logger.removeHandler(loggerHandler);
                loggerHandler.close();
            }
        }
        return -1;
    }

}
