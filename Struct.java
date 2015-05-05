import java.io.File;


public class Struct {

	//default
	public static String tableName = "table1"; 
	public static int infer_sample_size = 15;

	//Set in GUI.java
	public static File dataFile;
	
	//Parser view

	public static String[] initFields;
	public static int[] parseSizes;
	public static String[] parseFields;
	public static String[] parseTypes;

	public static int numDummyCols = 4;
	public static int initNumCols = 0;

	//LoadFile view on db
	public static String[] dbFields;
	public static String[] dbTypes;


	//User interaction view for change and query
	public static String[] userFields;
	public static String[] userTypes;


	//parserFields -> dbFields (for loadFile)
	public static void dbColumns(int numDummy) {

		dbFields = new String[initNumCols + numDummy]; 
		dbFields[0] = "id_0";
		for (int col = 1; col < numDummy; col++) {
			dbFields[initNumCols + col] =  "version_" + (initNumCols + col);
		}
		int title_id = 0;
		for (int i = 1; i <= initNumCols; i++) {
			if (GUI.titleRow) { dbFields[i] = initFields[title_id]; }
			else if (!GUI.titleRow) { dbFields[i] = "col_" + i; }
			title_id++;
		}
		
		dbTypes = new String[initNumCols + numDummy];
		dbTypes[0] = "INT";
		for (int col_type = 1; col_type < numDummy; col_type++) {
			dbTypes[initNumCols + col_type] =  "INT NULL";
		}
		int type_id = 0;
		for (int i = 1; i <= initNumCols; i++) {
			dbTypes[i] = parseTypes[type_id];
			type_id++;
		}
	

	}

	//parserFields -> userFields (for ChangeSchema, QueryData)
	public static void userColumns() {
		
		userFields = new String[initNumCols];
		if (GUI.titleRow) { userFields = initFields; }
		else if (!GUI.titleRow) { 
			for (int i = 0; i < initNumCols; i++) { userFields[i] = "col_" + (i+1); }
		}
		
		userTypes = new String[initNumCols];
		userTypes = parseTypes;
		
	}
	


}
