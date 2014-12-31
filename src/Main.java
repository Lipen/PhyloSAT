import jebl.evolution.io.ImportException;
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

/**
 * Vladimir Ulyantsev
 */
public class Main {
    @Argument(usage = "paths to files with trees", metaVar = "treesPaths", required = true)
    private List<String> treesPaths = new ArrayList<>();

    @Option(name = "--log", aliases = {"-l"}, usage = "write log to this file", metaVar = "<file>")
    private String logFilePath = null;

    @Option(name = "--result", aliases = {"-r"}, usage = "write result network in GV format to this file",
            metaVar = "<GV file>")
    private String resultFilePath;

    @Option(name = "--cnf", usage = "write CNF formula to this file", metaVar = "<file>")
    private String cnfFilePath = "cnf";

    @Option(name = "--hybridizationNumber", aliases = {"-h"},
            usage = "hybridization number, available in -ds mode", metaVar = "<int>")
    private int hn = -1;

    @Option(name = "--enableReticulationEdges", aliases = {"-e"},
            handler = BooleanOptionHandler.class, usage = "does reticulation-reticulation connection enabled")
    private boolean enableReticulationEdges;

    @Option(name = "--disableComments", aliases = {"-dc"},
            handler = BooleanOptionHandler.class, usage = "disables comments in CNF")
    private boolean disableComments = false;

    @Option(name = "--disableSplits", aliases = {"-ds"},
            handler = BooleanOptionHandler.class,
            usage = "disables splits, so it is possible to set hybridization number")
    private boolean disableSplits = false;

    Logger logger;

    private int launcher(String[] args) throws IOException, ImportException {
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
            System.out.println("Hybridization number can be set only in -dp mode");
            return -1;
        }

        logger = Logger.getLogger("Logger");
        if (logFilePath != null) {
            try {
                FileHandler fh = new FileHandler(logFilePath, false);
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);

                logger.setUseParentHandlers(false);
                System.out.println("Log redirected to " + logFilePath);
            } catch (Exception e) {
                System.err.println("Can't work with log file " + logFilePath + ": " + e.getMessage());
                return -1;
            }
        }

        List<SimpleRootedTree> trees = new ArrayList<>();
        for (String filePath : treesPaths) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(filePath));
                NewickImporter importer = new NewickImporter(reader, false);

                int treesCount = 0;
                for (Tree tree : importer.importTrees()) {
                    treesCount++;
                    trees.add((SimpleRootedTree) tree);
                }
                reader.close();

                logger.info(String.format("Loaded %d trees from %s", treesCount, filePath));
            } catch (Exception e) {
                logger.warning("Can't load trees from file " + filePath);
                e.printStackTrace();
                return -1;
            }
        }
        checkTrees(trees);

        String loggerString = "Input original trees:";
        List<PhylogeneticTree> inputTrees = new ArrayList<>();
        for (SimpleRootedTree srt : trees) {
            PhylogeneticTree inputTree = new PhylogeneticTree(srt);
            inputTrees.add(inputTree);
            loggerString += "\n" + inputTree;
        }
        logger.info(loggerString);

        int finalK = 0;
        for (List<PhylogeneticTree> subtaskTrees : preprocessing(inputTrees)) {
            String loggerStr = "Subtask trees:";
            for (PhylogeneticTree subtaskTree : subtaskTrees) {
                loggerStr += "\n" + subtaskTree;
            }
            logger.info(loggerStr);

            subtaskTrees = normalize(subtaskTrees);
            loggerStr = "Normalized trees:";
            for (PhylogeneticTree subtaskTree : subtaskTrees) {
                loggerStr += "\n" + subtaskTree;
            }
            logger.info(loggerStr);

            int k;
            if (hn >= 0) {
                k = solveSubtask(subtaskTrees, hn, 1000000, new long[1]) ? hn : -1;
            } else {
                k = solveSubtaskWithoutUNSAT(subtaskTrees);
            }

            if (k == -1) {
                logger.info("NO SOLUTION FOR SUBPROBLEM");
                return -1;
            } else {
                finalK += k;
            }
        }

        logger.info("Finally, there is a network with " + finalK + " reticulation nodes");
        return finalK;
    }

    private int solveSubtaskWithoutUNSAT(List<PhylogeneticTree> trees) throws IOException {
        int CHECK_FIRST = 3;
        long FIRST_TIME_LIMIT = 1000;
        long MAX_TL = 1000000;
        long TL_COEF = 50;

        int mink = 0;
        while (mink <= CHECK_FIRST) {
            long[] time = new long[1];
            boolean res = solveSubtask(trees, mink, FIRST_TIME_LIMIT, time);
            if (time[0] == -1) {
                break;
            }
            if (res) {
                return mink;
            }
            mink++;
        }

        int k = calcUpperBound(trees);
        long[] time = new long[1];
        boolean maxRes = solveSubtask(trees, k, MAX_TL, time);
        if (time[0] == -1) {
            logger.info("There is no solution found in MAX_TL time");
            return -1;
        }
        if (!maxRes) {
            logger.info("There is no solution with max bound k = " + k);
            return -1;
        }

        int l = mink, r = k;
        while (l < r) {
            int m = (l + r) / 2;
            boolean res = solveSubtask(trees, m, time[0] * TL_COEF, time);
            if (!res) {
                l = m + 1;
            } else {
                r = m;
            }
        }
        return l;
//        while (k >= mink) {
//            boolean res = solveSubtask(trees, k, time[0] * TL_COEF, time);
//            if (!res) {
//                return k + 1;
//            }
//            k--;
//        }
//
//        return k + 1;
    }

    private int calcUpperBound(List<PhylogeneticTree> trees) {
        //return Math.min(10, trees.get(0).getTaxaSize());
        return trees.get(0).getTaxaSize();
    }

//    private int solveSubtask(List<PhylogeneticTree> trees, int mink, int maxk) throws IOException {
//        long TIME_LIMIT = 1000000;
//        for (int k = mink; k <= maxk; k++) {
//            if (solveSubtask(trees, k, TIME_LIMIT, new long[1])) {
//                return k;
//            }
//        }
//        return -1;
//    }

    private boolean solveSubtask(List<PhylogeneticTree> trees, int k,
                                 long timeLimit, long[] time) throws IOException {
        Map<String, Integer> m = new TreeMap<>();
        logger.info("Trying to solve problem with k = " + k + " reticulation nodes");
        FormulaBuilder builder = new FormulaBuilder(trees, k, m, enableReticulationEdges, disableComments);
        String cnf = builder.buildCNF();
        String help = builder.getHelpMap();
        logger.info("CNF formula length is " + cnf.length() + " characters");

        try {
            PrintWriter cnfPrintWriter = new PrintWriter(new File(cnfFilePath));
            cnfPrintWriter.print(cnf);
            cnfPrintWriter.close();
            logger.info("CNF file written to " + cnfFilePath);
        } catch (FileNotFoundException e) {
            logger.warning("File " + cnfFilePath + " not found: " + e.getMessage());
        }

        try {
            PrintWriter cnfPrintWriter = new PrintWriter(new File("help"));
            cnfPrintWriter.print(help);
            cnfPrintWriter.close();
            logger.info("help file written to " + cnfFilePath);
        } catch (FileNotFoundException e) {
            logger.warning("File " + cnfFilePath + " not found: " + e.getMessage());
        }

        boolean[] solution = CryptominisatPort.solve(cnf, null, null, timeLimit, time);

        if(solution != null) {
            StringBuilder hlpbld = new StringBuilder();
            for (int i = 0; i < solution.length; ++i)
                if (solution[i])
                    hlpbld.append(i + 1).append(" ");
            logger.info(hlpbld.toString());
        }

        if (time[0] == -1) {
            logger.info("TIME LIMIT EXCEEDED (" + timeLimit + ")");
            return false;
        }
        logger.info("Execution time : " + time[0] + " / " + timeLimit);
        if (solution == null) {
            logger.info("NO SOLUTION with k = " + k);
        } else {
            logger.info("SOLUTION FOUND with k = " + k);
            if (resultFilePath != null) {
                try {
                    // Сейчас это имеет мало смысла
                    PrintWriter gvPrintWriter = new PrintWriter(new File(resultFilePath));
                    gvPrintWriter.print(NetworkBuilder.gvNetwork(m, solution, trees, k));
                    gvPrintWriter.close();
                } catch (FileNotFoundException e) {
                    logger.warning("File " + resultFilePath + " not found: " + e.getMessage());
                }
            }
            return true;
        }

        return false;
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

        if(label == null) {
            normalized = false;
        } else {
            for (PhylogeneticTree tree : inputTrees) {
                if (!isNormalized(tree, label)) {
                    normalized = false;
                    break;
                }
            }
        }
        if(normalized) {
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
        if(tree.size() < 3)
            return false;

        List<Integer> children = tree.getChildren(tree.size() - 1);

        return (tree.isLeaf(children.get(0)) && tree.getLabel(children.get(0)).equals(childLabel))
            || (tree.isLeaf(children.get(1)) && tree.getLabel(children.get(1)).equals(childLabel));
        }

    private List<List<PhylogeneticTree>> preprocessing(List<PhylogeneticTree> inputTrees) {
        List<List<PhylogeneticTree>> ans = new ArrayList<>();

        List<PhylogeneticTree> currentTrees = collapseAll(inputTrees);
        if (!disableSplits) {
            while (true) {
                List<PhylogeneticTree> newTask = equalsTaxaSplit(currentTrees);
                if (newTask == null) {
                    break;
                }
                ans.add(newTask);
                currentTrees = collapseAll(currentTrees);
            }
        }
        ans.add(currentTrees);

        return ans;
    }

    private List<PhylogeneticTree> collapseAll(List<PhylogeneticTree> inputTrees) {
        ArrayList<PhylogeneticTree> ans = new ArrayList<>();
        for (PhylogeneticTree inputTree : inputTrees) {
            ans.add(new PhylogeneticTree(inputTree));
        }

        int collapsedCount = 0;
        while (collapseEqualsSubtrees(ans)) {
            collapsedCount++;
        }
        if (collapsedCount > 0) {
            logger.info(collapsedCount + " subtrees collapsed in each phylogenetic tree");
            logger.info("There were " + inputTrees.get(0).getTaxaSize() + " taxons, now it is " + ans.get(0).getTaxaSize());
        }
        return ans;
    }

    private boolean collapseEqualsSubtrees(List<PhylogeneticTree> trees) {
        PhylogeneticTree firstTree = trees.get(0);
        // Почему не проверять на корень - непонятно
        // Ведь круто же сколлапсить сразу все деревья если они равны
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

                for (int treeNum = 0; treeNum < trees.size(); treeNum++) {
                    PhylogeneticTree tree = trees.get(treeNum);
                    int collapsedNodeNum = equalsNodesNumbers.get(treeNum);
                    trees.set(treeNum, tree.compressedTree(collapsedNodeNum, label));
                }
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

    public int run(String[] args) {
        try {
            return launcher(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return -1;
    }

    public static void main(String[] args) {
        new Main().run(args);
    }

}
