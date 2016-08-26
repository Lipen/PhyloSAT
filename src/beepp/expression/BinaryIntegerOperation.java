package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Vyacheslav Moklev
 */
public class BinaryIntegerOperation implements IntegerExpression, BooleanExpression { // TODO split?
    private enum ResultType {
        INT("", "int"), BOOL("_reif", "bool");

        private final String modifier;
        private final String typeName;

        ResultType(String modifier, String typeName) {
            this.modifier = modifier;
            this.typeName = typeName;
        }
    }

    private IntegerExpression left, right;
    private String op;
    private ResultType resultType;

    public BinaryIntegerOperation (String op, IntegerExpression left, IntegerExpression right) {
        this.left = left;
        this.right = right;
        this.op = op;
        switch (op) {
            case "plus":
            case "times":
            case "div":
            case "mod":
            case "max":
            case "min":
                resultType = ResultType.INT;
                break;
            case "leq":
            case "geq":
            case "eq":
            case "lt":
            case "gt":
            case "neq":
                resultType = ResultType.BOOL;
        }
    }

    @Override
    public int lowerBound() {
        if (!resultType.typeName.equals("int"))
            throw new IllegalStateException("Operation is supported only for integer expression");
        switch (op) {
            case "plus":
                return left.lowerBound() + right.lowerBound();
            case "times":
                // TODO check correctness
                int a = left.lowerBound();
                int b = left.upperBound();
                int c = right.lowerBound();
                int d = right.upperBound();
                List<Integer> list = Arrays.asList(a * c, a * d, b * c, b * d);
                return Collections.min(list);
            case "div":
                throw new UnsupportedOperationException("Div is not supported for now");
                // TODO ask Amit about div's result, rounding mode: to xero, to closest, to lower
            case "mod":
                throw new UnsupportedOperationException("Mod is not supported for now");
                // TODO ask Amit about mod's result range
            case "max":
                return Math.max(left.lowerBound(), right.lowerBound());
            case "min":
                return Math.min(left.lowerBound(), right.lowerBound());
            default:
                throw new IllegalStateException("op is unknown: op = " + op);
        }
    }

    @Override
    public int upperBound() {
        if (!resultType.typeName.equals("int"))
            throw new IllegalStateException("Operation is supported only for integer expression");
        if (!resultType.typeName.equals("int"))
            throw new IllegalStateException("Operation is supported only for integer expression");
        switch (op) {
            case "plus":
                return left.upperBound() + right.upperBound();
            case "times":
                // TODO check correctness
                int a = left.lowerBound();
                int b = left.upperBound();
                int c = right.lowerBound();
                int d = right.upperBound();
                List<Integer> list = Arrays.asList(a * c, a * d, b * c, b * d);
                return Collections.max(list);
            case "div":
                throw new UnsupportedOperationException("Div is not supported for now");
                // TODO ask Amit about div's result, rounding mode: to xero, to closest, to lower
            case "mod":
                throw new UnsupportedOperationException("Mod is not supported for now");
                // TODO ask Amit about mod's result range
            case "max":
                return Math.max(left.upperBound(), right.upperBound());
            case "min":
                return Math.min(left.upperBound(), right.upperBound());
            default:
                throw new IllegalStateException("op is unknown: op = " + op);
        }
    }

    @Override
    public Pair<String, String> compile() {
        Pair<String, String> cLeft = left.compile();
        Pair<String, String> cRight = right.compile();
        String constraints = cLeft.a + (cLeft.a.isEmpty() ? "" : "\n")
                + cRight.a + (cRight.a.isEmpty() ? "" : "\n");
        String newVar = "temp" + StaticStorage.lastTempVar++;
        if (resultType == ResultType.INT) {
            constraints += "new_int(" + newVar + ", " + lowerBound() + ", " + upperBound() + ")\n";
        } else {
            constraints += "new_bool(" + newVar + ")\n";
        }
        constraints += "int_" + op + resultType.modifier + "(" + cLeft.b + ", " + cRight.b + ", " + newVar + ")";
        return new Pair<>(constraints, newVar);
    }
}
