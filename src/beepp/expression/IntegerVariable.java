package beepp.expression;

import beepp.util.Pair;
import beepp.util.RangeUnion;

import java.util.Map;

/**
 * @author Vyacheslav Moklev
 */
public class IntegerVariable extends Variable implements IntegerExpression {
    private RangeUnion domain;
    private boolean isDual;

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
        if (isDual) {
            if (domain.isAtomicRange()) {
                return "new_int_dual(" + name + ", " + domain.lowerBound() + ", " + domain.upperBound() + ")";
            } else {
                return "new_int(" + name + ", " + domain + ")\n" +
                        "channel_int2direct(" + name + ")";
            }
        } else {
            if (domain.isAtomicRange()) {
                return "new_int(" + name + ", " + domain.lowerBound() + ", " + domain.upperBound() + ")";
            } else {
                return "new_int(" + name + ", " + domain + ")";
            }
        }
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
        try {
            return (int) obj;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Variable \"" + name + "\" is not int");
        }
    }
}
