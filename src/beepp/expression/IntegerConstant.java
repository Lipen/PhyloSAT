package beepp.expression;

import beepp.util.Pair;

/**
 * @author Vyacheslav Moklev
 */
public class IntegerConstant implements IntegerExpression {
    private int value;

    public IntegerConstant(int value) {
        this.value = value;
    }

    @Override
    public int lowerBound() {
        return value;
    }

    @Override
    public int upperBound() {
        return value;
    }

    @Override
    public Pair<String, String> compile() {
        return new Pair<>("", value + "");
    }
}
