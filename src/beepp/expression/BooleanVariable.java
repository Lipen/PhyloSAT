package beepp.expression;

import beepp.util.Pair;

/**
 * @author Moklev Vyacheslav
 */
public class BooleanVariable extends Variable implements BooleanExpression {
    public BooleanVariable(String name) {
        super(name);
    }

    @Override
    public Pair<String, String> compile() {
        return new Pair<>("", name);
    }
}
