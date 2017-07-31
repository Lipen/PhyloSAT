package beepp.expression;

import beepp.util.Pair;

/**
 * @author Vyacheslav Moklev
 */
interface Expression {
    Pair<String, String> compile();
}
