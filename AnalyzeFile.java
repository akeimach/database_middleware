import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class AnalyzeFile extends LoadData {

	public static boolean hasTitle = true;
	public static String[] defaultFields;
	public static String[] defaultTypes;
	public static String[] sampleVals;
	public static int[] defaultSize;
	public static int numCols;
	static String INT = "[\\+\\-]?\\d+";
	static String FLOAT = "[\\+\\-]?\\d+\\.\\d+(?:[eE][\\+\\-]?\\d+)?";
	static String CHAR = "[^0-9]";
	static String DDMMYYYY = "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)";
	static String MMDDYYYY = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d)";
	static String MMDDYY = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/(\\d\\d)";
	static String HOUR24 = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	static String HOUR12 = "(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)";
	static String IPADDRESS = "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	static String EMAIL = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";

	public static void getFormat(File file) throws IOException {
		//FIRST GET THE DELIMITER
		// Algorithm: count every ,;/ and tab, see which one is used most often
		BufferedReader lines = new BufferedReader(new FileReader(LoadData.file));
		char[] delimiters = { ',', '/', ' ', ';', '\t', '\n' };
		int[] counters = { 0, 0, 0, 0, 0, 0 };
		String topLine = lines.readLine(); //top line just in case has titles
		for (int linenum = 0; linenum < 15; linenum++) {
			String curr = lines.readLine();
			for (int i = 0; i < curr.length(); i++) {
				char c = curr.charAt(i);
				for (int d = 0; d < delimiters.length; d++) {
					if (c == delimiters[d]) { counters[d]++; }
				}
			}
		}
		int max = 0;
		int maxindex = 0;
		for (int i = 0; i < counters.length; i++) {
			if (max < counters[i]) { 
				max = counters[i]; 
				maxindex = i; 
			}
		}
		System.out.println("'" + delimiters[maxindex] + "' is the delimiter, it appeared " + max/15 + " times per line");
		delimiter = delimiters[maxindex];
		//NEXT GET THE FIELDS
		numCols = (max / 15) + 1;
		defaultFields = new String[numCols];
		defaultTypes = new String[numCols];
		defaultSize = new int[numCols];
		sampleVals = new String[numCols];
		int index = 0;
		int start = 0;
		int end = 0;
		for (int i = 0; i < topLine.length(); i++) {
			if (topLine.charAt(i) == delimiter) {
				end = i;
				String fieldinit = topLine.substring(start, end);
				defaultFields[index] = fieldinit.replace(' ', '_');
				defaultSize[index] = end - start + 3;
				start = i + 1;
				index++;
			}
		}
		if (index < numCols) { //get the last field because for loop exited
			String fieldinit = topLine.substring(start, topLine.length());
			defaultFields[index] = fieldinit.replace(' ', '_');
			defaultSize[index] = topLine.length() - start + 3;
		}
		//get the max size of field
		String randomLine = lines.readLine();
		index = 0;
		start = 0;
		end = 0;
		for (int i = 0; i < randomLine.length(); i++) {
			if (randomLine.charAt(i) == delimiter) {
				end = i;
				String fieldinit = randomLine.substring(start, end);
				sampleVals[index] = fieldinit;
				if ((end - start) > defaultSize[index]) { defaultSize[index] = end - start + 3; }
				start = i + 1;
				index++;
			}
		}
		if (index < numCols) { //get the last field
			String fieldinit = randomLine.substring(start, randomLine.length());
			sampleVals[index] = fieldinit;
			if ((end - start) > defaultSize[index]) { defaultSize[index] = randomLine.length() - start + 3; }
		}
		
		//print the fields
		for (int i = 0; i < defaultFields.length; i++) { 
			System.out.print(defaultFields[i] + " (" + defaultSize[i] + "), "); 
		}
		System.out.println();

		for (int i = 0; i < sampleVals.length; i++) { //check 15 lines
			if (Pattern.matches(CHAR, sampleVals[i])) { defaultTypes[i] = "CHAR(" + defaultSize[i] + ")"; }
			else if (Pattern.matches(FLOAT, sampleVals[i])) { defaultTypes[i] = "FLOAT"; }
			else if (Pattern.matches(INT, sampleVals[i])) { defaultTypes[i] = "INT"; }
			else if (Pattern.matches(DDMMYYYY, sampleVals[i]) || Pattern.matches(MMDDYYYY, sampleVals[i]) || Pattern.matches(MMDDYY, sampleVals[i])) { defaultTypes[i] = "DATE"; }
			else if (Pattern.matches(HOUR24, sampleVals[i]) || Pattern.matches(HOUR12, sampleVals[i])) { defaultTypes[i] = "TIME"; }
			else { defaultTypes[i] = "VARCHAR(100)"; }
			if (Pattern.matches(FLOAT, defaultFields[i]) || Pattern.matches(INT, defaultFields[i])) { hasTitle = false; }
		}
		
		//fix numerical titles
		if (hasTitle == false) {
			for (int i = 0; i < defaultFields.length; i++) { defaultFields[i] = "a" + i; }
		}
	}

}