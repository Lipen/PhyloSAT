import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CollapsedSubtask extends Subtask {
    private final Tree subtree;


    CollapsedSubtask(Tree subtree) {
        this.subtree = subtree;
    }


    Tree getSubtree() {
        return subtree;
    }

    @Override
    int getN() {
        return subtree.getTaxaSize();
    }

    @Override
    String getLabel() {
        return String.join("+", subtree.getLabels());
    }

    @Override
    void solve(SolveParameters p) {
        answer = new Network(this);
        answers = new ArrayList<>();
        answers.add(answer);
    }

    @Override
    List<String> reprData() {
        return Stream.of(subtree.repr())
                .collect(Collectors.toList());
    }
}
