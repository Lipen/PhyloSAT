package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Moklev
 */
public class BinaryIntegerOperation implements IntegerExpression{ 
    private IntegerExpression left, right;
    private String op;

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
                break;
            default:
                throw new IllegalArgumentException("Unsupported op: \"" + op + "\"");
        }
    }

    @Override
    public int lowerBound() {
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
                // TODO ask Amit about div's result, rounding mode: to zero, to closest, to lower
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
                // TODO ask Amit about div's result, rounding mode: to zero, to closest, to lower
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
        constraints += "new_int(" + newVar + ", " + lowerBound() + ", " + upperBound() + ")\n";
        constraints += "int_" + op + "(" + cLeft.b + ", " + cRight.b + ", " + newVar + ")";
        return new Pair<>(constraints, newVar);
    }

    @Override
    public int eval(Map<String, Object> vars) {
        int leftValue = left.eval(vars);
        int rightValue = right.eval(vars);
        switch (op) {
            case "plus":
                return leftValue + rightValue;
            case "times":
                return leftValue * rightValue;
            case "div":
                return leftValue / rightValue;
            case "mod":
                return leftValue % rightValue;
            case "max":
                return Math.max(leftValue, rightValue);
            case "min":
                return Math.min(leftValue, rightValue);
            default:
                throw new IllegalArgumentException("Unsupported op: \"" + op + "\"");
        }
    }
}
