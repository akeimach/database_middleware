import java.util.*;
import java.io.*;

public class MediocreExecJavac {
	
	public static void main(String args[]) {
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec("javac");
			InputStream stderr = proc.getErrorStream(); InputStreamReader isr = new InputStreamReader(stderr); BufferedReader br = new BufferedReader(isr);
			String line = null;
			System.out.println("<ERROR>");
			while ( (line = br.readLine()) != null)
				System.out.println(line); System.out.println("</ERROR>");
			int exitVal = proc.waitFor(); System.out.println("Process exitValue: " + exitVal);
		} 
		catch (Throwable t) { 
			t.printStackTrace(); 
		}
	}
}