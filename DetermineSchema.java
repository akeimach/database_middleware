import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVParser;

public class DetermineSchema {
	
	//TINYINT, SMALLINT, MEDIUMINT, INT and DECIMAL.
	//CHAR, VARCHAR
	//DATETIME
	
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
	
   public static void getSchema(String filepath)throws IOException{

	   File file = new File(filepath);
       file.createNewFile();
       FileReader input = new FileReader(file); 
       
       CSVParser parser = new CSVParser(input);
       Object[] values = parser.getLine(); 
       while (parser.getLineNumber() <= 30) {
    	   int numCols = 0;
    	   for (Object col: values) {
    		   numCols++; 
    		   DatabaseGUI.fileContents.append(col + "\t"); 
    	   }
    	   if (DatabaseGUI.maxCols < numCols) { 
    		   DatabaseGUI.maxCols = numCols; 
    		   for (Object col: values) {
    			   DatabaseGUI.defaultVals.add(col);
    		   }
    	   }
    	   DatabaseGUI.fileContents.append("\n");
		   values = parser.getLine();
       }
       input.close();
   }
  
   
   public static void guessTitles(ArrayList<Object> defaultVals) {
	   for (Object header : defaultVals) {
		   String test = (String)header;
		   
		   if (CHAR.matcher(test).matches()) {
			   System.out.print(" char ");
		   }
		   else if (DECIMAL.matcher(test).matches()) {
			   System.out.print(" decimal ");
		   }
		   else if (INT.matcher(test).matches()) {
			   System.out.print(" int ");
		   }
		   else if (DDMMYYYY.matcher(test).matches()) {
			   System.out.print(" ddmmyyyy ");
		   }
		   else if (MMDDYYYY.matcher(test).matches()) {
			   System.out.print(" mmddyyyy ");
		   }
		   else if (MMDDYY.matcher(test).matches()) {
			   System.out.print(" mmddyy ");
		   }
		   else if (HOUR24.matcher(test).matches()) {
			   System.out.print(" hour24 ");
		   }
		   else if (HOUR12.matcher(test).matches()) {
			   System.out.print(" hour12 ");
		   }
		   else if (IPADDRESS.matcher(test).matches()) {
			   System.out.print(" ipaddress ");
		   }
		   else if (EMAIL.matcher(test).matches()) {
			   System.out.print(" email ");
		   }
	   }
   }
}
