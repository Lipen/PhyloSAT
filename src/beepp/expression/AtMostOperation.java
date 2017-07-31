package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AtMostOperation implements BooleanExpression {
    private final int m;
    private final List<BooleanExpression> list;

    public AtMostOperation(int m, List<BooleanExpression> list) {
        this.m = m;
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

        /* Old */
        // constraints.add("bool_array_sum_leq(" + names + ", " + m + ")");
        // return new Pair<>(constraints.stream().collect(Collectors.joining("\n")), null);

        /* Naive */
        List<String> namesInt = new ArrayList<>();
        for (String name : names) {
            String newInt = StaticStorage.newVar();
            constraints.add("bool2int(" + name + ", " + newInt + ")");
            namesInt.add(newInt);
        }

        String newIntSum = StaticStorage.newVar();
        constraints.add("new_int_dual(" + newIntSum + ", 0, " + namesInt.size() + ")");
        constraints.add("int_array_plus(" + namesInt + ", " + newIntSum + ")");

        String newVar = StaticStorage.newVar();
        constraints.add("new_bool(" + newVar + ")");
        constraints.add("int_leq_reif(" + newIntSum + ", " + m + ", " + newVar + ")");

        return new Pair<>(constraints.stream().collect(Collectors.joining("\n")), newVar);

        /* Reified */
        // String newVar = StaticStorage.newVar();
        // constraints.add("new_bool(" + newVar + ")");
        // constraints.add("bool_array_sum_leq_reif(" + names + ", " + m + ", " + newVar + ")");
        // return new Pair<>(constraints.stream().collect(Collectors.joining("\n")), newVar);
    }

    @Override
    public boolean eval(Map<String, Object> vars) {
        return list.stream()
                .map(e -> e.eval(vars))
                .filter(v -> v)
                .count() <= 1;
    }
}
