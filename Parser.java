import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;


public class Parser extends Struct {

	public static char delimiter = ','; //default
	public static char terminator = '\n'; //default
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


	public static void findTerminator(File file) throws FileNotFoundException {
		BufferedReader lines = new BufferedReader(new FileReader(file));
		int countLines = 0;
		int c;
		int[] terminators = { 0x0A, 0x0D, 0x0D0A }; //\n, \r, \r\n
		int[] counters = { 0, 0, 0 };
		try {
			while (((c = lines.read()) != -1) && (countLines < infer_sample_size)) {
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

	public static void findDelimiter(File file) throws FileNotFoundException {

		// Count every ,;/ and tab, see which one is used most often
		BufferedReader lines = new BufferedReader(new FileReader(file));
		int countLines = 0;
		char[] delimiters = { ',', '/', ' ', ';', '\t' };
		int[] counters = { 0, 0, 0, 0, 0 };
		try {
			while (countLines < infer_sample_size) {
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
		init_table_size = (max / infer_sample_size) + 1;
	}


	//initFields and initSize set
	public static void findFields(File file) throws IOException {

		//NEXT GET THE FIELDS
		initFields = new String[init_table_size];
		parseFields = new String[init_table_size];
		initSizes = new int[init_table_size];

		BufferedReader lines = new BufferedReader(new FileReader(file));

		//throw away title line for data stuff, parse here
		if (GUI.titleRow) { 
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
					initFields[index] = titleinit;
					start = i + 1;
					index++;
				}
			}
			if (index < init_table_size) { 
				String titleinit = titles.substring(start, titles.length());
				for (int j = 0; j < titleinit.length(); j++) {
					if (!Character.isLetterOrDigit(titleinit.charAt(j))) {
						titleinit = titleinit.replace(titleinit.charAt(j), '_');
					}
				}
				initFields[index] = titleinit;
			}

		} 
		
		int countLines = 0;
		while (countLines < infer_sample_size) {	
			String tuple = lines.readLine();
			int index = 0;
			int start = 0;
			int end = 0;
			for (int i = 0; i < tuple.length(); i++) {
				if (tuple.charAt(i) == delimiter) {
					end = i;
					String fieldinit = tuple.substring(start, end);
					if (initSizes[index] < (end - start + 3)) {
						parseFields[index] = fieldinit;
						initSizes[index] = end - start + 3;
					}
					start = i + 1;
					index++;
				}
			}
			if (index < init_table_size) { //get the last field because for loop exited
				String fieldinit = tuple.substring(start, tuple.length());
				if (initSizes[index] < (tuple.length() - start + 3)) {
					parseFields[index] = fieldinit;
					initSizes[index] = tuple.length() - start + 3;
				}
			}
			countLines++;
		}
	}


	public static void splitFile(File file) throws IOException, InterruptedException {

		PrintWriter splitexe = new PrintWriter("/Users/alyssakeimach/Eclipse/DBconnector/data/split.exe", "UTF-8");
		Runtime.getRuntime().exec("chmod a+x /Users/alyssakeimach/Eclipse/DBconnector/data/split.exe");
		splitexe.println("split -a1 -l20000 " + file); //-a3 for three letter file names
		splitexe.close();
		
		ProcessBuilder pb = new ProcessBuilder("/Users/alyssakeimach/Eclipse/DBconnector/data/split.exe");
		pb.directory(new File("/Users/alyssakeimach/Eclipse/DBconnector/data/splits/"));
		pb.redirectErrorStream(true);
		Process p = pb.start();
		assert pb.redirectInput() == Redirect.PIPE;
		assert p.getInputStream().read() == -1;

	}

	public static File getRandFile(File roots) {

		//check if multiple files in directory
		if (roots.isFile() || roots.list().length == 0) { return roots; }

		File[] files = roots.listFiles();
		List<File> sub_dir = new ArrayList<File>(Arrays.asList(files));
		
		Iterator<File> fit = sub_dir.iterator();
		while (fit.hasNext()) {
			if (!fit.next().isDirectory()) { fit.remove(); }
		}

		Random rand = new Random();
		while (!sub_dir.isEmpty()) {
			File rndSubDir = sub_dir.get(rand.nextInt(sub_dir.size()));
			File rndSubFile = getRandFile(rndSubDir);
			if (rndSubFile != null) {
				System.out.println(rndSubFile.getAbsolutePath() + 2);
				return rndSubFile;
			}
			sub_dir.remove(rndSubDir);
		}
		return roots;
	}

	//initTypes set
	public static void findTypes(File file) throws FileNotFoundException {

		parseTypes = new String[init_table_size];

		// Count every ,;/ and tab, see which one is used most often
		BufferedReader lines = new BufferedReader(new FileReader(file));
		int countLines = 0;
		try {
			while (countLines < infer_sample_size) {
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

				if (index < init_table_size) { //get the last field because for loop exited
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
		//System.out.print(value);
		
		if (Pattern.matches(CHAR, value)) { 
			if (initSizes[i] < 10) { parseTypes[i] = "CHAR(" + initSizes[i] + 5 + ")"; }
			else { parseTypes[i] = "VARCHAR(100)"; }
		}
		else if (Pattern.matches(FLOAT, value)) { parseTypes[i] = "FLOAT"; }
		else if (Pattern.matches(INT, value)) { parseTypes[i] = "BIGINT"; } //TODO: for testing
		else if (Pattern.matches(DDMMYYYY, value) || Pattern.matches(MMDDYYYY, value) || Pattern.matches(MMDDYY, value)) { parseTypes[i] = "DATE"; }
		else if (Pattern.matches(HOUR24, value) || Pattern.matches(HOUR12, value)) { parseTypes[i] = "TIME"; }
		//else if (Pattern.matches(TIMESTAMP, value)) { initTypes[i] = "TIMESTAMP"; }
		else if (Pattern.matches(YYYYMMDDHHMMSS, value)) { parseTypes[i] = "TIMESTAMP"; }
		else { parseTypes[i] = "VARCHAR(100)"; }

	}

	
	public static void mainParser() throws FileNotFoundException, InterruptedException, InvocationTargetException {
		try {
			findTerminator(dataFile);
			findDelimiter(dataFile);
			findFields(dataFile);
			splitFile(dataFile);
			
			/*
			File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/data/splits/");
			File[] roots = folder.listFiles();
			Random rand = new Random();
			randomFile = Parser.getRandFile(roots[rand.nextInt(roots.length)]);
			*/
			findTypes(dataFile);
			Struct.setDBcolumns();
			Struct.setUserColumns();
			Struct.curr_table_size = Struct.init_table_size;

		} 
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	} 
}



