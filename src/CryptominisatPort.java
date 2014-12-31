import org.apache.commons.exec.*;

import java.io.*;
import java.util.Scanner;

/**
 * Vladimir Ulyantsev
 * Date: 24.04.13
 * Time: 19:15
 */
public class CryptominisatPort {
    public static boolean[] solve(String CNFString,
                                  PrintWriter CNFPrintWriter,
                                  PrintWriter solverPrintWriter,
                                  long timeLimit,
                                  long[] executionTime) throws IOException {
        if (CNFPrintWriter != null) {
            CNFPrintWriter.println(CNFString);
            CNFPrintWriter.flush();
        }

        File tmpFile = new File("tmp.cnf");
        PrintWriter tmpPW = new PrintWriter(tmpFile);
        tmpPW.print(CNFString);
        tmpPW.close();

        //Process p = Runtime.getRuntime().exec("cryptominisat --threads=4 tmp.cnf");

        String command = "soft/cryptominisat --threads=4 tmp.cnf";
        CommandLine cmdLine = CommandLine.parse(command);

        DefaultExecutor executor = new DefaultExecutor();
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        executor.setExitValue(20);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeLimit);
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errStream);
        executor.setStreamHandler(streamHandler);

        long curTime = System.currentTimeMillis();
        try {
            executor.execute(cmdLine, resultHandler);
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executionTime[0] = System.currentTimeMillis() - curTime;
        if (executionTime[0] > timeLimit) {
            executionTime[0] = -1;
        }

        String ansLine = "";
        Scanner input = new Scanner(outputStream.toString());
        //System.out.println(outputStream.toString());
        //System.out.println(errStream.toString());
        String line;
        while (input.hasNextLine()) {
            line = input.nextLine();
            if (solverPrintWriter != null) {
                solverPrintWriter.println(line);
            }
            if (line.charAt(0) == 'v') {
                ansLine += line.substring(2);
            }
        }
        input.close();
        tmpFile.delete();

        if (ansLine.isEmpty()) {
            return null;
        }

        String[] splitAns = ansLine.split(" ");
        boolean[] ans = new boolean[splitAns.length - 1];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = !splitAns[i].contains("-");
        }
        return ans;
    }
}
