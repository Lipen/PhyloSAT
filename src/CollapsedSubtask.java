class CollapsedSubtask extends Subtask {
    private final Tree subtree;

    CollapsedSubtask(Tree subtree) {
        this.subtree = subtree;
    }

    @Override
    String getLabel() {
        return String.join("+", subtree.getLabels());
    }

    @Override
    void solve(SolveParameters p) {
        answer = new Network(subtree);
    }
}
