import java.util.*;

/**
 * Vladimir Ulyantsev
 * Date: 24.04.13
 * Time: 21:01
 */
public class FormulaBuilder {
    private int n;

    private int k;

    private int treeNodesCount;

    private List<PhylogeneticTree> phTrees;

    private Map<String, Integer> m;

    private StringBuilder sb, hb;

    private Map<Integer, String> hlp;

    private boolean enableReticulationConnection;

    private boolean disableComments;

    private int clausesCount;

//    public FormulaBuilder(List<PhylogeneticTree> trees, int hybridisationNumber, Map<String, Integer> translationMap) {
//        this(trees, hybridisationNumber, translationMap, false, false);
//    }

    public FormulaBuilder(List<PhylogeneticTree> trees,
                          int hybridisationNumber,
                          Map<String, Integer> translationMap,
                          boolean enableReticulationConnection,
                          boolean disableComments) {
        this.phTrees = trees;

        this.k = hybridisationNumber;
        this.m = translationMap;

        this.n = phTrees.get(0).getTaxaSize();
        this.treeNodesCount = 2 * n - 1 + k;
        this.enableReticulationConnection = enableReticulationConnection;
        this.disableComments = disableComments;
        this.clausesCount = 0;
    }

    public String getHelpString() {
        return this.hb.toString();
    }

    public Map<Integer, String> getHelpMap() {
        return this.hlp;
    }

    public String buildCNF() {
        if (this.m.size() > 0) {
            throw new RuntimeException("Given translation map (variable -> int) is not empty");
        }

        this.sb = new StringBuilder();
        this.hb = new StringBuilder();
        this.hlp = new HashMap<>();
        commentCNF("n = %d; k = %d; trees count = %d", n, k, phTrees.size());

        addParentConstraints();
        addLeftRightConstraints();
        addReticulationChildConstraints();
        addReticulationParentConstraints();
        addChildParentConstraints();

        for (int treeNumber = 0; treeNumber < phTrees.size(); treeNumber++) {
            addDirUsedConstraints(treeNumber);
            if (this.enableReticulationConnection) {
                addRUsedConstraints(treeNumber);
            }
//            addUpConstraints(treeNumber);
            addDownConstraints(treeNumber);
            addXConstraints(treeNumber);
            addDataConstraints(treeNumber);
        }

        for (int treeNumber = 0; treeNumber < phTrees.size(); treeNumber++) {
            for (int otherTree = 0; otherTree < phTrees.size(); otherTree++) {
                if (treeNumber != otherTree) {
                    addConstraintsForPairOfTrees(treeNumber, otherTree);
                }
            }
        }

        String CNFProperties = String.format("p cnf %d %d\n", this.m.size(), this.clausesCount);
        this.sb.insert(0, CNFProperties);

        return this.sb.toString();
    }

    private void addParentConstraints() {
        {
            int intervalStart = m.size() + 1;
            for (int nodeNumber = 0; nodeNumber < treeNodesCount; nodeNumber++) {
                for (int parentNumber : possibleParents(nodeNumber)) {
                    createVar("parent", nodeNumber, parentNumber);
                    hb.append("p ").append(nodeNumber).append(" ").append(parentNumber).append(" ").append(m.size()).append("\n");
                    hlp.put(m.size(), "p " + nodeNumber + " " + parentNumber);
                }
            }
            commentCNF("Variables parent_v_u is in [%d, %d]", intervalStart, m.size());
        }

        commentCNF("At-least-one constraints for parent_v_u");
        for (int nodeNumber = 0; nodeNumber < treeNodesCount - 1; nodeNumber++) {
            String atLeastOneParent = "";
            for (int parentNumber : possibleParents(nodeNumber)) {
                atLeastOneParent += getVar("parent", nodeNumber, parentNumber) + " ";
            }
            addClause(atLeastOneParent);
        }

        commentCNF("At-most-one constraints for parent_v_u");
        for (int nodeNumber = 0; nodeNumber < treeNodesCount - 1; nodeNumber++) {
            for (int parentNumber : possibleParents(nodeNumber)) {
                for (int otherParentNumber : possibleParents(nodeNumber)) {
                    if (otherParentNumber > parentNumber) {
                        addClause(-getVar("parent", nodeNumber, parentNumber),
                                -getVar("parent", nodeNumber, otherParentNumber));
                    }
                }
            }
        }
    }

    private void addLeftRightConstraints() {
        {
            int intervalStart = m.size() + 1;
            for (int nodeNumber = n; nodeNumber < treeNodesCount; nodeNumber++) {
                for (int childNumber : possibleChildren(nodeNumber)) {
                    createVar("left", nodeNumber, childNumber);
                    hb.append("l ").append(nodeNumber).append(" ").append(childNumber).append(" ").append(m.size()).append("\n");
                    hlp.put(m.size(), "l " + nodeNumber + " " + childNumber);
                }
            }
            commentCNF("Variables left_v_u is in [%d, %d]", intervalStart, m.size());

            intervalStart = m.size() + 1;
            for (int nodeNumber = n; nodeNumber < treeNodesCount; nodeNumber++) {
                for (int childNumber : possibleChildren(nodeNumber)) {
                    createVar("right", nodeNumber, childNumber);
                    hb.append("r ").append(nodeNumber).append(" ").append(childNumber).append(" ").append(m.size()).append("\n");
                    hlp.put(m.size(), "r " + nodeNumber + " " + childNumber);
                }
            }
            commentCNF("Variables right_v_u is in [%d, %d]", intervalStart, m.size());
        }

        commentCNF("At-least-one constraints for left_v_u and right_v_u");
        for (int nodeNumber = n; nodeNumber < treeNodesCount; nodeNumber++) {
            String atLeastOneLeft = "", atLeastOneRight = "";
            for (int childNumber : possibleChildren(nodeNumber)) {
                atLeastOneLeft += getVar("left", nodeNumber, childNumber) + " ";
                atLeastOneRight += getVar("right", nodeNumber, childNumber) + " ";
            }
            addClause(atLeastOneLeft);
            addClause(atLeastOneRight);
        }

        commentCNF("At-most-one constraints for left_v_u and right_v_u");
        commentCNF("Also, constraints for left_v_u < right_v_u");
        for (int nodeNumber = n; nodeNumber < treeNodesCount; nodeNumber++) {
            for (int childNumber : possibleChildren(nodeNumber)) {
                for (int otherNumber : possibleChildren(nodeNumber)) {
                    if (childNumber < otherNumber) {
                        addClause(-getVar("left", nodeNumber, childNumber),
                                -getVar("left", nodeNumber, otherNumber));
                        addClause(-getVar("right", nodeNumber, childNumber),
                                -getVar("right", nodeNumber, otherNumber));
                    }

                    if (childNumber <= otherNumber) {
                        addClause(-getVar("right", nodeNumber, childNumber),
                                -getVar("left", nodeNumber, otherNumber));
                    }
                }
            }
        }
    }

    private void addReticulationChildConstraints() {
        {
            int intervalStart = m.size() + 1;
            for (int nodeNumber : reticulationNodes()) {
                for (int childNumber : possibleChildren(nodeNumber)) {
                    createVar("ch", nodeNumber, childNumber);
                    hb.append("re ").append(nodeNumber).append(" ").append(childNumber).append(" ").append(m.size()).append("\n");
                    hlp.put(m.size(), "re " + nodeNumber + " " + childNumber);
                }
            }
            commentCNF("Variables ch_v_u is in [%d, %d]", intervalStart, m.size());
        }

        commentCNF("At-least-one constraints for ch_v_u");
        for (int nodeNumber : reticulationNodes()) {
            String atLeastOne = "";
            for (int childNumber : possibleChildren(nodeNumber)) {
                atLeastOne += getVar("ch", nodeNumber, childNumber) + " ";
            }
            addClause(atLeastOne);
        }

        commentCNF("At-most-one constraints for ch_v_u");
        for (int nodeNumber : reticulationNodes()) {
            for (int childNumber : possibleChildren(nodeNumber)) {
                for (int otherNumber : possibleChildren(nodeNumber)) {
                    if (childNumber < otherNumber) {
                        addClause(-getVar("ch", nodeNumber, childNumber), -getVar("ch", nodeNumber, otherNumber));
                    }
                }
            }
        }
    }

    private void addReticulationParentConstraints() {
        {
            int intervalStart = m.size() + 1;
            for (int nodeNumber : reticulationNodes()) {
                for (int parentNumber : possibleParents(nodeNumber)) {
                    createVar("lp", nodeNumber, parentNumber);
                    hb.append("rpl ").append(nodeNumber).append(" ").append(parentNumber).append(" ").append(m.size()).append("\n");
                    hlp.put(m.size(), "rpl " + nodeNumber + " " + parentNumber);
                }
            }
            commentCNF("Variables lp_v_u is in [%d, %d]", intervalStart, m.size());

            intervalStart = m.size() + 1;
            for (int nodeNumber : reticulationNodes()) {
                for (int parentNumber : possibleParents(nodeNumber)) {
                    createVar("rp", nodeNumber, parentNumber);
                    hb.append("rpr ").append(nodeNumber).append(" ").append(parentNumber).append(" ").append(m.size()).append("\n");
                    hlp.put(m.size(), "rpr " + nodeNumber + " " + parentNumber);
                }
            }
            commentCNF("Variables rp_v_u is in [%d, %d]", intervalStart, m.size());
        }

        commentCNF("At-least-one constraints for lp_v_u and rp_v_u");
        for (int nodeNumber : reticulationNodes()) {
            String atLeastOneLeft = "", atLeastOneRight = "";
            for (int parentNumber : possibleParents(nodeNumber)) {
                atLeastOneLeft += getVar("lp", nodeNumber, parentNumber) + " ";
                atLeastOneRight += getVar("rp", nodeNumber, parentNumber) + " ";
            }
            addClause(atLeastOneLeft);
            addClause(atLeastOneRight);
        }

        commentCNF("At-most-one constraints for lp_v_u and rp_v_u");
        commentCNF("Also, constraints for lp_v_u < rp_v_u");
        for (int nodeNumber : reticulationNodes()) {
            for (int parent : possibleParents(nodeNumber)) {
                for (int otherParent : possibleParents(nodeNumber)) {
                    int lpVar = getVar("lp", nodeNumber, parent);
                    int otherLpVar = getVar("lp", nodeNumber, otherParent);
                    int rpVar = getVar("rp", nodeNumber, parent);
                    int otherRpVar = getVar("rp", nodeNumber, otherParent);

                    if (parent < otherParent) {
                        addClause(-lpVar, -otherLpVar);
                        addClause(-rpVar, -otherRpVar);
                    }

                    if (parent <= otherParent) {
                        addClause(-rpVar, -otherLpVar);
                    }
                }
            }
        }
    }

    private void addChildParentConstraints() {
        commentCNF("Constraints which connect tree nodes left_v_u, right_v_u with children's parents vars");
        for (int nodeNumber : treeNodes()) {
            for (int childNumber : possibleChildren(nodeNumber)) {
                int leftVar = getVar("left", nodeNumber, childNumber);
                int rightVar = getVar("right", nodeNumber, childNumber);

                if (childNumber < treeNodesCount) {
                    int parentVar = getVar("parent", childNumber, nodeNumber);

                    addClause(-leftVar, parentVar); // if LEFT then PARENT
                    addClause(-rightVar, parentVar); // if RIGHT then PARENT
                    addClause(-parentVar, leftVar, rightVar); // if PARENT then LEFT or RIGHT
                } else {
                    int lpVar = getVar("lp", childNumber, nodeNumber);
                    int rpVar = getVar("rp", childNumber, nodeNumber);

                    addClause(-leftVar, lpVar, rpVar); // if LEFT then LP or RP
                    addClause(-rightVar, lpVar, rpVar); // if RIGHT then LP or RP
                    addClause(-lpVar, leftVar, rightVar); // if LP then LEFT or RIGHT
                    addClause(-rpVar, leftVar, rightVar); // if RP then LEFT or RIGHT
                }
            }
        }

        commentCNF("Constraints which connect reticulation nodes ch_v_u with children's parents vars");
        for (int nodeNumber : reticulationNodes()) {
            for (int childNumber : possibleChildren(nodeNumber)) {
                int chVar = getVar("ch", nodeNumber, childNumber);

                if (childNumber < treeNodesCount) {
                    int parentVar = getVar("parent", childNumber, nodeNumber);

                    addClause(-chVar, parentVar); // if CH then PARENT
                    addClause(-parentVar, chVar); // if PARENT then CH
                } else {
                    int lpVar = m.get("lp_" + childNumber + "_" + nodeNumber);
                    int rpVar = m.get("rp_" + childNumber + "_" + nodeNumber);

                    addClause(-lpVar, chVar); // if LP then CH
                    addClause(-rpVar, chVar); // if RP then CH
                    addClause(-chVar, lpVar, rpVar); // if CH then LP or RP
                }
            }
        }

        commentCNF("Constraints for ordering ch_v_u and lp_v_u, rp_v_u, connected with reticulation nodes");
        for (int nodeNumber : reticulationNodes()) {
            for (int childNumber : possibleChildren(nodeNumber)) {
                if (childNumber < treeNodesCount) {
                    int chVar = getVar("ch", nodeNumber, childNumber);

                    for (int parentNumber = n; parentNumber <= childNumber; parentNumber++) {
                        int lpVar = getVar("lp", nodeNumber, parentNumber);
                        int rpVar = getVar("rp", nodeNumber, parentNumber);

                        addClause(-chVar, -lpVar); // CHILD less then LP
                        addClause(-chVar, -rpVar); // CHILD less then RP
                    }
                }
            }
        }
    }

    private void addDirUsedConstraints(int treeNumber) {
        {
            int intervalStart = m.size() + 1;
            for (int nodeNumber : reticulationNodes()) {
                createVar("dir", treeNumber, nodeNumber);
                hb.append("dir ").append(treeNumber).append(" ").append(nodeNumber).append(" ").append(m.size()).append("\n");
                hlp.put(m.size(), "dir " + treeNumber + " " + nodeNumber);
            }
            commentCNF("Variables dir_%d_v is in [%d, %d]", treeNumber, intervalStart, m.size());

            intervalStart = m.size() + 1;
            for (int nodeNumber : treeNodes()) {
                createVar("used", treeNumber, nodeNumber);
                hb.append("used ").append(treeNumber).append(" ").append(nodeNumber).append(" ").append(m.size()).append("\n");
                hlp.put(m.size(), "used " + treeNumber + " " + nodeNumber);
            }
            commentCNF("Variables used_%d_v is in [%d, %d]", treeNumber, intervalStart, m.size());
        }

        commentCNF("(not dir => not used) constraints");
        for (int nodeNumber : reticulationNodes()) {
            for (int parentNumber : possibleParents(nodeNumber)) {
                int dirVar = getVar("dir", treeNumber, nodeNumber);
                int lpVar = getVar("lp", nodeNumber, parentNumber);
                int rpVar = getVar("rp", nodeNumber, parentNumber);

                if (parentNumber < treeNodesCount) {
                    int usedVar = getVar("used", treeNumber, parentNumber);

                    addClause(dirVar, -lpVar, -usedVar); // if ~DIR and LP then ~USED
                    addClause(-dirVar, -rpVar, -usedVar); // if DIR and RP then ~USED
                }
            }
        }
    }

    private void addRUsedConstraints(int treeNumber) {
        {
            int intervalStart = m.size() + 1;
            for (int nodeNumber : reticulationNodes()) {
                createVar("rused", treeNumber, nodeNumber);
                hb.append("rused ").append(treeNumber).append(" ").append(nodeNumber).append(" ").append(m.size()).append("\n");
                hlp.put(m.size(), "rused " + treeNumber + " " + nodeNumber);
            }
            commentCNF("Variables rused_%d_v is in [%d, %d]", treeNumber, intervalStart, m.size());
        }

        commentCNF("RUsed constraints");
        for (int nodeNumber : reticulationNodes()) {
            int rusedVar = getVar("rused", treeNumber, nodeNumber);

            for (int childNumber : possibleChildren(nodeNumber)) {
                int chVar = getVar("ch", nodeNumber, childNumber);

                if (childNumber < treeNodesCount) {
                    addClause(-chVar, rusedVar); // if CH is tree node then RUSED
                } else {
                    int childRusedVar = getVar("rused", treeNumber, childNumber);
                    int childDirVar = getVar("dir", treeNumber, childNumber);
                    int lpVar = getVar("lp", childNumber, nodeNumber);
                    int rpVar = getVar("rp", childNumber, nodeNumber);

                    addClause(-chVar, childRusedVar, -rusedVar); // if CH not RUSED then ~RUSED

                    addClause(-lpVar, childDirVar, -rusedVar); // if child LP and ~DIR then ~RUSED
                    // if child LP and DIR and child RUSED then RUSED
                    addClause(-lpVar, -childDirVar, -childRusedVar, rusedVar);

                    // same for RP but DIR
                    addClause(-rpVar, -childDirVar, -rusedVar);
                    addClause(-rpVar, childDirVar, -childRusedVar, rusedVar);
                }
            }
        }

        commentCNF("Connect RUsed with tree nodes Used");
        for (int nodeNumber : reticulationNodes()) {
            int rusedVar = getVar("rused", treeNumber, nodeNumber);

            for (int parentNumber : possibleParents(nodeNumber)) {
                int lpVar = getVar("lp", nodeNumber, parentNumber);
                int rpVar = getVar("rp", nodeNumber, parentNumber);

                if (parentNumber < treeNodesCount) {
                    int usedVar = m.get("used_" + treeNumber + "_" + parentNumber);

                    addClause(-lpVar, rusedVar, -usedVar); // LP and ~RUSED then ~USED
                    addClause(-rpVar, rusedVar, -usedVar); // RP and ~RUSED then ~USED
                }
            }
        }
    }

    private void addDownConstraints(int treeNumber) {
        {
            int intervalStart = m.size() + 1;
            for (int nodeNumber = n; nodeNumber < treeNodesCount + k; ++nodeNumber) {
                for (int down : possibleDown(nodeNumber)) {
                    createVar("downl", treeNumber, nodeNumber, down);
                    hb.append("downl ").append(treeNumber).append(" ").append(nodeNumber).append(" ").append(down).append(" ").append(m.size()).append("\n");
                    hlp.put(m.size(), "downl " + treeNumber + " " + nodeNumber + " " + down);
                }
            }
            commentCNF("Variables downl_%d_v_'u is in [%d, %d]", treeNumber, intervalStart, m.size());

            intervalStart = m.size() + 1;
            for (int nodeNumber = n; nodeNumber < treeNodesCount + k; ++nodeNumber) {
                for (int down : possibleDown(nodeNumber)) {
                    createVar("downr", treeNumber, nodeNumber, down);
                    hb.append("downr ").append(treeNumber).append(" ").append(nodeNumber).append(" ").append(down).append(" ").append(m.size()).append("\n");
                    hlp.put(m.size(), "downr " + treeNumber + " " + nodeNumber + " " + down);
                }
            }
            commentCNF("Variables downr_%d_v_'u is in [%d, %d]", treeNumber, intervalStart, m.size());
        }

        commentCNF("At-least-one constraints for downl_%d_v_u and downr_%d_v_u", treeNumber, treeNumber);
        for (int node : allNodesExceptLeaves()) {
            String atLeastOneLeft = "", atLeastOneRight = "";
            for (int down : possibleDown(node)) {
                atLeastOneLeft += getVar("downl", treeNumber, node, down) + " ";
                atLeastOneRight += getVar("downr", treeNumber, node, down) + " ";
            }
            addClause(atLeastOneLeft);
            addClause(atLeastOneRight);
        }

        commentCNF("At-most-one constraints for downl_%d_v_u and downr_%d_v_u", treeNumber, treeNumber);
        commentCNF("Also, constraints for downl_%d_v_u < downr_%d_v_u", treeNumber, treeNumber);
        for (int node : allNodesExceptLeaves()) {
            for (int down : possibleDown(node)) {
                for (int otherDown : possibleDown(node)) {
                    if (down < otherDown) {
                        addClause(-getVar("downl", treeNumber, node, down),
                                -getVar("downl", treeNumber, node, otherDown));
                        addClause(-getVar("downr", treeNumber, node, down),
                                -getVar("downr", treeNumber, node, otherDown));
                    }
//                    if (down <= otherDown) {
//                        addClause(-getVar("downr", treeNumber, node, down),
//                                -getVar("downl", treeNumber, node, otherDown));
//                    }
                }
            }
        }

        commentCNF("Connect downl_%d_v_u and downr_%d_v_u with left_v_u, right_v_u and used_%d_v (tree nodes)", treeNumber, treeNumber, treeNumber);
//        for (int node : treeNodes()) {
//            for (int child : possibleChildren(node)) {
//                int leftChildVar = getVar("left", node, child);
//                int rightChildVar = getVar("right", node, child);
//
//                if (child < n) {
//                    continue;
//                }
//
//                if (child < treeNodesCount) {
//                    int downLeftVar = getVar("downl", treeNumber, node, child);
//                    int downRightVar = getVar("downr", treeNumber, node, child);
//                    int usedChildVar = getVar("used", treeNumber, child);
//
//                    // if left child is used
//                    {
//                        addClause(-leftChildVar, -usedChildVar, downLeftVar); // if LEFT and USED then DOWNLEFT
//                        addClause(-leftChildVar, -downLeftVar, usedChildVar); // if LEFT and DOWNLEFT then USED
//                    }
//
//                    // if right child is used
//                    {
//                        addClause(-rightChildVar, -usedChildVar, downRightVar); // if RIGHT and USED then DOWNRIGHT
//                        addClause(-rightChildVar, -downRightVar, usedChildVar); // if RIGHT and DOWNRIGHT then USED
//                    }
//
//                    // if left child is not used
//                    for (int childDown : possibleDown(child)) {
//                        int nextDownLeftVar = getVar("downl", treeNumber, node, childDown);
//                        int childDownLeftVar = getVar("downl", treeNumber, child, childDown);
//
//                        addClause(-leftChildVar, usedChildVar, -childDownLeftVar, nextDownLeftVar);
//                        addClause(-leftChildVar, usedChildVar, -nextDownLeftVar, childDownLeftVar);
//
////                        int nextDownRightVar = getVar("downr", treeNumber, node, childDown);
////                        int childDownRightVar = getVar("downr", treeNumber, child, childDown);
////
////                        addClause(-leftChildVar, usedChildVar, -childDownRightVar, nextDownRightVar);
////                        addClause(-leftChildVar, usedChildVar, -nextDownRightVar, childDownRightVar);
//                    }
//
//                    // if right child is not used
//                    for (int childDown : possibleDown(child)) {
////                        int nextDownLeftVar = getVar("downl", treeNumber, node, childDown);
////                        int childDownLeftVar = getVar("downl", treeNumber, child, childDown);
////
////                        addClause(-rightChildVar, usedChildVar, -childDownLeftVar, nextDownLeftVar);
////                        addClause(-rightChildVar, usedChildVar, -nextDownLeftVar, childDownLeftVar);
//
//                        int nextDownRightVar = getVar("downr", treeNumber, node, childDown);
//                        int childDownRightVar = getVar("downr", treeNumber, child, childDown);
//
//                        addClause(-rightChildVar, usedChildVar, -childDownRightVar, nextDownRightVar);
//                        addClause(-rightChildVar, usedChildVar, -nextDownRightVar, childDownRightVar);
//                    }
//                } else {
//                    for (int childDown : possibleDown(child)) {
//                        int childDownLeftVar = getVar("downl", treeNumber, child, childDown);
//                        int childDownRightVar = getVar("downr", treeNumber, child, childDown);
//
//                        if (childDown >= node) {
//                            addClause(-leftChildVar, -childDownLeftVar);
//                            addClause(-leftChildVar, -childDownRightVar);
//                            addClause(-rightChildVar, -childDownLeftVar);
//                            addClause(-rightChildVar, -childDownRightVar);
//                        } else {
//                            int downLeftVar = getVar("downl", treeNumber, node, childDown);
//                            int downRightVar = getVar("downr", treeNumber, node, childDown);
//
//                            addClause(-leftChildVar, -childDownLeftVar, downLeftVar);
////                            addClause(-leftChildVar, -childDownRightVar, downLeftVar);
////                            addClause(-rightChildVar, -childDownLeftVar, downRightVar);
//                            addClause(-rightChildVar, -childDownRightVar, downRightVar);
//
//                            // здесь можно добавить еще ограничение
//                        }
//                    }
//                }
//            }
//        }

//        commentCNF("Connect up_%d_v_u with parent_v_u and used_%d_v (network nodes)", treeNumber, treeNumber);
//        for (int nodeNumber : reticulationNodes()) {
//            int dirVar = getVar("dir", treeNumber, nodeNumber);
//
//            for (int parent : possibleParents(nodeNumber)) {
//                int lpVar = getVar("lp", nodeNumber, parent);
//                int rpVar = getVar("rp", nodeNumber, parent);
//
//                if (parent < treeNodesCount) {
//                    int parentUsedVar = getVar("used", treeNumber, parent);
//                    int upVar = getVar("up", treeNumber, nodeNumber, parent);
//                    // if parent is used and up
//                    {
//                        addClause(-lpVar, -dirVar, -parentUsedVar, upVar); // LP and USED then UP
//                        addClause(-rpVar, dirVar, -parentUsedVar, upVar); // RP and USED then UP
//                    }
//
//                    // if parent is not used
//                    for (int parentUp : possibleUp(parent)) {
//                        int parentUpVar = getVar("up", treeNumber, parent, parentUp);
//                        int nodeUpVar = getVar("up", treeNumber, nodeNumber, parentUp);
//
//                        // PARENT is LP and ~UP
//                        addClause(-lpVar, -dirVar, parentUsedVar, -parentUpVar, nodeUpVar);
//
//                        // PARENT is RP and ~UP
//                        addClause(-rpVar, dirVar, parentUsedVar, -parentUpVar, nodeUpVar);
//                    }
//                } else {
//                    for (int parentUp : possibleUp(parent)) {
//                        // parent is reticulation
//                        int parentUpVar = getVar("up", treeNumber, parent, parentUp);
//                        int nodeUpVar = getVar("up", treeNumber, nodeNumber, parentUp);
//
//                        addClause(-lpVar, -dirVar, -parentUpVar, nodeUpVar);
//                        addClause(-rpVar, dirVar, -parentUpVar, nodeUpVar);
//                    }
//                }
//            }
//        }
    }

    private void addXConstraints(int treeNumber) {
        {
            int intervalStart = m.size() + 1;
            for (int treeNodeNumber = n; treeNodeNumber < 2 * n - 1; treeNodeNumber++) {
                for (int nodeNumber : treeNodes()) {
                    createVar("x", treeNumber, treeNodeNumber, nodeNumber);
                    hb.append("x ").append(treeNumber).append(" ").append(treeNodeNumber).append(" ").
                            append(nodeNumber).append(" ").append(m.size()).append("\n");
                    hlp.put(m.size(), "x " + treeNumber + " " + treeNodeNumber + " " + nodeNumber);
                }
            }
            for (int childNumber = 0; childNumber < n; childNumber++) {
                createVar("x", treeNumber, childNumber, childNumber);
                hb.append("x ").append(treeNumber).append(" ").append(childNumber).append(" ").
                        append(childNumber).append(" ").append(m.size()).append("\n");
                hlp.put(m.size(), "x " + treeNumber + " " + childNumber + " " + childNumber);
            }
            commentCNF("Variables x_%d_tv_v is in [%d, %d]", treeNumber, intervalStart, m.size());
        }

        if(treeNumber == 1) {
            addClause(-(getVar("x", treeNumber, 5, 5)));
            addClause(-(getVar("x", treeNumber, 4, 4)));
            addClause(getVar("downr", 1, 7, 3));
            addClause(getVar("downl", 1, 7, 6));
            addClause(getVar("downr", 1, 6, 5));
            addClause(getVar("downr", 1, 8, 3));
            addClause(getVar("downl", 1, 4, 2));
            addClause(getVar("downl", 1, 5, 1));
            addClause(getVar("downl", 1, 6, 0));
        }
        commentCNF("At-least-one constraints for x_%d_tv_v", treeNumber);
        for (int treeNodeNumber = n; treeNodeNumber < 2 * n - 1; treeNodeNumber++) {
            String atLeastOne = "";
            for (int nodeNumber : treeNodes()) {
                atLeastOne += getVar("x", treeNumber, treeNodeNumber, nodeNumber) + " ";
            }
            addClause(atLeastOne);
        }

        commentCNF("At-most-one constraints for x_" + treeNumber + "_tv_v");
        for (int treeNodeNumber = n; treeNodeNumber < 2 * n - 1; treeNodeNumber++) {
            for (int nodeNumber : treeNodes()) {
                for (int otherNode : treeNodes()) {
                    if (nodeNumber < otherNode) {
                        addClause(-getVar("x", treeNumber, treeNodeNumber, nodeNumber),
                                -getVar("x", treeNumber, treeNodeNumber, otherNode));
                    }
                }
            }
        }

        commentCNF("Only-one constraints for x_" + treeNumber + "_tv_v for leaves");
        for (int childNodeNumber = 0; childNodeNumber < n; childNodeNumber++) {
            addClause(getVar("x", treeNumber, childNodeNumber, childNodeNumber));
        }

        commentCNF("At-most-one x_%d_tv_v points to v", treeNumber);
        for (int treeNodeNumber = n; treeNodeNumber < 2 * n - 1; treeNodeNumber++) {
            for (int otherTreeNode = n; otherTreeNode < treeNodeNumber; otherTreeNode++) {
                for (int nodeNumber : treeNodes()) {
                    addClause(-getVar("x", treeNumber, treeNodeNumber, nodeNumber),
                            -getVar("x", treeNumber, otherTreeNode, nodeNumber));
                }
            }
        }

        commentCNF("If x_%d_tv_v then used_%d_v", treeNumber, treeNumber);
        for (int treeNodeNumber = n; treeNodeNumber < 2 * n - 1; treeNodeNumber++) {
            for (int nodeNumber : treeNodes()) {
                // X means USED
                addClause(-getVar("x", treeNumber, treeNodeNumber, nodeNumber),
                        getVar("used", treeNumber, nodeNumber));
            }
        }
    }

    private void addDataConstraints(int treeNumber) {
        PhylogeneticTree phTree = this.phTrees.get(treeNumber);

        commentCNF("Data constraints for tree %d: connect X and DOWN variables", treeNumber);
        for (int treeNodeNumber = n; treeNodeNumber < phTree.size(); treeNodeNumber++) {
            List<Integer> children = phTree.getChildren(treeNodeNumber);
            if (children.isEmpty()) {
                continue;
            }

            int treeLeftChildNumber = children.get(0);
            int treeRightChildNumber = children.get(1);

            for (int node : treeNodes()) {
                int xVar = getVar("x", treeNumber, treeNodeNumber, node);

                for (int child : possibleDown(node)) {
                    if (child >= n) {
//                        if (treeLeftChildNumber >= n) {
//                            int leftChildXVar = getVar("x", treeNumber, treeLeftChildNumber, child);
//                            int downLeftVar = getVar("downl", treeNumber, node, child);
//                            addClause(-xVar, -leftChildXVar, downLeftVar);
//                        } else {
//                            int downLeftVar = getVar("downl", treeNumber, node, child);
//                            addClause(-xVar, -downLeftVar);
//                        }
////
//                        if (treeRightChildNumber >= n) {
//                            int rightChildXVar = getVar("x", treeNumber, treeRightChildNumber, child);
//                            int downRightVar = getVar("downr", treeNumber, node, child);
//                            addClause(-xVar, -rightChildXVar, downRightVar);
//                        } else {
//                            int downRightVar = getVar("downr", treeNumber, node, child);
//                            addClause(-xVar, -downRightVar);
//                        }
                    } else {
//                        if (treeLeftChildNumber == child) {
//                            int leftChildXVar = getVar("x", treeNumber, treeLeftChildNumber, child);
//                            int downLeftVar = getVar("downl", treeNumber, node, child);
//                            addClause(-xVar, -leftChildXVar, downLeftVar);
//                        } else {
//                            int downLeftVar = getVar("downl", treeNumber, node, child);
//                            addClause(-xVar, -downLeftVar);
//                        }
//
//                        if (treeRightChildNumber == child) {
//                            int rightChildXVar = getVar("x", treeNumber, treeRightChildNumber, child);
//                            int downRightVar = getVar("downr", treeNumber, node, child);
//                            addClause(-xVar, -rightChildXVar, downRightVar);
//                        } else {
//                            int downRightVar = getVar("downr", treeNumber, node, child);
//                            addClause(-xVar, -downRightVar);
//                        }
                    }
                }

//                for (int childNodeNumber : treeNodes()) {
//                    if (childNodeNumber >= node) {
//                        if (treeLeftChildNumber >= n) {
//                            int leftChildXVar = getVar("x", treeNumber, treeLeftChildNumber, childNodeNumber);
//                            addClause(-xVar, -leftChildXVar);
//                        }
//                        if (treeRightChildNumber >= n) {
//                            int rightChildXVar = getVar("x", treeNumber, treeRightChildNumber, childNodeNumber);
//                            addClause(-xVar, -rightChildXVar);
//                        }
//                    }
//                }
            }
        }

        commentCNF("Constraints connected with tree nodes depth and subtree sizes (heap structure)");
        for (int treeNodeNumber = n; treeNodeNumber < phTree.size() - 1; treeNodeNumber++) {
            int subtreeNonLeafCount = phTree.getSubtreeSize(treeNodeNumber) / 2 - 1;
            for (int nodeNumber = n; nodeNumber < n + subtreeNonLeafCount; nodeNumber++) {
                addClause(-getVar("x", treeNumber, treeNodeNumber, nodeNumber));
            }
            for (int nodeNumber = treeNodesCount - phTree.getDepth(treeNodeNumber); nodeNumber < treeNodesCount; nodeNumber++) {
                addClause(-getVar("x", treeNumber, treeNodeNumber, nodeNumber));
            }
        }

    }

    private void addConstraintsForPairOfTrees(int t1, int t2) {
        PhylogeneticTree phTree1 = this.phTrees.get(t1);
        PhylogeneticTree phTree2 = this.phTrees.get(t2);

        int totalEquals = 0, totalDifferent = 0;
        for (int node1 = n; node1 < 2 * n - 2; node1++) {
            List<Integer> taxa1 = phTree1.getTaxa(node1);
            for (int node2 = n; node2 < 2 * n - 2; node2++) {
                List<Integer> taxa2 = phTree2.getTaxa(node2);

                boolean isEquals = taxa1.size() == taxa2.size() && taxa1.containsAll(taxa2);
                if (isEquals) {
                    addEqualsNodesConstraints(t1, node1, t2, node2);
                    totalEquals++;
                }

                boolean allDifferent = Collections.disjoint(taxa1, taxa2);
                if (allDifferent && t1 < t2) {
                    addDifferentTaxaNodesConstraints(t1, node1, t2, node2);
                    totalDifferent++;
                }
            }
        }
        commentCNF("In trees %d and %d there are %d pairs of equal nodes and %d pairs of different nodes",
                t1, t2, totalEquals, totalDifferent);
    }

    private void addEqualsNodesConstraints(int t1, int n1, int t2, int n2) {
        PhylogeneticTree phTree1 = this.phTrees.get(t1);
        PhylogeneticTree phTree2 = this.phTrees.get(t2);
        commentCNF("Node %d in tree %d have the same %d taxons (out of %d) in subtree as node %d in tree %d",
                n1, t1, phTree1.getTaxa(n1).size(), this.n, n2, t2);


        for (int nodeNumber : treeNodes()) {
            int x1Var = getVar("x", t1, n1, nodeNumber);
            int x2Var = getVar("x", t2, n2, nodeNumber);
            addClause(-x1Var, x2Var); // x1 then x2
        }

        for (int subtreeNode : phTree1.getSubtreeNodes(n1)) {
            if (subtreeNode >= n) {
                for (int nonSubtreeNode = n; nonSubtreeNode < 2 * n - 1; nonSubtreeNode++) {
                    if (!phTree2.getSubtreeNodes(n2).contains(nonSubtreeNode)) {
                        for (int nodeNumber : treeNodes()) {
                            int x1Var = getVar("x", t1, subtreeNode, nodeNumber);
                            int x2Var = getVar("x", t2, nonSubtreeNode, nodeNumber);
                            addClause(-x1Var, -x2Var); // x1 then ~x2
                        }
                    }
                }
            }
        }
    }

    /*
    Adding this constraints is a little bit risky
     */
    private void addDifferentTaxaNodesConstraints(int t1, int n1, int t2, int n2) {
        PhylogeneticTree phTree1 = this.phTrees.get(t1);
        PhylogeneticTree phTree2 = this.phTrees.get(t2);
        commentCNF("Node %d in tree %d and node %d in tree %d have disjoint set of taxons (%d and %d)",
                n1, t1, n2, t2, phTree1.getTaxa(n1).size(), phTree2.getTaxa(n2).size());

        for (int nodeNumber : treeNodes()) {
            int x1Var = getVar("x", t1, n1, nodeNumber);
            int x2Var = getVar("x", t2, n2, nodeNumber);
            addClause(-x1Var, -x2Var);
        }
    }

    private List<Integer> possibleChildren(int nodeNumber) {
        if (nodeNumber < 0 || nodeNumber >= treeNodesCount + k) {
            throw new RuntimeException("Node number out of bounds");
        }

        List<Integer> ans = new ArrayList<>();
        if (nodeNumber < n) {
            return ans;
        }
        for (int childNumber : allNodes()) {
            if (nodeNumber < treeNodesCount) {
                if (childNumber < nodeNumber || childNumber >= treeNodesCount) {
                    ans.add(childNumber);
                }
            } else if (childNumber < treeNodesCount || (enableReticulationConnection && childNumber < nodeNumber)) {
                ans.add(childNumber);
            }
        }

        return ans;
    }

    private List<Integer> possibleParents(int nodeNumber) {
        if (nodeNumber < 0 || nodeNumber >= treeNodesCount + k) {
            throw new RuntimeException("Node number out of bounds");
        }

        List<Integer> ans = new ArrayList<>();
//        if (nodeNumber == treeNodesCount - 1) {
//            return ans;
//        }
        for (int parentNumber = n; parentNumber < treeNodesCount + k; parentNumber++) {
            if (nodeNumber < n) {
                ans.add(parentNumber);
            } else if (nodeNumber < treeNodesCount) {
                if (nodeNumber < parentNumber) {
                    ans.add(parentNumber);
                }
            } else if (parentNumber < treeNodesCount || (enableReticulationConnection && nodeNumber < parentNumber)) {
                ans.add(parentNumber);
            }
        }

        return ans;
    }

    private List<Integer> possibleDown(int nodeNumber) {
        List<Integer> ans = new ArrayList<>();
        for (int down : possibleChildren(nodeNumber)) {
            if (down < treeNodesCount) {
                ans.add(down);
            }
        }
        return ans;
    }

    private List<Integer> allNodes() {
        List<Integer> ans = new ArrayList<>();
        for (int nodeNumber = 0; nodeNumber < this.treeNodesCount + k; nodeNumber++) {
            ans.add(nodeNumber);
        }
        return ans;
    }

    private List<Integer> treeNodes() {
        List<Integer> ans = new ArrayList<>();
        for (int nodeNumber = this.n; nodeNumber < this.treeNodesCount; nodeNumber++) {
            ans.add(nodeNumber);
        }
        return ans;
    }

    private List<Integer> reticulationNodes() {
        List<Integer> ans = new ArrayList<>();
        for (int nodeNumber = this.treeNodesCount; nodeNumber < this.treeNodesCount + k; nodeNumber++) {
            ans.add(nodeNumber);
        }
        return ans;
    }

    private List<Integer> allNodesExceptLeaves() {
        List<Integer> ans = new ArrayList<>();
        for (int nodeNumber = n; nodeNumber < this.treeNodesCount + k; nodeNumber++) {
            ans.add(nodeNumber);
        }
        return ans;
    }

    private String getKey(String type, int ... params) {
        String key = type;
        for (int p : params) {
            key += "_" + p;
        }
        return key;
    }

    private int createVar(String type, int ... params) {
        m.put(getKey(type, params), m.size() + 1);
        return m.size();
    }

    private int getVar(String type, int ... params) {
        return m.get(getKey(type, params));
    }

    private void commentCNF(String format, Object... args) {
        if (!disableComments) {
            this.sb.append(String.format("c " + format + "\n", args));
        }
    }

    private void addClause(String clause) {
        if (!clause.endsWith(" ")) {
            clause += " ";
        }
        clause += "0\n";
        this.sb.append(clause);
        clausesCount++;
    }

    private void addClause(int ... literals) {
        String clause = "";
        for (int literal : literals) {
            clause += literal + " ";
        }
        addClause(clause);
    }
}
