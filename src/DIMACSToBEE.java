import jdk.internal.util.xml.impl.Input;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Moklev Vyacheslav
 */
public class DIMACSToBEE {

    /**
     * Converts cnf formula from DIMACS to BEE format
     *
     * @param is InputStream with cnf formula in DIMACS format
     * @param os OutputStream to write resulting sat-instance in BEE format
     */
    public static void convert(InputStream is, OutputStream os) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));

        List<String> list = br.lines()
                .filter(s -> !s.startsWith("c"))
                .collect(Collectors.toList());

        int nbVars = Integer.parseInt(list.get(0).split(" ")[2]);
        int nbClauses = Integer.parseInt(list.get(0).split(" ")[3]);
        for (int i = 1; i <= nbVars; i++) {
            pw.println("new_bool(v" + i + ")");
        }
        for (int i = 1; i <= nbClauses; i++) {
            pw.println("new_bool(w" + i + ")");
        }

        for (int i = 1; i < list.size(); i++) {
            String line = list.get(i);
            String clause = Arrays.stream(line.split(" "))
                    .filter(s -> !s.equals("0"))
                    .map(s -> s.startsWith("-") ? "-v" + s.substring(1) : "v" + s)
                    .collect(Collectors.joining(", "));
            pw.println("bool_array_or_reif([" + clause + "], w" + i + ")");
        }

        String allClauses = IntStream.range(1, nbClauses + 1)
                .mapToObj(x -> "w" + x)
                .collect(Collectors.joining(", "));
        pw.println("bool_array_and([" + allClauses + "])");

        pw.println("solve satisfy");

        pw.flush();
    }

    public static void main(String[] args) throws IOException {
        InputStream is = new FileInputStream("cnf");
        OutputStream os = new FileOutputStream("bee");

        DIMACSToBEE.convert(is, os);

        is.close();
        os.close();
    }

}
