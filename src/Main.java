import jebl.evolution.io.NewickImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
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
import java.util.stream.Collectors;

class Main {
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

    @Option(name = "--checkFirst", aliases = {
            "-cf"}, usage = "solve first few subtasks", metaVar = "<int>")
    private int checkFirst = 3;

    @Option(name = "--firstTimeLimit", aliases = {
            "-ftl"}, usage = "time available to solve first few subtasks", metaVar = "<seconds>")
    private int firstTimeLimit = 10;

    @Option(name = "--maxTimeLimit", aliases = {
            "-tl", "-mtl"}, usage = "maximum time available to solve subtask", metaVar = "<seconds>")
    private int maxTimeLimit = 300;

    @Option(name = "--maxChildren", aliases = {
            "-m1", "-mc"}, usage = "maximum common-vertex children", metaVar = "<int>")
    private int m1 = 2;

    @Option(name = "--maxParents", aliases = {
            "-m2", "-mp"}, usage = "maximum reticular-vertex parents", metaVar = "<int>")
    private int m2 = 2;

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
        new Main().run(args);
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

        List<SimpleRootedTree> trees = new ArrayList<>();
        for (String filePath : treesPaths) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                NewickImporter importer = new NewickImporter(reader, false);
                trees.addAll(importer.importTrees().stream()
                        // .map(SimpleRootedTree.class::cast)
                        .map(t -> (SimpleRootedTree) t)
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                logger.warning("Can't load trees from file " + filePath);
                e.printStackTrace();
                return -1;
            }
        }
        checkTrees(trees);

        Manager manager = new Manager(trees);

        if (!disableSplits)
            manager.preprocess();

        for (CollapsedSubtask subtask : manager.getCollapsedSubtasks()) {
            subtask.solve();
        }

        for (ClusterSubtask subtask : manager.getClusterSubtasks()) {
            long[] time = new long[1];
            if (hn >= 0)
                subtask.solveEx(hn, m1, m2, maxTimeLimit, time);
            else
                subtask.solve(m1, m2, firstTimeLimit, maxTimeLimit, checkFirst, time);
        }

        if (resultFilePath != null) {
            manager.printTrees(resultFilePath, logger);
        }

        Network result = manager.cookNetwork();

        if (resultFilePath != null) {
            try {
                PrintWriter gvPrintWriter = new PrintWriter(new File(resultFilePath + ".gv"));
                gvPrintWriter.print(result.toGVString());
                gvPrintWriter.close();
            } catch (FileNotFoundException e) {
                logger.warning("Can not open " + resultFilePath + " :\n" + e.getMessage());
            } catch (Exception e) {
                logger.warning("Caught exception while printing network:\n" + e.getMessage());
            }
        }

        int resultK = result.getK();
        logger.info("Finally, there is a network with " + resultK + " reticulation nodes");
        return resultK;
    }

    private void run(String[] args) {
        try {
            launcher(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (this.loggerHandler != null) {
                this.logger.removeHandler(loggerHandler);
                loggerHandler.close();
            }
        }
    }
}
