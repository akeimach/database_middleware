import java.io.File;


public class Struct {

	//default
	public static String dbName = "dynamicDB";
	public static String tableName = "table1"; 
	public static long table_size = 0;
	public static int infer_sample_size = 15;
	public static int k_subsets = 2;
	//LoadFile
	public static int db_table_size = 0;
	public static int num_dummy_cols = 4;
	public static int ks_num = 1;
	
	//ParseFile
	public static int init_table_size = 0;
	public static int curr_table_size = 0;

	//Set in GUI.java
	public static File dataFile;
	public static File randomFile;

	//Parser view
	public static String[] parseFields;
	public static String[] parseTypes;
	public static String[] initFields;
	public static int[] initSizes;

	//LoadFile view on db
	public static String[] dbFields;
	public static String[] dbTypes;


	//User interaction view for change and query
	public static String[] userFields;
	public static String[] userTypes;


	//parserFields -> dbFields (for loadFile)
	public static void setDBcolumns() {
		
		//set the dummy columns id_0 and version_(num+colnum)
		db_table_size = init_table_size + num_dummy_cols;
		
		dbFields = new String[init_table_size + num_dummy_cols]; 
		dbFields[0] = "id_0";
		for (int col = 1; col < num_dummy_cols; col++) { dbFields[init_table_size + col] =  "version_" + (init_table_size + col); }
		int title_id = 0;
		//set the rest of the columns to the standard titles
		for (int i = 1; i <= init_table_size; i++) {
			if (GUI.titleRow) { dbFields[i] = initFields[title_id]; }
			else if (!GUI.titleRow) { dbFields[i] = "col_" + i; }
			title_id++;
		}

		dbTypes = new String[init_table_size + num_dummy_cols];
		dbTypes[0] = "INT";
		for (int col = 1; col < num_dummy_cols; col++) { dbTypes[init_table_size + col] =  "INT NULL"; }
		int type_id = 0;
		for (int i = 1; i <= init_table_size; i++) {
			dbTypes[i] = parseTypes[type_id];
			type_id++;
		}
	}

	//parserFields -> userFields (for ChangeSchema, QueryData)
	public static void setUserColumns() {
		
		userFields = new String[init_table_size];

		if (GUI.titleRow) { userFields = initFields; }
		else if (!GUI.titleRow) { 
			for (int i = 0; i < init_table_size; i++) { userFields[i] = "col_" + (i+1); }
		}

		userTypes = new String[init_table_size];
		userTypes = parseTypes;
	}

	
	public static void updateDBstrings() {

		db_table_size = curr_table_size + num_dummy_cols;
		
		dbFields = new String[curr_table_size + num_dummy_cols]; 
		dbFields[0] = "id_0";
		for (int col = 1; col < num_dummy_cols; col++) { dbFields[curr_table_size + col] =  "version_" + (curr_table_size + col); }
		int title_id = 0;
		//set the rest of the columns to the standard titles
		for (int i = 1; i <= init_table_size; i++) {
			if (GUI.titleRow) { dbFields[i] = userFields[title_id]; }
			else if (!GUI.titleRow) { dbFields[i] = "col_" + i; }
			title_id++;
		}

		dbTypes = new String[curr_table_size + num_dummy_cols];
		dbTypes[0] = "INT";
		for (int col = 1; col < num_dummy_cols; col++) { dbTypes[curr_table_size + col] =  "INT NULL"; }
		int type_id = 0;
		for (int i = 1; i <= init_table_size; i++) {
			dbTypes[i] = parseTypes[type_id];
			type_id++;
		}
		init_table_size = curr_table_size;
	}
	

}
