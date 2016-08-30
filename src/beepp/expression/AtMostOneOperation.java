package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Moklev Vyacheslav
 */
public class AtMostOneOperation implements BooleanExpression {
    private List<BooleanExpression> list;

    public AtMostOneOperation(List<BooleanExpression> list) {
        this.list = list;
    }

    @Override
    public Pair<String, String> compile() {
        List<String> constraints = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (BooleanExpression expr : list) {
            Pair<String, String> compiled = expr.compile();
            if (!compiled.a.isEmpty())
                constraints.add(compiled.a);
            names.add(compiled.b);
        }
        constraints.add("bool_array_sum_leq(" + names + ", 1)");
        return new Pair<>(constraints.stream().collect(Collectors.joining("\n")), null);
    }
}
