package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

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
    public Pair<String, String> compile() {
        Pair<String, String> cLeft = left.compile();
        Pair<String, String> cRight = right.compile();
        String constraints = cLeft.a + (cLeft.a.isEmpty() ? "" : "\n")
                + cRight.a + (cRight.a.isEmpty() ? "" : "\n");
        String newVar = "temp" + StaticStorage.lastTempVar++;
        constraints += "new_" + resultType.typeName + "(" + newVar + ")\n";
        constraints += "int_" + op + resultType.modifier + "(" + cLeft.b + ", " + cRight.b + ", " + newVar + ")";
        return new Pair<>(constraints, newVar);
    }
}
