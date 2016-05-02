import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Vladimir Ulyantsev Date: 24.04.13 Time: 19:15
 */
public class CryptominisatPort {
	public static int counter = 0;
	public static String netData, addData;

	static void permute(List<String> arr, int k){
        for(int i = k; i < arr.size(); i++){
            java.util.Collections.swap(arr, i, k);
            permute(arr, k+1);
            java.util.Collections.swap(arr, k, i);
        }
        if (k == arr.size() -1){
        	String ans = netData + "c solve point for iterate solver\n";
			for (String s : arr) {
				ans += (s + "c solve point for iterate solver\n");
			}
			ans += (addData + "c solve point for iterate solver\n");
			File tmpFile = new File("tmp" + counter + ".cnf");
			PrintWriter tmpPW;
			try {
				tmpPW = new PrintWriter(tmpFile);
				tmpPW.print(ans);
				tmpPW.close();
				counter += 1;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
	

	public static boolean[] solve(String CNFString, PrintWriter CNFPrintWriter, PrintWriter solverPrintWriter,
			long timeLimit, long[] executionTime, String solverOptions) throws IOException {
		if (CNFPrintWriter != null) {
			CNFPrintWriter.println(CNFString);
			CNFPrintWriter.flush();
		}
		String[] pieces = CNFString.split("c solve point for iterate solver\n");
		netData = pieces[0];
		addData = pieces[pieces.length - 1];
		List<String> treesData = new ArrayList<String>();
		for (int i = 1; i <= pieces.length - 2; i++) {
			treesData.add(pieces[i]);
		}
		counter = 0;
		permute(treesData, 0);
		int nThreads = Math.min(20, counter);
		List<Integer> nFiles = new ArrayList<Integer>();
		for (int i = 0; i < counter; i++){
			nFiles.add(i);
		}
		long seed = System.nanoTime();
		Collections.shuffle(nFiles, new Random(seed));
		nFiles = nFiles.subList(0, nThreads);
		
		long curTime = System.currentTimeMillis();

		
		ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
		System.out.println(nFiles.toString());
		Set<Callable<String>> callables = new HashSet<Callable<String>>();
		for (int i = 0; i < nThreads; i++) {
			final String file = "tmp" + nFiles.get(i) + ".cnf";
			callables.add(new Callable<String>() {
				public String call() throws Exception {
					String command = solverOptions + " " + file;
					Runtime runtime = Runtime.getRuntime();
				    Process process = runtime.exec(command);
				    BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String ansLine = "";
					String line;
					while ((line = outputReader.readLine()) != null) {
						if (solverPrintWriter != null) {
							solverPrintWriter.println(line);
						}
						if (line.length() > 0 && line.charAt(0) == 'v') {
							ansLine += line.substring(2);
							if (!(line.charAt(line.length() - 1) == ' '))
								ansLine += " ";
						}
					}
					outputReader.close();
				    process.waitFor(timeLimit, TimeUnit.MILLISECONDS);
					return ansLine;
				}
			});
		}
		String ansLine = "";
		try {
			ansLine = executorService.invokeAny(callables);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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