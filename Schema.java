import java.sql.SQLException;

@SuppressWarnings("serial")
public class Schema extends GUI {
	
	public static int tableSize = 0;
	public static String[] schemaTypes = null;
	public static String[] schemaVals = null;
	public static String[] dropTypes = null;
	public static String[] sampleArr = null;
	
	public static void getInferred() throws SQLException {
		tableSize = InferData.sampleTypes.size();
		schemaTypes = new String[tableSize];
		schemaVals = new String[tableSize];
		sampleArr = new String[tableSize];
		dropTypes = new String[tableSize];
		int i = 0;
		for (String col : InferData.sampleTypes) {
			dropTypes[i] = col;
			if (col == "VARCHAR") { schemaTypes[i] = "VARCHAR(100)"; }
			else if (col == "CHAR") { schemaTypes[i] = "CHAR(5)"; }
			else { schemaTypes[i] = col; }
			schemaVals[i] = "a" + i;
			System.out.println(schemaVals[i] + ", " + schemaTypes[i]);
			i++;
		}
		i = 0;
		for (String col : InferData.sampleVals) { 
			sampleArr[i] = col; 
			i++;
		}
		
		initialSchema();
	}
	
	public static void initialSchema() throws SQLException {
		//send schema to GUI
		//receive user edits/approval
		GUI.schemaContents(GUI.tabLoad, tableSize, schemaVals, sampleArr, dropTypes);
		finalSchema();
	}
	
	public static void finalSchema() throws SQLException {
		HandleData.tableInit();
	}
}