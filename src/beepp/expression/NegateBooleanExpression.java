package beepp.expression;

import beepp.util.Pair;

import java.util.Map;

/**
 * @author Vyacheslav Moklev
 */
public class NegateBooleanExpression implements BooleanExpression {
    private BooleanExpression expr;

    public NegateBooleanExpression(BooleanExpression expr) {
        this.expr = expr;
    }

    @Override
    public Pair<String, String> compile() {
        if (expr instanceof NegateBooleanExpression) {
            return ((NegateBooleanExpression) expr).expr.compile();
        } else {
            Pair<String, String> compiled = expr.compile();
            return new Pair<>(compiled.a, "-" + compiled.b);
        }
    }

    @Override
    public boolean eval(Map<String, Object> vars) {
        return !expr.eval(vars);
    }
}
