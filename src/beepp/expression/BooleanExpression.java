package beepp.expression;

/**
 * @author Moklev Vyacheslav
 */
public interface BooleanExpression extends Expression {
    String holds();

    BooleanExpression and(BooleanExpression... expr);
    BooleanExpression or(BooleanExpression... expr);
    BooleanExpression xor(BooleanExpression... expr);
    BooleanExpression iff(BooleanExpression... expr);
    BooleanExpression then(BooleanExpression expr);
}
