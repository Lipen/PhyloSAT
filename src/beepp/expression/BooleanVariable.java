package beepp.expression;

import beepp.util.Pair;

import java.util.Map;

/**
 * @author Vyacheslav Moklev
 */
public class BooleanVariable extends Variable implements BooleanExpression {
    public BooleanVariable(String name) {
        super(name);
    }

    @Override
    public String getDeclaration() {
        return "new_bool(" + name + ")";
    }

    @Override
    public Pair<String, String> compile() {
        return new Pair<>("", name);
    }

    @Override
    public boolean eval(Map<String, Object> vars) {
        Object obj = vars.get(name);
        if (obj == null) {
            throw new IllegalArgumentException("There is no defined variable \"" + name + "\"");
        }
        try {
            return (boolean) obj;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Variable \"" + name + "\" is not boolean");
        }
    }
}
