package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

import java.util.Map;

/**
 * @author Vyacheslav Moklev
 */
public class BinaryIntBooleanOperation implements BooleanExpression { 
    private IntegerExpression left, right;
    private String op;

    public BinaryIntBooleanOperation(String op, IntegerExpression left, IntegerExpression right) {
        this.left = left;
        this.right = right;
        this.op = op;
        switch (op) {
            case "leq":
            case "geq":
            case "eq":
            case "lt":
            case "gt":
            case "neq":
                break;
            default:
                throw new IllegalArgumentException("Unknown op: \"" + op + "\"");
        }
    }

    @Override
    public Pair<String, String> compile() {
        Pair<String, String> cLeft = left.compile();
        Pair<String, String> cRight = right.compile();
        String constraints = cLeft.a + (cLeft.a.isEmpty() ? "" : "\n")
                + cRight.a + (cRight.a.isEmpty() ? "" : "\n");
        String newVar = StaticStorage.newVar();
        constraints += "new_bool(" + newVar + ")\n";
        constraints += "int_" + op + "_reif(" + cLeft.b + ", " + cRight.b + ", " + newVar + ")";
        return new Pair<>(constraints, newVar);
    }

    @Override
    public String holds() {
        Pair<String, String> cLeft = left.compile();
        Pair<String, String> cRight = right.compile();
        String constraints = cLeft.a + (cLeft.a.isEmpty() ? "" : "\n")
                + cRight.a + (cRight.a.isEmpty() ? "" : "\n");
        constraints += "int_" + op + "(" + cLeft.b + ", " + cRight.b + ")";
        return constraints;
    }

    @Override
    public boolean eval(Map<String, Object> vars) {
        int leftValue = left.eval(vars);
        int rightValue = right.eval(vars);
        switch (op) {
            case "leq":
                return leftValue <= rightValue;
            case "geq":
                return leftValue >= rightValue;
            case "eq":
                return leftValue == rightValue;
            case "lt":
                return leftValue < rightValue;
            case "gt":
                return leftValue > rightValue;
            case "neq":
                return leftValue != rightValue;
            default:
                throw new IllegalArgumentException("Unknown op: \"" + op + "\"");
        }
    }
}
