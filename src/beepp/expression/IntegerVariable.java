package beepp.expression;

import beepp.util.RangeUnion;

/**
 * @author Moklev Vyacheslav
 */
public class IntegerVariable extends Variable { // TODO implements IntegerExpression
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
}
