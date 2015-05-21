import java.util.*;
import java.io.*;


public class TestExec
{
	public static void main(String args[]) {
		if (args.length < 1) {
			System.out.println("USAGE: java TestExec \"cmd\"");
			System.exit(1); }
		try
		{
			String cmd = args[0];
			Runtime rt = Runtime.getRuntime(); Process proc = rt.exec(cmd);
			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERR");
			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUT");
			// kick them off
			errorGobbler.start(); outputGobbler.start();
			// any error???
			int exitVal = proc.waitFor();
			System.out.println("ExitValue: " + exitVal); } catch (Throwable t)
			{ t.printStackTrace();
			} }
}