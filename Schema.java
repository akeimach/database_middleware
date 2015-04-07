import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVParser;

public class Schema {
	
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
	
   public static void getSchema(String filepath) throws IOException{

	   File file = new File(filepath);
       file.createNewFile();
       FileReader input = new FileReader(file); 
       
       CSVParser parser = new CSVParser(input);
       String[] values = parser.getLine(); 
       while (parser.getLineNumber() <= 30) {
    	   int numCols = 0;
    	   for (String col: values) {
    		   numCols++; 
    		   GUI.sampleTuple.append(col + " | "); 
    	   }
    	   if (GUI.maxCols < numCols) { 
    		   GUI.maxCols = numCols; 
    		   for (String col: values) {
    			   GUI.defaultVals.add(col);
    			   GUI.defaultNames.add(col);
    		   }
    	   }
    	   GUI.sampleTuple.append("\n");
		   values = parser.getLine();
       }
       input.close();
       
       determineDtype(GUI.defaultVals);
       finalizeSchema(); 
   }
  
   //{"INT", "BIGINT", "FLOAT", "DOUBLE", "BIT", "CHAR", "VARCHAR", "TEXT", "DATE", "DATETIME", "TIME", "TIMESTAMP", "YEAR"}
   
   public static void determineDtype(ArrayList<String> defaultVals) {
	   int index = 0;
	   for (Object header : defaultVals) {
		   String test = (String)header;
		   
		   if (CHAR.matcher(test).matches()) {
			   defaultVals.set(index, "CHAR");
			   //System.out.print(" char ");
		   }
		   else if (DECIMAL.matcher(test).matches()) {
			   defaultVals.set(index, "FLOAT");
			  // System.out.print(" decimal ");
		   }
		   else if (INT.matcher(test).matches()) {
			   defaultVals.set(index, "INT");
			   //System.out.print(" int ");
		   }
		   else if (DDMMYYYY.matcher(test).matches()) {
			   defaultVals.set(index, "DATE");
			  // System.out.print(" ddmmyyyy ");
		   }
		   else if (MMDDYYYY.matcher(test).matches()) {
			   defaultVals.set(index, "DATE");
			   //System.out.print(" mmddyyyy ");
		   }
		   else if (MMDDYY.matcher(test).matches()) {
			   defaultVals.set(index, "DATE");
			   //System.out.print(" mmddyy ");
		   }
		   else if (HOUR24.matcher(test).matches()) {
			   defaultVals.set(index, "TIME");
			   //System.out.print(" hour24 ");
		   }
		   else if (HOUR12.matcher(test).matches()) {
			   defaultVals.set(index, "TIME");
			   //System.out.print(" hour12 ");
		   }
		   else if (IPADDRESS.matcher(test).matches()) {
			   defaultVals.set(index, "VARCHAR");
			   //System.out.print(" ipaddress ");
		   }
		   else if (EMAIL.matcher(test).matches()) {
			   defaultVals.set(index, "VARCHAR");
			   //System.out.print(" email ");
		   }
		   else {
			   defaultVals.set(index, "VARCHAR");
		   }
		   index++;
	   }
   }
   
   public static void finalizeSchema() {
	   int x = GUI.defaultVals.size();
	   GUI.finalNames = new String[x];
	   GUI.finalVals = new String[x];
	   int i = 0;
	   for (String col : GUI.defaultVals) {
		   if (col == "VARCHAR") { GUI.finalVals[i] = "VARCHAR(20)"; }
		   else if (col == "CHAR") { GUI.finalVals[i] = "CHAR(5)"; }
		   else { GUI.finalVals[i] = col; }
		   GUI.finalNames[i] = "a" + i;
		   i++;
	   }
	   
	   Connect.runDB();
   }
   
}