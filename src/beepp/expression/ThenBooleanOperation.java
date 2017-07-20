package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

import java.util.Map;

/**
 * @author Vyacheslav Moklev
 */
public class ThenBooleanOperation implements BooleanExpression {
    private BooleanExpression from, to;

    public ThenBooleanOperation(BooleanExpression from, BooleanExpression to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Pair<String, String> compile() {
        Pair<String, String> cFromNeg = new NegateBooleanExpression(from).compile();
        Pair<String, String> cTo = to.compile();
        String constraints = cFromNeg.a + (cFromNeg.a.isEmpty() ? "" : "\n")
                + cTo.a + (cTo.a.isEmpty() ? "" : "\n");
        String newVar = StaticStorage.newVar();
        constraints += "new_bool(" + newVar + ")\n";
        constraints += "bool_array_or_reif([" + cFromNeg.b + ", " + cTo.b + "], " + newVar + ")";
        return new Pair<>(constraints, newVar);
    }

    @Override
    public String holds() {
        Pair<String, String> cFromNeg = new NegateBooleanExpression(from).compile();
        Pair<String, String> cTo = to.compile();
        String constraints = cFromNeg.a + (cFromNeg.a.isEmpty() ? "" : "\n")
                + cTo.a + (cTo.a.isEmpty() ? "" : "\n");
        constraints += "bool_array_or([" + cFromNeg.b + ", " + cTo.b + "])";
        return constraints;
    }
    
    @Override
    public boolean eval(Map<String, Object> vars) {
        return !from.eval(vars) || to.eval(vars);
    }
}
