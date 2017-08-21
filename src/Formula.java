import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

class Formula {
    private final List<String> clauses;

    Formula(List<String> clauses) {
        this.clauses = clauses;
    }


    List<String> getClauses() {
        return clauses;
    }

    void dump(String filename) {
        System.out.println("[.] Dumping formula to <" + filename + ">");
        try (PrintWriter out = new PrintWriter(filename)) {
            out.println(this.toString());
        } catch (IOException e) {
            System.err.println("[!] So sad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return String.join(System.lineSeparator(), clauses);
    }
}
