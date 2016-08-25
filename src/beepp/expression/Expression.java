package beepp.expression;

import beepp.util.Pair;

/**
 * @author Vyacheslav Moklev
 */
public interface Expression {
    Pair<String, String> compile();
}
