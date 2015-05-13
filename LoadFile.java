import java.sql.SQLException;

//USES DB_TABLE_SIZE, NUM_DUMMY_COLS
public class LoadFile extends Connect {

	public static void tableInit() throws SQLException {

		try {
			String dropString = "DROP TABLE IF EXISTS " + Struct.tableName;
			executeUpdate(dropString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not drop the table");
			e.printStackTrace();
		}

		try {
			String createTableString = "CREATE TABLE " + Struct.tableName + " (" + Struct.dbFields[0] + " " + Struct.dbTypes[0] + " UNSIGNED NOT NULL AUTO_INCREMENT, "; //Create new
			for (int i = 1; i < Struct.dbFields.length; i++) {
				createTableString += Struct.dbFields[i] + " " + Struct.dbTypes[i] + ", ";
			}
			createTableString += "PRIMARY KEY (" + Struct.dbFields[0] + "))";
			System.out.println(createTableString);
			executeUpdate(createTableString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
		}
	}


	public static void startBulkLoad() throws SQLException {

		String bulkLoad = "LOAD DATA CONCURRENT LOCAL INFILE '" + Struct.dataFile.getAbsolutePath() + "' INTO TABLE " + Struct.tableName + " FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + Parser.terminator + "'";
		if (GUI.titleRow) { bulkLoad += " IGNORE 1 LINES "; }
		bulkLoad += " (";
		for (int i = 1; i < Struct.db_table_size; i++) { bulkLoad += Struct.dbFields[i] + ", "; }
		bulkLoad += Struct.dbFields[Struct.db_table_size - 1] + ") SET " + Struct.dbFields[0] + " = NULL, ";
		for (int i = Struct.db_table_size - Struct.num_dummy_cols; i < Struct.db_table_size; i++) {
			bulkLoad += Struct.dbFields[i] + " = NULL, ";
		}
		bulkLoad += Struct.dbFields[Struct.db_table_size - 1] + " = NULL";

		try {
			executeQuery(bulkLoad);
			System.out.println("Uploading file: " + Struct.dataFile.getAbsolutePath());	
		}
		catch (SQLException e) {
			try {
				String error = "SELECT * INTO OUTFILE '" + Struct.dataFile.getAbsolutePath() + ".out' FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + Parser.terminator + "'";
				System.out.println("Error, sent query \"" + error + "\"");
				executeQuery(error);
			} 
			catch (SQLException e2) { e2.printStackTrace(); }
		}
	}

	
	public static void mainLoader() throws SQLException {
		tableInit();
		Thread loaderThread = new Thread() {
			public void run() {
				try { startBulkLoad(); }
				catch (SQLException e) { e.printStackTrace(); }
			}
		};
		loaderThread.setName("loaderThread");
		loaderThread.start();
		
	}


}