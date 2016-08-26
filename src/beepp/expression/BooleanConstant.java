package beepp.expression;

import beepp.util.Pair;

/**
 * @author Vyacheslav Moklev
 */
public enum BooleanConstant implements BooleanExpression {
    FALSE(false), TRUE(true);

    private final boolean value;

    BooleanConstant(boolean value) {
        this.value = value;
    }

    @Override
    public Pair<String, String> compile() {
        return new Pair<>("", value + "");
    }
}
