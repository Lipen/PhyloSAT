package beepp.expression;

import beepp.util.Pair;
import beepp.util.RangeUnion;

import java.util.Map;

/**
 * @author Vyacheslav Moklev
 */
public class IntegerVariable extends Variable implements IntegerExpression {
    private final RangeUnion domain;
    private final boolean isDual;

    public IntegerVariable(String name, RangeUnion domain, boolean isDual) {
        super(name);
        this.domain = domain;
        this.isDual = isDual;
    }

    public IntegerVariable(String name, int lowerBound, int upperBound, boolean isDual) {
        this(name, new RangeUnion(lowerBound, upperBound), isDual);
    }

    public IntegerVariable(String name, int lowerBound, int upperBound) {
        this(name, lowerBound, upperBound, false);
    }

    public IntegerVariable(String name, RangeUnion domain) {
        this(name, domain, false);
    }

    @Override
    public String getDeclaration() {
        String decl = "new_int";
        if (domain.isAtomicRange()) {
            if (isDual) decl += "_dual";
            decl += "(" + name + ", " + domain.lowerBound() + ", " + domain.upperBound() + ")";
        } else {
            decl += "(" + name + ", " + domain + ")";
            if (isDual) decl += "\nchannel_int2direct(" + name + ")";
        }
        return decl;
    }

    @Override
    public int lowerBound() {
        return domain.lowerBound();
    }

    @Override
    public int upperBound() {
        return domain.upperBound();
    }

    @Override
    public Pair<String, String> compile() {
        return new Pair<>("", name);
    }

    @Override
    public int eval(Map<String, Object> vars) {
        Object obj = vars.get(name);
        if (obj == null) {
            throw new IllegalArgumentException("There is no defined variable \"" + name + "\"");
        }
        try {
            return (int) obj;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Variable \"" + name + "\" is not int");
        }
    }
}
