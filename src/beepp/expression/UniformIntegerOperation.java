package beepp.expression;

import beepp.StaticStorage;
import beepp.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Vyacheslav Moklev
 */
public class UniformIntegerOperation implements IntegerExpression {
    private List<IntegerExpression> list;
    private String op; // available: plus, times, min, max

    public UniformIntegerOperation(String op, IntegerExpression first, IntegerExpression... rest) {
        this.op = op;
        list = new ArrayList<>();
        list.add(first);
        list.addAll(Arrays.asList(rest));
    }

    @Override
    public int lowerBound() {
        List<Integer> lowerBounds = list.stream()
                .map(IntegerExpression::lowerBound)
                .collect(Collectors.toList());
        switch (op) {
            case "plus":
                return lowerBounds.stream()
                        .mapToInt(Integer::intValue)
                        .sum();
            case "times": // TODO do precise lower bound?
                if (lowerBounds.stream().anyMatch(x -> x < 0)) {
                    return lowerBounds.stream()
                            .mapToInt(Math::abs)
                            .reduce(-1, (x, y) -> x * y); // [a1, b1] * ... * [an, bn] >= - |max1| * ... * |maxn|, where |maxk| = max{|x|, x ∈ [ak, bk]}
                } else { // all positive => precise lower bound [a1, b1] * ... * [an, bn] >= a1 * ... * an
                    return lowerBounds.stream()
                            .map(Integer::intValue)
                            .reduce(1, (x, y) -> x * y);
                }
            case "min":
                //noinspection OptionalGetWithoutIsPresent : stream is never empty
                return lowerBounds.stream()
                        .mapToInt(Integer::intValue)
                        .min().getAsInt();
            case "max":
                //noinspection OptionalGetWithoutIsPresent : stream is never empty
                return lowerBounds.stream()
                        .mapToInt(Integer::intValue)
                        .max().getAsInt();
            default:
                throw new IllegalStateException("op is unknown: op = " + op);
        }
    }

    @Override
    public int upperBound() {
        List<Integer> upperBounds = list.stream()
                .map(IntegerExpression::upperBound)
                .collect(Collectors.toList());
        switch (op) {
            case "plus":
                return upperBounds.stream()
                        .mapToInt(Integer::intValue)
                        .sum();
            case "times": // FIXME very bad upper bound
                return upperBounds.stream()
                        .mapToInt(Math::abs)
                        .reduce(1, (x, y) -> x * y); // [a1, b1] * ... * [an, bn] <= |max1| * ... * |maxn|, where |maxk| = max{|x|, x ∈ [ak, bk]}
            case "min":
                //noinspection OptionalGetWithoutIsPresent : stream is never empty
                return upperBounds.stream()
                        .mapToInt(Integer::intValue)
                        .min().getAsInt();
            case "max":
                //noinspection OptionalGetWithoutIsPresent : stream is never empty
                return upperBounds.stream()
                        .mapToInt(Integer::intValue)
                        .max().getAsInt();
            default:
                throw new IllegalStateException("op is unknown: op = " + op);
        }
    }

    @Override
    public Pair<String, String> compile() {
        List<String> constraints = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (IntegerExpression expr : list) {
            Pair<String, String> compiled = expr.compile();
            if (!compiled.a.isEmpty())
                constraints.add(compiled.a);
            names.add(compiled.b);
        }
        String newVar = StaticStorage.newVar();
        constraints.add("new_int(" + newVar + ", " + lowerBound() + ", " + upperBound() + ")");
        constraints.add("int_array_" + op + "(" + names + ", " + newVar + ")");
        return new Pair<>(constraints.stream().collect(Collectors.joining("\n")), newVar);
    }

    @Override
    public int eval(Map<String, Object> vars) {
        IntStream stream = list.stream()
                .mapToInt(e -> e.eval(vars));
        switch (op) {
            case "plus":
                return stream.sum();
            case "times":
                return stream.reduce(1, (a, b) -> a * b);
            case "min":
                return stream.min().orElseThrow(() -> 
                        new IllegalArgumentException("Minimum of empty list"));
            case "max":
                return stream.max().orElseThrow(() ->
                        new IllegalArgumentException("Maximum of empty list"));
            default:
                throw new IllegalArgumentException("Unknown op: \"" + op + "\"");
        }
    }
}
