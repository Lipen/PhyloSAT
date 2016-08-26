package beepp.expression;

/**
 * @author Vyacheslav Moklev
 */
public interface IntegerExpression extends Expression {

    int lowerBound();

    int upperBound();

    default IntegerExpression plus(IntegerExpression expr) {
        return new BinaryIntegerOperation("plus", this, expr);
    }

    default IntegerExpression times(IntegerExpression expr) {
        return new BinaryIntegerOperation("times", this, expr);
    }

    default IntegerExpression div (IntegerExpression expr) {
        return new BinaryIntegerOperation("div", this, expr);
    }

    default IntegerExpression mod(IntegerExpression expr) {
        return new BinaryIntegerOperation("mod", this, expr);
    }

    default IntegerExpression max(IntegerExpression expr) {
        return new BinaryIntegerOperation("max", this, expr);
    }

    default IntegerExpression min(IntegerExpression expr) {
        return new BinaryIntegerOperation("min", this, expr);
    }

    default IntegerExpression groupPlus(IntegerExpression... expr) {
        return new UniformIntegerOperation("plus", this, expr);
    }

    default IntegerExpression groupTimes(IntegerExpression... expr) {
        return new UniformIntegerOperation("times", this, expr);
    }

    default IntegerExpression groupMax(IntegerExpression... expr) {
        return new UniformIntegerOperation("max", this, expr);
    }

    default IntegerExpression groupMin(IntegerExpression... expr) {
        return new UniformIntegerOperation("min", this, expr);
    }

    default BooleanExpression equals(IntegerExpression expr) {
        return new BinaryIntegerOperation("eq", this, expr);
    }

    default BooleanExpression notEquals(IntegerExpression expr) {
        return new BinaryIntegerOperation("neq", this, expr);
    }

    default BooleanExpression lessEq(IntegerExpression expr) {
        return new BinaryIntegerOperation("leq", this, expr);
    }

    default BooleanExpression greaterEq(IntegerExpression expr) {
        return new BinaryIntegerOperation("geq", this, expr);
    }

    default BooleanExpression less(IntegerExpression expr) {
        return new BinaryIntegerOperation("lt", this, expr);
    }

    default BooleanExpression greater(IntegerExpression expr) {
        return new BinaryIntegerOperation("gt", this, expr);
    }

}
