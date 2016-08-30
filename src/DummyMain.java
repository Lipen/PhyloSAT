import beepp.BEEppCompiler;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.SimpleRootedTree;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

        PrintWriter pw = new PrintWriter(new FileWriter("out.keksik"), true);
        pw.print(new BEEFormulaBuilder(inputTrees, 1, false).build());
        pw.close();

        BEEppCompiler.compile(new FileInputStream("out.keksik"), new FileOutputStream("out.bee"));

        BEERunner.makeDimacs("out.bee", "bee.dimacs", "bee.map");
    }
}
