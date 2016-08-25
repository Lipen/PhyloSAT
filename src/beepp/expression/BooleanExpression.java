package beepp.expression;

import beepp.util.Pair;

/**
 * @author Vyacheslav Moklev
 */
public interface BooleanExpression extends Expression {
    default String holds() {
        Pair<String, String> compiled = compile();
        return compiled.a + (compiled.a.isEmpty() ? "" : "\n") + "bool_eq(" + compiled.b + ", true)";
    }

    default BooleanExpression and(BooleanExpression... exprs) {
        return new UniformBooleanOperation("and", this, exprs);
    }

    default BooleanExpression or(BooleanExpression... exprs) {
        return new UniformBooleanOperation("or", this, exprs);
    }

    default BooleanExpression xor(BooleanExpression... exprs) {
        return new UniformBooleanOperation("xor", this, exprs);
    }

    default BooleanExpression iff(BooleanExpression... exprs) {
        return new UniformBooleanOperation("iff", this, exprs);
    }

    default BooleanExpression then(BooleanExpression expr) {
        return new ThenBooleanOperation(this, expr);
    }
}
