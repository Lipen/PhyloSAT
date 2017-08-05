import java.util.UUID;

abstract class Subtask {
    protected final UUID uuid = UUID.randomUUID();
    Network answer;  // The best network found so far

    abstract String getLabel();

    abstract void solve(SolveParameters p);
}
