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
			String createTableString = "CREATE TABLE " + Struct.tableName + " (id INT UNSIGNED NOT NULL AUTO_INCREMENT, "; //Create new
			for (int i = 0; i < Struct.numCols; i++ ) {
				createTableString += Struct.initFields[i] + " " + Struct.initTypes[i] + ", ";
			}
			createTableString += "version INT NULL, PRIMARY KEY (id))";
			System.out.println(createTableString);
			executeUpdate(conn, createTableString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
		}
	}

	
	public static void startBulkLoad() throws SQLException {
		
		String bulkLoad = "LOAD DATA CONCURRENT LOCAL INFILE '" + Struct.dataFile.getAbsolutePath() 
				+ "' INTO TABLE " + Struct.tableName + " FIELDS TERMINATED BY '" + Parser.delimiter 
				+ "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\"" + Parser.terminator + "' IGNORE 1 LINES (";
		for (int i = 0; i < Struct.numCols - 1; i++) { bulkLoad += Struct.initFields[i] + ", "; }
		bulkLoad += Struct.initFields[Struct.numCols - 1] + ") SET id = null, version = NULL";
		
		try {
			conn = Connect.getConnection();
			stmt.executeQuery(bulkLoad);
			System.out.println("Uploading file");			
		}
		catch (SQLException e) {
			String error = "SELECT * INTO OUTFILE '" + Struct.dataFile.getAbsolutePath() + ".out' FIELDS TERMINATED BY '" 
					+ Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + Parser.terminator + "'";
			try {
				stmt.executeQuery(error);
				System.out.println("Error, sent query \"" + error + "\"");
			} 
			catch (SQLException e1) { e1.printStackTrace(); }	
		}
	}

}