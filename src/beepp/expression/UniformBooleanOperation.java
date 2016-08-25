package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vyacheslav Moklev
 */
public class UniformBooleanOperation implements BooleanExpression {
    private List<BooleanExpression> list;
    private String op;

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
        for (BooleanExpression expr: list) {
            Pair<String, String> compiled = expr.compile();
            if (!compiled.a.isEmpty())
                constraints.add(compiled.a);
            names.add(compiled.b);
        }
        String newVar = "temp" + StaticStorage.lastTempVar++;
        constraints.add("new_bool(" + newVar + ")");
        constraints.add("bool_array_" + op + "_reif(" + names + ", " + newVar + ")");
        return new Pair<>(constraints.stream().collect(Collectors.joining("\n")), newVar);
    }
}
