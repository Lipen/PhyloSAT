import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Moklev Vyacheslav
 */
public class BEERunner {
    private static final String BEE_PATH = "C:\\Users\\slava\\Downloads\\bee20160827\\";

    private static int execute(String... args) {
        try {
            Process p = Runtime.getRuntime().exec(BEE_PATH + "BumbleBEE " +
                    Arrays.stream(args).map(s -> "\"" + s + "\"").collect(Collectors.joining(" ")));
            return p.waitFor();
        } catch (IOException | InterruptedException e) {
            return -1;
        }
    }

    public static void makeDimacs(String source, String outDimacs, String outMap) {
        execute(source, "-dimacs", outDimacs, outMap);
    }
}
