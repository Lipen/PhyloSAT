import beepp.BEEppCompiler;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.SimpleRootedTree;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Vyacheslav Moklev
 */
public class DummyMain {
    public static void main(String[] args) throws IOException {
        List<SimpleRootedTree> trees = new ArrayList<>();
        String filePath = "test.trees";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            NewickImporter importer = new NewickImporter(reader, false);
            trees.addAll(importer.importTrees().stream().map(tree -> (SimpleRootedTree) tree).collect(Collectors.toList()));
            reader.close();
        } catch (Exception ignored) {}

        List<PhylogeneticTree> inputTrees = new ArrayList<>();
        for (SimpleRootedTree srt : trees) {
            PhylogeneticTree inputTree = new PhylogeneticTree(srt);
            inputTrees.add(inputTree);
        }

        for (PhylogeneticTree tree: inputTrees) {
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
        BEEppCompiler.compile(new FileInputStream("out.keksik"), new FileOutputStream("out.bee"));
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
        }
    }
    
    private static String path(String filepath) throws IOException {
        return new File(filepath).getCanonicalPath();
    }
}
