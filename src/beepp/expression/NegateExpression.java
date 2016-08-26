package beepp.expression;

import beepp.util.Pair;

/**
 * @author Vyacheslav Moklev
 */
public class NegateExpression implements BooleanExpression, IntegerExpression {
    private Expression expr;

    public NegateExpression(Expression expr) {
        this.expr = expr;
    }

    @Override
    public int lowerBound() {
        // x ∈ [a, b] ⇒ -x ∈ [-b, -a]
        if (expr instanceof IntegerExpression)
            return -((IntegerExpression) expr).upperBound();
        else
            throw new IllegalStateException("Unsupported operation for boolean negation");
    }

    @Override
    public int upperBound() {
        if (expr instanceof IntegerExpression)
            return -((IntegerExpression) expr).lowerBound();
        else
            throw new IllegalStateException("Unsupported operation for boolean negation");
    }

    @Override
    public Pair<String, String> compile() {
        if (expr instanceof NegateExpression) {
            return ((NegateExpression) expr).expr.compile();
        } else {
            Pair<String, String> compiled = expr.compile();
            return new Pair<>(compiled.a, "-" + compiled.b);
        }
    }
}
