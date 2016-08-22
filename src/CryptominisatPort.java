import org.apache.commons.exec.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Vladimir Ulyantsev Date: 24.04.13 Time: 19:15
 */
public class CryptominisatPort {
	public static boolean[] solve(String CNFString, PrintWriter CNFPrintWriter, PrintWriter solverPrintWriter,
			long timeLimit, long[] executionTime, String solverOptions) throws IOException {
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
        if (resultHandler.getException() != null) {
            System.err.println(">>> !!! <<<");
            resultHandler.getException().printStackTrace();
//            System.exit(1);
        }

		executionTime[0] = System.currentTimeMillis() - curTime;
		if (executionTime[0] > timeLimit) {
			executionTime[0] = -1;
			String cnf_k_num = CNFString.split("\n")[1];
			cnf_k_num = cnf_k_num.split(" ")[6];
			cnf_k_num = cnf_k_num.substring(0, cnf_k_num.length() - 1);
			File unresolved_cnf = new File("cnf_unresolved_k_num_" + cnf_k_num);
			PrintWriter unr_cnf = new PrintWriter(unresolved_cnf);
			unr_cnf.print(CNFString);
			unr_cnf.close();

		}

		String ansLine = "";
		Scanner input = new Scanner(outputStream.toString());
		String line;
		while (input.hasNextLine()) {
			line = input.nextLine();
			if (solverPrintWriter != null) {
				solverPrintWriter.println(line);
			}
			if (line.length() > 0 && line.charAt(0) == 'v') {
				ansLine += line.substring(2);
				if (!(line.charAt(line.length() - 1) == ' '))
					ansLine += " ";
			}
		}
		input.close();
		// tmpFile.delete();

		if (ansLine.isEmpty()) {
			return null;
		}

		String[] splitAns = ansLine.split(" ");
		ArrayList<Integer> model = new ArrayList<Integer>();
		int max_c = 0;
		for (int i = 0; i < splitAns.length; i++) {
			int c = Integer.parseInt(splitAns[i]);
			model.add(c);
			max_c = Math.max(Math.abs(c), max_c);
		}
		boolean[] ans = new boolean[max_c];
		for (int i = 0; i < ans.length; i++) {
			ans[i] = true;
		}
		for (int m : model) {
			if (!(m == 0)) {
				ans[Math.abs(m) - 1] = m > 0;
			}
		}
		return ans;
	}
}
