import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;


public class Parser {

	public static char delimiter = ','; //default
	public static char terminator = '\n'; //default
	static String INT = "[\\+\\-]?\\d+";
	static String FLOAT = "[\\+\\-]?\\d+\\.\\d+(?:[eE][\\+\\-]?\\d+)?";
	static String CHAR = "([a-z]|[A-Z])+";
	static String DDMMYYYY = "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)";
	static String MMDDYYYY = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d)";
	static String MMDDYY = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/(\\d\\d)";
	static String HOUR24 = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	static String HOUR12 = "(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)";
	static String IPADDRESS = "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	static String EMAIL = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";
	static String INVALTITLE = "[^\\s^\\d^a-z^A-Z]";


	public static void findTerminator(File file) throws FileNotFoundException {
		BufferedReader lines = new BufferedReader(new FileReader(file));
		int countLines = 0;
		int c;
		char[] terminators = { '\r', '\n' };
		int[] counters = { 0, 0 };
		try {
			while (((c = lines.read()) != -1) && (countLines < Struct.sampleLines)) {
				char character = (char) c;
				for (int d = 0; d < terminators.length; d++) {
					if (character == terminators[d]) { 
						counters[d]++; 
						countLines++;
					}
				}
			}
		} 
		catch (IOException e) { e.printStackTrace(); }

		int max = 0;
		int maxindex = 0;
		for (int i = 0; i < counters.length; i++) {
			if (max < counters[i]) { 
				max = counters[i]; 
				maxindex = i; 
			}
		}
		terminator = terminators[maxindex];
	}

	public static void findDelimiter(File file) throws FileNotFoundException {

		// Count every ,;/ and tab, see which one is used most often
		BufferedReader lines = new BufferedReader(new FileReader(file));
		int countLines = 0;
		char[] delimiters = { ',', '/', ' ', ';', '\t' };
		int[] counters = { 0, 0, 0, 0, 0 };
		try {
			while (countLines < Struct.sampleLines) {
				String tuple = lines.readLine();
				if (countLines == 0) { Struct.titleRow = tuple; } //top line might be titles
				for (int i = 0; i < tuple.length(); i++) {
					char c = tuple.charAt(i);
					for (int d = 0; d < delimiters.length; d++) {
						if (c == delimiters[d]) { counters[d]++; }
					}
				}
				countLines++;
			}
		} 
		catch (IOException e) { e.printStackTrace(); }

		int max = 0;
		int maxindex = 0;
		for (int i = 0; i < counters.length; i++) {
			if (max < counters[i]) { 
				max = counters[i]; 
				maxindex = i; 
			}
		}

		delimiter = delimiters[maxindex];
		System.out.println("Delimiter: '" + delimiters[maxindex] + "'");
		Struct.numCols = (max / Struct.sampleLines) + 1;

	}

	public static void findFields() {

		//NEXT GET THE FIELDS
		Struct.initFields = new String[Struct.numCols];
		Struct.initTypes = new String[Struct.numCols];
		Struct.initSize = new int[Struct.numCols];

		int index = 0;
		int start = 0;
		int end = 0;
		for (int i = 0; i < Struct.titleRow.length(); i++) {
			if (Struct.titleRow.charAt(i) == delimiter) {
				end = i;
				String fieldinit = Struct.titleRow.substring(start, end);
				fieldinit = fieldinit.replace("\"", "");
				fieldinit = fieldinit.replace(" ", "");
				Struct.initFields[index] = fieldinit + "_" + (index + 1);
				Struct.initSize[index] = end - start + 3;
				start = i + 1;
				index++;
			}
		}
		if (index < Struct.numCols) { //get the last field because for loop exited
			String fieldinit = Struct.titleRow.substring(start, Struct.titleRow.length());
			fieldinit = fieldinit.replace("\"", "");
			fieldinit = fieldinit.replace(" ", "");
			Struct.initFields[index] = "col" + fieldinit + "_" + (index + 1);
			Struct.initSize[index] = Struct.titleRow.length() - start + 3;
		}
	}

	public static void findTypes(File file) throws FileNotFoundException {

		// Count every ,;/ and tab, see which one is used most often
		BufferedReader lines = new BufferedReader(new FileReader(file));
		int countLines = 0;
		try {
			while (countLines < Struct.sampleLines) {
				int index = 0;
				int start = 0;
				int end = 0;
				String tuple = lines.readLine();
				for (int i = 0; i < tuple.length(); i++) {
					if (tuple.charAt(i) == delimiter) {
						end = i;
						String fieldinit = tuple.substring(start, end);
						patternMatcher(fieldinit, index);
						start = i + 1;
						index++;
					}
				}

				if (index < Struct.numCols) { //get the last field because for loop exited
					String fieldinit = tuple.substring(start, tuple.length());
					patternMatcher(fieldinit, index);
				}
				countLines++;
			}
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	public static void patternMatcher(String value, int i) {

		if (Pattern.matches(CHAR, value)) { 
			if (Struct.initSize[i] < 10) { Struct.initTypes[i] = "CHAR(" + Struct.initSize[i] + 5 + ")"; }
			else { Struct.initTypes[i] = "VARCHAR(100)"; }
		}
		else if (Pattern.matches(FLOAT, value)) { Struct.initTypes[i] = "FLOAT"; }
		else if (Pattern.matches(INT, value)) { Struct.initTypes[i] = "INT"; }
		else if (Pattern.matches(DDMMYYYY, value) || Pattern.matches(MMDDYYYY, value) || Pattern.matches(MMDDYY, value)) { Struct.initTypes[i] = "DATE"; }
		else if (Pattern.matches(HOUR24, value) || Pattern.matches(HOUR12, value)) { Struct.initTypes[i] = "TIME"; }
		else { Struct.initTypes[i] = "VARCHAR(100)"; }
	}
	
	/*
	public static void main(String args[]) {

		findTerminator(File file)
		findDelimiter(File file)
		findFields()
		findTypes(File file)

	} 
	*/


}

