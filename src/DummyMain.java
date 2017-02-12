import beepp.BEEppCompiler;
import beepp.expression.BooleanExpression;
import beepp.expression.IntegerConstant;
import beepp.util.Pair;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Vyacheslav Moklev
 */
public class DummyMain {
    public static void main(String[] args) throws IOException {
        if (true) {
            BEEppCompiler.fastCompile(new FileInputStream("test"), new FileOutputStream("bee"));
            return;
        }
        
        List<SimpleRootedTree> trees = new ArrayList<>();
        String filePath = "data\\clusters_neq_trees.txt";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            NewickImporter importer = new NewickImporter(reader, false);
//            trees.addAll(importer.importTrees().stream().map(tree -> (SimpleRootedTree) tree).collect(Collectors.toList()));
            for (Tree tree : importer.importTrees()) {
                trees.add((SimpleRootedTree) tree);
            }
            reader.close();
            checkTrees(trees);
        } catch (Exception ignored) {}

        List<PhylogeneticTree> inputTrees = new ArrayList<>();
        for (SimpleRootedTree srt : trees) {
            PhylogeneticTree inputTree = new PhylogeneticTree(srt);
            inputTrees.add(inputTree);
        }

        for (PhylogeneticTree tree: inputTrees) {
//            tree.addFictitiousRoot();
            System.out.println(tree);
            for (int i = 0; i < tree.size(); i++) {
                System.out.println("    " + i + " [<- " + tree.getParent(i) + "] -> " + tree.getChildren(i));
            }
        }

        System.out.println("---");
        
        List<List<PhylogeneticTree>> kek = preprocessing(inputTrees);
        System.out.println("kek.size = " + kek.size());
        inputTrees = normalize(kek.get(0));

        for (PhylogeneticTree tree: inputTrees) {
//            tree.addFictitiousRoot();
            System.out.println(tree);
            for (int i = 0; i < tree.size(); i++) {
                System.out.println("    " + i + " [<- " + tree.getParent(i) + "] -> " + tree.getChildren(i));
            }
        }

        int k = 2;
        
        System.err.println("Making BEE++ source...");
        PrintWriter pw = new PrintWriter(new FileWriter("out.keksik"), true);
        pw.print(new BEEFormulaBuilder(inputTrees, k, false).build());
        pw.close();

        System.err.println("Compiling BEE++ to BEE...");
        Pair<List<BooleanExpression>, List<String>> constraints = 
                BEEppCompiler.compile(new FileInputStream("out.keksik"), new FileOutputStream("out.bee"));
        
//        if (true) {
//            Map<String, Object> vars = new HashMap<>();
//            BufferedReader br = new BufferedReader(new FileReader("sat.solution")); 
//            br.lines().forEach(s -> {
//                String[] parts = s.split("=");
//                try {
//                    vars.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
//                } catch (NumberFormatException e) {
//                    vars.put(parts[0].trim(), Boolean.parseBoolean(parts[1].trim()));
//                }
//            });
//            for (int i = 0; i < constraints.a.size(); i++) {
//                System.out.println(constraints.b.get(i));
//                System.out.println(" ∟ " + constraints.a.get(i).eval(vars));
//            }
//            return;
//        }
        
        System.err.println("Compiling BEE to SAT...");
        BEERunner.makeDimacs(path("out.bee"), path("bee.dimacs"), path("bee.map"));

        String cnf = new BufferedReader(new FileReader("bee.dimacs")).lines().collect(Collectors.joining("\n"));

        System.err.println("Solving SAT...");
        long[] time = new long[1];
        boolean[] result = CryptominisatPort.solve(
                cnf, 
                null, 
                null, 
                1_000_000, 
                time, 
                "cryptominisat --threads=4"
        );
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(time));

        if (result != null) {
            PrintWriter resPw = new PrintWriter("result");
            Map<String, Object> map = MapResolver.resolve(new File("bee.map"), result);
//            if (!ResultVerifier.verify(map, n, k)) {
//                System.out.println("Incorrect result!");
//                return;
//            }
            map.forEach((s, o) -> {
                if (!s.startsWith("temp")) {
                    resPw.println(s + " = " + o);
                }
            });
            resPw.close();
            PhylogeneticNetwork network = BEENetworkBuilder.buildNetwork(map, inputTrees, k);
            new PrintWriter(new FileWriter("test.gv"), true).println(network.toGVString());
        }
    }
    
    private static String path(String filepath) throws IOException {
        return new File(filepath).getCanonicalPath();
    }

    private static List<List<PhylogeneticTree>> preprocessing(List<PhylogeneticTree> inputTrees) {
        List<List<PhylogeneticTree>> ans = new ArrayList<>();
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

        return ans;
    }

    private static List<PhylogeneticTree> collapseAll(List<PhylogeneticTree> inputTrees,
                                               List<List<PhylogeneticTree>> splitTrees) {
        ArrayList<PhylogeneticTree> ans = new ArrayList<>();
        for (PhylogeneticTree inputTree : inputTrees) {
            ans.add(new PhylogeneticTree(inputTree));
        }

        int collapsedCount = 0;
        while (collapseEqualsSubtrees(ans, splitTrees)) {
            collapsedCount++;
        }
        return ans;
    }

    private static boolean collapseEqualsSubtrees(List<PhylogeneticTree> trees, List<List<PhylogeneticTree>> splitTrees) {
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

    private static List<PhylogeneticTree> equalsTaxaSplit(List<PhylogeneticTree> trees) {
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

    private static List<PhylogeneticTree> normalize(List<PhylogeneticTree> inputTrees) {
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

    private static boolean isNormalized(PhylogeneticTree tree, String childLabel) {
        if (tree.size() < 3)
            return false;

        List<Integer> children = tree.getChildren(tree.size() - 1);

        return (tree.isLeaf(children.get(0)) && tree.getLabel(children.get(0)).equals(childLabel))
                || (tree.isLeaf(children.get(1)) && tree.getLabel(children.get(1)).equals(childLabel));
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
}
