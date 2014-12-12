import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;
import junit.framework.TestCase;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PhylogeneticTreeTest extends TestCase {
    public void testFictitiousRoot() throws Exception {
        List<PhylogeneticTree> trees = new ArrayList<>();
        List<PhylogeneticTree> originalTrees = new ArrayList<>();
        String s = "(0,(1,(2,3)));\n((0,(1,2)),3);";
        StringReader reader = new StringReader(s);
        NewickImporter importer = new NewickImporter(reader, false);
        for(Tree tree : importer.importTrees()) {
            trees.add(new PhylogeneticTree((SimpleRootedTree) tree));
            originalTrees.add(new PhylogeneticTree((SimpleRootedTree) tree));
        }

        for(PhylogeneticTree tree : trees) {
            tree.addFictitiousRoot();
        }
        for(PhylogeneticTree tree : trees) {
            tree.removeFictitiousRoot();
        }

        assertEquals(trees.size(), originalTrees.size());
        for(int i = 0; i < trees.size(); i++) {
            TestCase.assertTrue(PhylogeneticTree.isSubtreesEquals(trees.get(i), trees.get(i).size() - 1,
                    originalTrees.get(i), originalTrees.get(i).size() - 1));
        }
    }
}