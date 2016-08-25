package beepp.expression;

import beepp.util.Pair;

/**
 * @author Моклев Вячеслав
 */
public interface IntegerExpression extends Expression {
    IntegerExpression plus(IntegerExpression expr);
    IntegerExpression times(IntegerExpression expr);
    IntegerExpression div (IntegerExpression expr);
    IntegerExpression mod(IntegerExpression expr);
    IntegerExpression max(IntegerExpression expr);
    IntegerExpression min(IntegerExpression expr);

    BooleanExpression equals(IntegerExpression expr);
    BooleanExpression notEquals(IntegerExpression expr);
    BooleanExpression lessEq(IntegerExpression expr);
    BooleanExpression greaterEq(IntegerExpression expr);
    BooleanExpression less(IntegerExpression expr);
    BooleanExpression greater(IntegerExpression expr);
}
