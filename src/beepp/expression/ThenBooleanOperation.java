package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

import java.util.Map;

/**
 * @author Vyacheslav Moklev
 */
public class ThenBooleanOperation implements BooleanExpression {
    private BooleanExpression from, to;

    public ThenBooleanOperation(BooleanExpression from, BooleanExpression to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Pair<String, String> compile() {
        Pair<String, String> cFrom = from.compile();
        Pair<String, String> cTo = to.compile();
        String constraints = cFrom.a + (cFrom.a.isEmpty() ? "" : "\n")
                + cTo.a + (cTo.a.isEmpty() ? "" : "\n");
        String newVar = "temp" + StaticStorage.lastTempVar++;
        constraints += "new_bool(" + newVar + ")\n";
        constraints += "bool_array_or_reif([-" + cFrom.b + ", " + cTo.b + "], " + newVar + ")";
        return new Pair<>(constraints, newVar);
    }

    @Override
    public boolean eval(Map<String, Object> vars) {
        return !from.eval(vars) || to.eval(vars);
    }
}
