import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVParser;

@SuppressWarnings("serial")
public class InferData extends GUI {

	public static ArrayList<String> sampleVals;// = new ArrayList<String>();
	public static ArrayList<String> sampleTypes = new ArrayList<String>();
	static Pattern INT = Pattern.compile("[\\+\\-]?\\d+");
	static Pattern DECIMAL = Pattern.compile("[\\+\\-]?\\d+\\.\\d+(?:[eE][\\+\\-]?\\d+)?");
	static Pattern CHAR = Pattern.compile("[^0-9]");
	static Pattern DDMMYYYY = Pattern.compile("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)");
	static Pattern MMDDYYYY = Pattern.compile("(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d)");
	static Pattern MMDDYY = Pattern.compile("(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/(\\d\\d)");
	static Pattern HOUR24 = Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]");
	static Pattern HOUR12 = Pattern.compile("(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)");
	static Pattern IPADDRESS = Pattern.compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
	static Pattern EMAIL = Pattern.compile("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})");

	public static void getSample() throws IOException, SQLException {
		File file = new File(path.getAbsolutePath());
		FileReader input = new FileReader(file); 
		CSVParser parser = new CSVParser(input);
		String[] values = parser.getLine(); 
		int maxCol = 0;
		while (parser.getLineNumber() < 15) {	
			int curCol = 0;
			for (@SuppressWarnings("unused") String count : values) { curCol++; }
			if (maxCol < curCol) { 
				maxCol = curCol;
				sampleVals = new ArrayList<String>();
				for (String val : values) { sampleVals.add(val); }
			}
			values = parser.getLine();
		}
		input.close();
		getDType();
	}

	public static void getDType() throws SQLException {
		for (String test : sampleVals) {
			if (CHAR.matcher(test).matches()) { sampleTypes.add("CHAR"); }
			else if (DECIMAL.matcher(test).matches()) { sampleTypes.add("FLOAT"); }
			else if (INT.matcher(test).matches()) { sampleTypes.add("INT"); }
			else if (DDMMYYYY.matcher(test).matches()) { sampleTypes.add("DATE"); }
			else if (MMDDYYYY.matcher(test).matches()) { sampleTypes.add("DATE"); }
			else if (MMDDYY.matcher(test).matches()) { sampleTypes.add("DATE"); }
			else if (HOUR24.matcher(test).matches()) { sampleTypes.add("TIME"); }
			else if (HOUR12.matcher(test).matches()) { sampleTypes.add("TIME"); }
			else if (IPADDRESS.matcher(test).matches()) { sampleTypes.add("VARCHAR"); }
			else if (EMAIL.matcher(test).matches()) { sampleTypes.add("VARCHAR"); }
			else { sampleTypes.add("VARCHAR"); }
		}
		Schema.getInferred();
	}
	
}