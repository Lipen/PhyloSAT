import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

class Main {
    @Parameter(names = {"-i", "--trees"}, variableArity = true, required = true,
            description = "Files with trees")
    private List<String> treesPaths = new ArrayList<>();

    @Parameter(names = {"-l", "--log"})
    private String logFilePath;

    @Parameter(names = {"-r", "--result"}, required = true,
            description = "Results base filepath (without extension)")
    private String resultFilePath = "network";

    @Parameter(names = "--help", help = true)
    private boolean help = false;

    @Parameter(names = {"-np", "--noPreprocessing"},
            description = "Disables preprocessing (collapsing/clusterizing)")
    private boolean disablePreprocessing = false;

    @Parameter(names = {"-h", "-hn", "--hybridizationNumber"},
            description = "Hybridization number, available with disabled preprocessing")
    private int hybridizationNumber = -1;

    @Parameter(names = {"-cf", "--checkFirst"},
            description = "Number of first solved tasks")
    private int checkFirst = 3;

    @Parameter(names = {"-ftl", "--firstTimeLimit"},
            description = "Time available to solve first <checkFirst> subtasks")
    private int firstTimeLimit = 10;

    @Parameter(names = {"-tl", "-mtl", "--maxTimeLimit"},
            description = "Maximum time available to solve subtask")
    private int maxTimeLimit = 300;

    @Parameter(names = {"-mc", "-m1", "--maxChildren"},
            description = "maximum common-vertex children")
    private int m1 = 2;

    @Parameter(names = {"-mp", "-m2", "--maxParents"},
            description = "maximum reticular-vertex parents")
    private int m2 = 2;

    private FileHandler loggerHandler;
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

    private void addLoggerHandler(String logFilePath) throws IOException {
        loggerHandler = new FileHandler(logFilePath, false);
        loggerHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(loggerHandler);
        logger.setUseParentHandlers(false);
        System.out.println("[*] Log redirected to <" + logFilePath + ">");
    }

    private void removeAndCloseLoggerHandler() {
        if (loggerHandler != null) {
            logger.removeHandler(loggerHandler);
            loggerHandler.close();
        }
    }

    private void run(JCommander j) {
        Locale.setDefault(Locale.US);

        if (help) {
            j.usage();
            return;
        }

        if (maxTimeLimit > 0)
            maxTimeLimit *= 1000;
        if (firstTimeLimit > 0)
            firstTimeLimit *= 1000;

        if (!disablePreprocessing && hybridizationNumber >= 0) {
            System.out.println("Hybridization number can be set only in -ds mode");
            return;
        }

        if (logFilePath != null) {
            try {
                addLoggerHandler(logFilePath);
            } catch (IOException e) {
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
                logger.warning("[!] Can't load trees from <" + filePath + ">");
                e.printStackTrace();
                return;
            }
        }
        checkTrees(trees);

        Manager manager = new Manager(trees,
                new SolveParameters(hybridizationNumber, m1, m2,
                        firstTimeLimit, maxTimeLimit, checkFirst));
        manager.printTrees(resultFilePath, logger);
        if (!disablePreprocessing)
            manager.preprocess();
        manager.solve();
        manager.cookNetwork();
        manager.printNetwork(resultFilePath, logger);

        logger.info("Finally, there is a network with " + manager.result.getK() + " reticulation nodes");
    }

    private void run_wrapper(JCommander j) {
        try {
            run(j);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            removeAndCloseLoggerHandler();
        }
    }

    public static void main(String... argv) {
        Main main = new Main();
        JCommander j = JCommander.newBuilder()
                .addObject(main)
                .build();
        j.setCaseSensitiveOptions(false);
        j.parse(argv);
        main.run_wrapper(j);
    }
}
