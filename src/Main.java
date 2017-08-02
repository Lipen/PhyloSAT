import jebl.evolution.io.NewickImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

class Main {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Argument(usage = "paths to files with trees", metaVar = "treesPaths", required = true)
    private List<String> treesPaths = new ArrayList<>();

    @Option(name = "--log", aliases = {"-l"}, usage = "write log to this file", metaVar = "<file>")
    private String logFilePath = null;

    @Option(name = "--result", aliases = {
            "-r"}, usage = "write result network in GV format to this file", metaVar = "<GV file>")
    private String resultFilePath = "network";

    @SuppressWarnings("unused")
    @Option(name = "--solverOptions", aliases = {
            "-s"}, usage = "launch with this solver and solver options", metaVar = "<string>")
    private String solverOptions = "cryptominisat --threads=4";

    @Option(name = "--hybridizationNumber", aliases = {
            "-h", "-hn"}, usage = "hybridization number, available in -ds mode", metaVar = "<int>")
    private int hn = -1;

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

    private static void checkTrees(List<SimpleRootedTree> trees) {
        if (trees.size() < 2)
            throw new RuntimeException("There are less then 2 trees");

        Set<Taxon> taxa = new TreeSet<>(trees.get(0).getTaxa());
        int taxaSize = taxa.size();

        for (int t = 1; t < trees.size(); t++) {
            SimpleRootedTree tree = trees.get(t);
            Set<Taxon> treeTaxa = new TreeSet<>(tree.getTaxa());

            if (treeTaxa.size() != taxaSize)
                throw new RuntimeException(String.format("Tree %d has %d taxa, but tree 0 has %d", t, treeTaxa.size(), taxaSize));
            if (!taxa.containsAll(treeTaxa))
                throw new RuntimeException(String.format("Tree %d and tree 0 has different taxa", t));
        }
    }

    public static void main(String[] args) {
        new Main().run(args);
    }

    private FileHandler addLoggerHandler(String logFilePath) throws IOException {
        FileHandler fh = new FileHandler(logFilePath, false);
        fh.setFormatter(new SimpleFormatter());

        logger.addHandler(fh);
        logger.setUseParentHandlers(false);
        System.out.println("Log redirected to " + logFilePath);
        return fh;
    }

    private void launcher(String[] args) throws IOException {
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
            return;
        }

        if (maxTimeLimit > 0)
            maxTimeLimit *= 1000;
        if (firstTimeLimit > 0)
            firstTimeLimit *= 1000;

        if (!disableSplits && hn >= 0) {
            System.out.println("Hybridization number can be set only in -ds mode");
            return;
        }

        if (logFilePath != null) {
            try {
                this.loggerHandler = addLoggerHandler(logFilePath);
            } catch (Exception e) {
                System.err.println("Can't work with log file " + logFilePath + ": " + e.getMessage());
                return;
            }
        }

        List<SimpleRootedTree> trees = new ArrayList<>();
        for (String filePath : treesPaths) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                NewickImporter importer = new NewickImporter(reader, false);
                trees.addAll(importer.importTrees().stream()
                        .map(t -> (SimpleRootedTree) t)
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                logger.warning("Can't load trees from file " + filePath);
                e.printStackTrace();
                return;
            }
        }
        checkTrees(trees);

        Manager manager = new Manager(trees,
                new SolveParameters(hn, m1, m2, firstTimeLimit, maxTimeLimit, checkFirst));

        manager.printTrees(resultFilePath, logger);

        if (!disableSplits)
            manager.preprocess();

        manager.solve();

        Network result = manager.cookNetwork();

        manager.printNetwork(resultFilePath, logger);

        logger.info("Finally, there is a network with " + result.getK() + " reticulation nodes");
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
