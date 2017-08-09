import java.util.UUID;

abstract class Subtask {
    protected final UUID uuid = UUID.randomUUID();
    Network answer;  // The best network found so far

    abstract int getN();

    abstract String getLabel();

    abstract void solve(SolveParameters p);

    @Override
    public String toString() {
        return String.format("{Subtask <%s> n=%d}", getLabel(), getN());
    }
}
