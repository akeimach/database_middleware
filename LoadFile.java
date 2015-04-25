import java.sql.SQLException;

public class LoadFile extends Connect {


	public static void tableInit() throws SQLException {

		conn = Connect.getConnection();

		try {
			String dropString = "DROP TABLE IF EXISTS " + Struct.tableName; //Drop table
			executeUpdate(conn, dropString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not drop the table");
			e.printStackTrace();
		}

		try {
			String createTableString = "CREATE TABLE " + Struct.tableName + " (" + Struct.dynamicFields[0] + " " + Struct.dynamicTypes[Struct.dynamicNumCols - 1] + " UNSIGNED NOT NULL AUTO_INCREMENT, "; //Create new
			for (int i = 1; i < Struct.dynamicNumCols - 1; i++ ) {
				createTableString += Struct.dynamicFields[i] + " " + Struct.dynamicTypes[i] + ", ";
			}
			createTableString += Struct.dynamicFields[Struct.dynamicNumCols - 1] + " " + Struct.dynamicTypes[Struct.dynamicNumCols - 1] + " NULL, PRIMARY KEY (" + Struct.dynamicFields[0] + "))";
			executeUpdate(conn, createTableString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
		}
	}


	public static void startBulkLoad() throws SQLException {

		String bulkLoad = "LOAD DATA CONCURRENT LOCAL INFILE '" + Struct.dataFile.getAbsolutePath() + "' INTO TABLE " + Struct.tableName + " FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + Parser.terminator + "' (";
		for (int i = 1; i < Struct.numCols; i++) { bulkLoad += Struct.dynamicFields[i] + ", "; }
		bulkLoad += Struct.dynamicFields[Struct.numCols] + ") SET " + Struct.dynamicFields[0] + " = NULL, " + Struct.dynamicFields[Struct.dynamicNumCols - 1] + " = NULL";

		try {
			conn = Connect.getConnection();
			stmt.executeQuery(bulkLoad);
			System.out.println("Uploading file: " + bulkLoad);			

		}
		catch (SQLException e) {
			try {
				String error = "SELECT * INTO OUTFILE '" + Struct.dataFile.getAbsolutePath() + ".out' FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + Parser.terminator + "'";
				System.out.println("Error, sent query \"" + error + "\"");
				stmt.executeQuery(error);
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
		loaderThread.start();
	}


}