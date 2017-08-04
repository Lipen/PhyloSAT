import java.util.Map;

abstract class Solver {
    abstract Map<String, Object> resolve(long timeLimit, long[] executionTime);
}
