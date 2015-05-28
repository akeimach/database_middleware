
import java.io.*;
import java.util.*;
import java.lang.*;

public class test {

	public static void main(String[]args) throws InterruptedException, IOException {
		
		ProcessBuilder processBuilder = new ProcessBuilder("/Users/alyssakeimach/Eclipse/DBconnector/shuf.exe");        
		Process process = processBuilder.start();
		InputStream stream = process.getInputStream();
		BufferedReader output = new BufferedReader(new InputStreamReader(stream));
		String line = "";
		while ((line = output.readLine()) != null) { System.out.print(line + "\n"); }
		process.waitFor();
		
		
		/*
	

		//String command="ping ‐c 3 www.google.com";
		//String command = "gshuf AK_wrangler_test.csv";
		//String command = "ls";
		//String command = "gshuf airlines.dat > airlines_shuf.csv";
		String command = "/Users/alyssakeimach/Eclipse/DBconnector/shuf.exe";
		Process proc = Runtime.getRuntime().exec(command);
		//Readtheoutput
		proc.waitFor();
		
		//BufferedReader outs = new BufferedReader(OutputStream(proc.getOutputStream()));
		BufferedReader reader= new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line=""; 
		while((line=reader.readLine())!=null) {
			System.out.print(line+"\n"); 
		}
		
		*/
		
		
		
		
		
		
	}
}


	/*
	//PING EXAMPLE
	private String test(String command) { 
		StringBuffer output = new StringBuffer();
		Process p; 
		try {
			p = Runtime.getRuntime().exec(command); p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = reader.readLine())!= null) { output.append(line + "\n"); }
		} 
		catch (Exception e) { e.printStackTrace(); }
		return output.toString();
	}

	public static void main(String[] args) {
		test obj = new test();
		String domainName = "google.com";
		String command = "ping -c 3 " + domainName; 
		String output = obj.test(command); 
		System.out.println(output);
	}
	 */
	/*
	//DIRECT STRING EXAMPLE
	public static void main(String args[]) {
		try
		{
			Runtime rt = Runtime.getRuntime();
			String[] command = {"/Users/alyssakeimach/", "gshuf", "-o", "shuffle_airline.csv", "<", "airlines.dat"};
			Process proc = rt.exec(command);
			InputStream stdin = proc.getInputStream(); 
			InputStreamReader isr = new InputStreamReader(stdin); 
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ( (line = br.readLine()) != null) { System.out.println(line); }
			int exitVal = proc.waitFor(); 
			System.out.println("Process exitValue: " + exitVal);
		} 
		catch (Throwable t) { t.printStackTrace(); }
	} 
	*/
	/*
	//EXECUTABLE FILE EXAMPLE
	public static void main(String args[]) {
		try{
			String ss = null;
			Process p = Runtime.getRuntime().exec("/Users/alyssakeimach/cmd.exe");
			BufferedWriter writeer=new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			writeer.write("dir");
			writeer.flush();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream())); 
			BufferedReader stdError=new BufferedReader(new InputStreamReader(p.getErrorStream()));
			System.out.println("Hereisthestandardoutputofthecommand:\n"); 
			while((ss=stdInput.readLine()) != null) { System.out.println(ss); }
			System.out.println("Hereisthestandarderrorofthecommand(ifany):\n"); 
			while((ss=stdError.readLine())!=null){ System.out.println(ss); }
		}
		catch (IOException e) { System.out.println("FROMCATCH "+ e.toString()); }
	}
	*/

