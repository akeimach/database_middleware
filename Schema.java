import java.sql.SQLException;

@SuppressWarnings("serial")
public class Schema extends GUI {
	
	static int tableSize = 0;
	static String[] schemaTypes = null;
	static String[] schemaVals = null;

	public static void getInferred() throws SQLException {
		tableSize = InferData.sampleTypes.size();
		schemaTypes = new String[tableSize];
		schemaVals = new String[tableSize];
		int i = 0;
		for (String col : InferData.sampleTypes) {
			if (col == "VARCHAR") { schemaTypes[i] = "VARCHAR(20)"; }
			else if (col == "CHAR") { schemaTypes[i] = "CHAR(5)"; }
			else { schemaTypes[i] = col; }
			schemaVals[i] = "a" + i;
			System.out.println(schemaVals[i] + ", " + schemaTypes[i]);
			i++;
		}
		initialSchema();
	}
	
	public static void initialSchema() throws SQLException {
		//send schema to GUI
		//receive user edits/approval
		finalSchema();
	}
	
	public static void finalSchema() throws SQLException {
		HandleData.tableInit();
	}
}