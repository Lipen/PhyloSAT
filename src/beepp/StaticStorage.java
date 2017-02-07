package beepp;

import beepp.expression.Variable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vyacheslav Moklev
 */
public class StaticStorage {
    public static int lastTempVar;
    public static Map<String, Variable> vars;
}
