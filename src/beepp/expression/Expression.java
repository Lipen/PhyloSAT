package beepp.expression;

import beepp.util.Pair;

/**
 * @author Моклев Вячеслав
 */
public interface Expression {
    Pair<String, String> compile();
}
