import beepp.BEEppCompiler;
import beepp.expression.*;
import beepp.parser.BEEppLexer;
import beepp.parser.BEEppParser;
import beepp.util.Pair;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.SimpleRootedTree;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Vyacheslav Moklev
 */
public class DummyMain {
    public static void main(String[] args) throws IOException {
        List<SimpleRootedTree> trees = new ArrayList<>();
        String filePath = "C:\\Users\\slava\\Downloads\\PhyloSAT-master\\PhyloSAT\\data\\simple.tre";
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
        pw.print(new BEEFormulaBuilder(inputTrees, 3, false).build());
        pw.close();

        BEEppCompiler.compile(new FileInputStream("out.keksik"), new FileOutputStream("out.bee"));
    }
}
