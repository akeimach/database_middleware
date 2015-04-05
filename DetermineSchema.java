import java.util.ArrayList;

public class DetermineSchema extends DatabaseGUI {
	
	ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
	
    public static ArrayList<String> DetermineSchema(String line, ArrayList<ArrayList<String>> rows) {
    	
    	//System.out.println(line);
    	
    	ArrayList<String> cols = new ArrayList<String>();
    	
    	//add to arraylist using split operation
    	if (line != null) {
    		String[] parseLine = line.split("\\s*,\\s*");
    		for (int i = 0; i < parseLine.length; i++) {
    			if (!(parseLine[i] == null) || (parseLine[i].length() == 0)) {
    				cols.add(parseLine[i].trim());
    			}
    		
    			
    		}	
    	
    	}
    	
    	rows.add(cols);
    	return cols;
    	
    }
    
}