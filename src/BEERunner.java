import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Moklev Vyacheslav
 */
public class BEERunner {
    private static final String BEE_PATH = "C:\\Users\\slava\\Downloads\\bee20160830\\";

    private static int execute(String... args) {
        try {
            Process p = Runtime.getRuntime().exec(BEE_PATH + "BumbleBEE " +
                    Arrays.stream(args).map(s -> "\"" + s + "\"").collect(Collectors.joining(" ")));
            int retCode = p.waitFor();
            System.out.println(
                    new BufferedReader(
                            new InputStreamReader(p.getInputStream())
                    ).lines().collect(Collectors.joining("\n"))
            );
            return retCode;
        } catch (IOException | InterruptedException e) {
            return -1;
        }
    }

    public static void makeDimacs(String source, String outDimacs, String outMap) {
        try {
            new PrintWriter(outDimacs).close();
            new PrintWriter(outMap).close();
            execute(source, "-dimacs", outDimacs, outMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
