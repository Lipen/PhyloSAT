package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Vyacheslav Moklev
 */
public class UniformBooleanOperation implements BooleanExpression {
    private List<BooleanExpression> list;
    private String op; // supported operations: and, or, xor, iff 

    public UniformBooleanOperation(String op, BooleanExpression first, BooleanExpression... rest) {
        this.op = op;
        list = new ArrayList<>();
        list.add(first);
        list.addAll(Arrays.asList(rest));
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
        String newVar = StaticStorage.newVar();
        constraints.add("new_bool(" + newVar + ")");
        constraints.add("bool_array_" + op + "_reif(" + names + ", " + newVar + ")");
        return new Pair<>(constraints.stream().collect(Collectors.joining("\n")), newVar);
    }

    @Override
    public String holds() {
        List<String> constraints = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (BooleanExpression expr : list) {
            Pair<String, String> compiled = expr.compile();
            if (!compiled.a.isEmpty())
                constraints.add(compiled.a);
            names.add(compiled.b);
        }
        constraints.add("bool_array_" + op + "(" + names + ")");
        return constraints.stream()
                .collect(Collectors.joining("\n"));
    }

    @Override
    public boolean eval(Map<String, Object> vars) {
        Stream<Boolean> stream = list.stream()
                .map(e -> e.eval(vars));
        switch (op) {
            case "and":
                return stream.allMatch(v -> v);
            case "or":
                return stream.anyMatch(v -> v);
            case "xor":
                return stream.reduce(false, (a, b) -> a ^ b);
            case "iff":
                return stream.distinct().count() == 1;
            default:
                throw new IllegalArgumentException("Unknown op: \"" + op + "\"");
        }
    }
}
