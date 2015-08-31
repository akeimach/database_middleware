package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;


public class Parser {
	
	public static String[] fields;
	public static String[] types;
	public static int[] sizes;
	public static String[] titleRow;
	public static int tableSize = 0;
	public static boolean titlesIncluded = true;
	public static int sampleSize = 15;
	

	//default terminator and delimiter for file parsing
	public static char delimiter = ',';
	public static char terminator = '\n';

	//REGEX pattern matching
	static String NUMERIC = "-?\\d+(\\.\\d+)?";
	static String INT = "[\\+\\-]?\\d+";
	static String FLOAT = "[\\+\\-]?\\d+\\.\\d+(?:[eE][\\+\\-]?\\d+)?";
	static String CHAR = "([a-z]|[A-Z])+";
	static String DDMMYYYY = "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)";
	static String MMDDYYYY = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d)";
	static String MMDDYY = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/(\\d\\d)";
	static String HOUR24 = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	static String HOUR12 = "(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)";
	static String TIMESTAMP = MMDDYYYY + " " + HOUR24;
	static String IPADDRESS = "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	static String EMAIL = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";
	static String INVALTITLE = "[^\\s^\\d^a-z^A-Z]";
	static String QUOTES = "([\"'])(?:(?=(\\?))\2.)*?\1";
	static String YYYYMMDDHHMMSS = "(19[0-9]{2}|2[0-9]{3})-(0[1-9]|1[012])-([123]0|[012][1-9]|31) ([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])";

	//look through the text file to determine the terminator of each line
	//will be used in the bulk load function later
	public static void findTerminator(File file) throws FileNotFoundException {
		BufferedReader lines = new BufferedReader(new FileReader(file));
		int countLines = 0;
		int c;
		int[] terminators = { 0x0A, 0x0D, 0x0D0A }; //\n, \r, \r\n
		int[] counters = { 0, 0, 0 };
		try {
			while (((c = lines.read()) != -1) && (countLines < sampleSize)) {
				for (int d = 0; d < terminators.length; d++) {
					if (c == terminators[d]) { 
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
		terminator = (char)terminators[maxindex];
		System.out.println("Terminator: '" + terminators[maxindex] + "'");
	}

	//find the deliminator by looking through the data file
	public static void findDelimiter(File file) throws FileNotFoundException {

		// Count every ,;/ and tab, see which one is used most often
		BufferedReader lines = new BufferedReader(new FileReader(file));
		int countLines = 0;
		char[] delimiters = { ',', '/', ' ', ';', '\t' };
		int[] counters = { 0, 0, 0, 0, 0 };
		try {
			while (countLines < sampleSize) {
				String tuple = lines.readLine();
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
		tableSize = (max / sampleSize) + 1;
	}


	//get the titles and sizes for each field
	//if titles are included in the data file, use them as the field names
	public static void findFields(File file) throws IOException {

		titleRow = new String[tableSize];
		fields = new String[tableSize];
		sizes = new int[tableSize];

		BufferedReader lines = new BufferedReader(new FileReader(file));

		//throw away title line for data stuff, parse here
		if (titlesIncluded) { 
			String titles = lines.readLine(); 
			int index = 0;
			int start = 0;
			int end = 0;
			for (int i = 0; i < titles.length(); i++) {
				if (titles.charAt(i) == delimiter) {
					end = i;
					String titleinit = titles.substring(start, end);
					for (int j = 0; j < titleinit.length(); j++) {
						if (!Character.isLetterOrDigit(titleinit.charAt(j))) {
							titleinit = titleinit.replace(titleinit.charAt(j), '_');
						}
					}
					titleRow[index] = titleinit;
					start = i + 1;
					index++;
				}
			}
			if (index < tableSize) { 
				String titleinit = titles.substring(start, titles.length());
				for (int j = 0; j < titleinit.length(); j++) {
					if (!Character.isLetterOrDigit(titleinit.charAt(j))) {
						titleinit = titleinit.replace(titleinit.charAt(j), '_');
					}
				}
				titleRow[index] = titleinit;
			}

		} 

		int countLines = 0;
		while (countLines < sampleSize) {	
			String tuple = lines.readLine();
			int index = 0;
			int start = 0;
			int end = 0;
			for (int i = 0; i < tuple.length(); i++) {
				if (tuple.charAt(i) == delimiter) {
					end = i;
					String fieldinit = tuple.substring(start, end);
					if (sizes[index] < (end - start + 3)) {
						fields[index] = fieldinit;
						sizes[index] = end - start + 3;
					}
					start = i + 1;
					index++;
				}
			}
			if (index < tableSize) { //get the last field because for loop exited
				String fieldinit = tuple.substring(start, tuple.length());
				if (sizes[index] < (tuple.length() - start + 3)) {
					fields[index] = fieldinit;
					sizes[index] = tuple.length() - start + 3;
				}
			}
			countLines++;
		}
	}

	
	//check the raw data to determine the data type for the column
	//use regex pattern checking
	public static void findTypes(File file) throws FileNotFoundException {

		types = new String[tableSize];

		// Count every ,;/ and tab, see which one is used most often
		BufferedReader lines = new BufferedReader(new FileReader(file));
		int countLines = 0;
		try {
			while (countLines < sampleSize) {
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

				if (index < tableSize) { //get the last field because for loop exited
					String fieldinit = tuple.substring(start, tuple.length());
					patternMatcher(fieldinit, index);
				}
				countLines++;
			}
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	//redo init types with regex
	public static void patternMatcher(String value, int i) {

		value = value.replaceAll("^\"|\"$", "");
		value = value.replaceAll(QUOTES, "");
		
		if (Pattern.matches(CHAR, value)) { 
			if (sizes[i] < 10) { types[i] = "CHAR(" + sizes[i] + 5 + ")"; }
			else { types[i] = "VARCHAR(100)"; }
		}
		else if (Pattern.matches(FLOAT, value)) { types[i] = "FLOAT"; }
		else if (Pattern.matches(INT, value)) { types[i] = "BIGINT"; } //for testing
		else if (Pattern.matches(DDMMYYYY, value) || Pattern.matches(MMDDYYYY, value) || Pattern.matches(MMDDYY, value)) { types[i] = "DATE"; }
		else if (Pattern.matches(HOUR24, value) || Pattern.matches(HOUR12, value)) { types[i] = "TIME"; }
		//else if (Pattern.matches(TIMESTAMP, value)) { initTypes[i] = "TIMESTAMP"; }
		else if (Pattern.matches(YYYYMMDDHHMMSS, value)) { types[i] = "TIMESTAMP"; }
		else { types[i] = "VARCHAR(100)"; }

	}


	public static void parseData(File dataFile) throws FileNotFoundException, InterruptedException, InvocationTargetException {
		try {
			findTerminator(dataFile);
			findDelimiter(dataFile);
			findFields(dataFile);
			findTypes(dataFile);
			DBView.setDBcolumns();
			UserView.setUserColumns();
			UserView.tableSize = tableSize;
		} 
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	} 
}



