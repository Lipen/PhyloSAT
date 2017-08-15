import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("FieldCanBeLocal")
class Main {
    @Parameter(names = {"-i", "--trees"}, variableArity = true, required = true, order = 0,
            description = "Files with trees")
    private List<String> treesPaths = new ArrayList<>();

    @Parameter(names = {"-r", "--result"}, required = true, order = 1,
            description = "Results base filepath (without extension)")
    private String resultFilePath = "network";

    @Parameter(names = "--help", help = true, hidden = true)
    private boolean help = false;

    @Parameter(names = {"-np", "-dp", "--noPreprocessing"}, order = 2,
            description = "Disables preprocessing (collapsing/clusterizing)")
    private boolean disablePreprocessing = false;

    @Parameter(names = {"-h", "-hn", "--hybridizationNumber"}, order = 3,
            description = "Hybridization number, available with disabled preprocessing")
    private int hybridizationNumber = -1;

    @Parameter(names = {"-cf", "--checkFirst"},
            description = "Number of first solved tasks")
    private int checkFirst = 3;

    @Parameter(names = {"-ftl", "--firstTimeLimit"}, hidden = true,
            description = "Time available to solve first <checkFirst> subtasks")
    private int firstTimeLimit = 10;

    @Parameter(names = {"-tl", "-mtl", "--maxTimeLimit"}, hidden = true,
            description = "Maximum time available to solve subtask")
    private int maxTimeLimit = 300;

    @Parameter(names = {"-mc", "-m1", "--maxChildren"}, hidden = true,
            description = "Maximum number of common vertex children")
    private int m1 = 2;

    @Parameter(names = {"-mp", "-m2", "--maxParents"}, hidden = true,
            description = "Maximum number of reticulate node parents")
    private int m2 = 2;

    @Parameter(names = "--prefix",
            description = "Filenames prefix (path=prefix+uuid+out.{beepp,bee,dimacs,map})")
    private String prefix = "";

    @Parameter(names = {"-e", "--external"},
            description = "Use external solver instead of BumbleBEE's built-in")
    private boolean isExternal = false;

    @Parameter(names = {"-t", "--threads"},
            description = "Number of threads for cryptominisat")
    private int threads = 4;

    @Parameter(names = {"-p", "--parallel"}, hidden = true,
            description = "Solve subtasks in parallel")
    private boolean isParallel = false;

    @Parameter(names = {"-d", "--dump"},
            description = "Keep temporary files")
    private boolean isDumping = false;

    @Parameter(names = {"-nos", "--numberOfSolutions"},
            description = "Number of different networks")
    private int numberOfSolutions = 1;

    private static void checkTrees(List<SimpleRootedTree> trees) {
        if (trees.size() < 2)
            throw new IllegalArgumentException("There are less then 2 trees");

        Set<Taxon> taxa = new TreeSet<>(trees.get(0).getTaxa());
        int taxaSize = taxa.size();

        for (int t = 1; t < trees.size(); t++) {
            SimpleRootedTree tree = trees.get(t);
            Set<Taxon> treeTaxa = new HashSet<>(tree.getTaxa());

            if (treeTaxa.size() != taxaSize)
                throw new IllegalArgumentException(String.format("Tree %d has %d taxa, but tree 0 has %d", t, treeTaxa.size(), taxaSize));
            if (!taxa.containsAll(treeTaxa))
                throw new IllegalArgumentException(String.format("Tree %d and tree 0 has different taxa", t));
        }

        System.out.println("[+] All trees has n=" + taxaSize + " taxa");
    }

    public static void main(String... argv) {
        Main main = new Main();
        JCommander j = JCommander.newBuilder()
                .addObject(main)
                .build();
        j.setCaseSensitiveOptions(false);
        j.parse(argv);
        main.run(j);
    }

    private List<SimpleRootedTree> readTrees() {
        List<SimpleRootedTree> trees = new ArrayList<>();
        for (String filePath : treesPaths) {
            System.out.println("[*] Loading trees from <" + filePath + ">...");
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                NewickImporter importer = new NewickImporter(reader, false);
                trees.addAll(importer.importTrees().stream()
                        .map(t -> (SimpleRootedTree) t)
                        .collect(Collectors.toList()));
            } catch (ImportException e) {
                System.err.println("[!] Can't load trees from <" + filePath + ">");
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                System.err.println("[!] So sad: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.out.println("[+] Total trees loaded: " + trees.size());
        return trees;
    }

    private void run(JCommander j) {
        Locale.setDefault(Locale.US);

        if (help) {
            System.out.println("Constructs parsimonious hybridization network from multiple non-binary* phylogenetic trees");
            System.out.println("  * non-binary = not necessary binary");
            System.out.println("  * non-binary trees are not supported yet");
            System.out.println("\nAuthors:\n  > Vladimir Ulyantsev (ulyantsev@rain.ifmo.ru)\n  > Vyacheslav Moklev\n  > Konstantin Chukharev (lipen00@gmail.com)\n");
            j.usage();
            return;
        }

        if (!disablePreprocessing && hybridizationNumber >= 0) {
            System.err.println("[!] Hybridization number can be set only in -np mode");
            return;
        }

        if (maxTimeLimit > 0)
            maxTimeLimit *= 1000;
        if (firstTimeLimit > 0)
            firstTimeLimit *= 1000;

        List<SimpleRootedTree> trees = readTrees();
        checkTrees(trees);

        long time_start = System.currentTimeMillis();
        Manager manager = new Manager(trees,
                new SolveParameters(hybridizationNumber, m1, m2, firstTimeLimit,
                        maxTimeLimit, checkFirst, prefix, isExternal, threads, isDumping,
                        new File(treesPaths.get(0)).getName().split("\\.")[0],
                        numberOfSolutions));
        manager.printTrees(resultFilePath);

        if (!disablePreprocessing)
            manager.preprocess();
        else
            System.out.println("[*] Not preprocessing");

        if (isParallel)
            manager.solveParallel();
        else
            manager.solve();
        manager.printNetworks(resultFilePath);
        manager.cookNetwork();
        manager.printNetwork(resultFilePath);

        long time_total = System.currentTimeMillis() - time_start;
        System.out.printf("[*] Total execution time: %.3fs\n", time_total / 1000.);
        try (FileWriter fw = new FileWriter("everything.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.printf("%s,%d,%d,%.3f,%s\n",
                    new File(treesPaths.get(0)).getName().split("\\.")[0],
                    trees.get(0).getTaxa().size(),
                    manager.result.getK(),
                    time_total / 1000.,
                    isExternal ? "external" : "builtin");
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }
}
