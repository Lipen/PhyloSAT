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
                                  long[] executionTime,
                                  String solverOptions) throws IOException {
        if (CNFPrintWriter != null) {
            CNFPrintWriter.println(CNFString);
            CNFPrintWriter.flush();
        }

        File tmpFile = new File("tmp.cnf");
        PrintWriter tmpPW = new PrintWriter(tmpFile);
        tmpPW.print(CNFString);
        tmpPW.close();

        String command = solverOptions + " tmp.cnf";
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
        int[] model = new int[splitAns.length];
		for (int i = 0; i < splitAns.length; i++) {
			model[i] = Integer.parseInt(splitAns[i]);
		}
		int ansLength = model[model.length - 2];
        boolean[] ans = new boolean[ansLength];
        for (int m : model) {
        	if (!(m == 0)) {
        		ans[Math.abs(m) - 1] = m > 0;
        	}
        }
        return ans;
    }
}
