abstract class Subtask {
    Network answer;  // The best network found so far

    abstract String getLabel();

    abstract void solve(SolveParameters p);
}
