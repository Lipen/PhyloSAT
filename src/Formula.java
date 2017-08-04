import java.io.*;

class Formula {
    private final String formula;

    Formula(String formula) {
        this.formula = formula;
    }

    void dump(String filename) {
        try (PrintWriter out = new PrintWriter(filename)) {
            out.println(formula);
        } catch (IOException e) {
            System.err.println("[!] So sad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return formula;
    }
}
