import java.io.IOException;
import java.sql.SQLException;

public class LoadFile extends Connect {

	public static String createTableString;

	public static void initUpload() throws SQLException, IOException {
		Connect.conn = Connect.getConnection();
		tableInit();
		Parser.findTerminator(Parser.file);
		Parser.getFormat(Parser.file); //get sample data and default schema
		createTable();
	}

	public static void tableInit() throws SQLException {
		try {
			String dropString = "DROP TABLE IF EXISTS " + Struct.tableName; //Drop table
			executeUpdate(conn, dropString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not drop the table");
			e.printStackTrace();
		}
	}

	public static void createTable() throws SQLException {
		try {
			createTableString = "CREATE TABLE " + Struct.tableName + " (id INT UNSIGNED NOT NULL AUTO_INCREMENT, "; //Create new
			for (int i = 0; i < Parser.numCols; i++ ) {
				createTableString += Parser.defaultFields[i] + " " + Parser.defaultTypes[i] + ", ";
			}
			createTableString += "version INT NULL, PRIMARY KEY (id))";
			executeUpdate(conn, createTableString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
		}
	}

	public static void startBulkLoad() {
		String bulkLoad = "LOAD DATA CONCURRENT LOCAL INFILE '" + Parser.file.getAbsolutePath() + "' INTO TABLE " + Struct.tableName + " FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\"" + Parser.terminator + "'";
		if (Parser.hasTitle == true) { bulkLoad += " IGNORE 1 LINES"; }
		bulkLoad += " (";
		for (int i = 0; i < Parser.numCols - 1; i++) {
			bulkLoad += Parser.defaultFields[i] + ", ";
		}
		bulkLoad += Parser.defaultFields[Parser.numCols - 1] + ") SET id = null, version = NULL";
		try {
			stmt.executeQuery(bulkLoad);
			System.out.println("Sent query \"" + bulkLoad + "\"");
			
			System.out.println("Uploading file");
		
			String collectErrors = "SHOW WARNINGS";
			conn = Connect.getConnection();
			rs = executeQuery(conn, collectErrors.trim());
			int i = 0;
			while (rs.next()) {
				System.out.println(rs.getString(i));
				i++;
			}
			
		}
			
		catch (SQLException e) {
			String error = "SELECT * INTO OUTFILE '" + Parser.file.getAbsolutePath() + ".out' FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + Parser.terminator + "'";
			try {
				stmt.executeQuery(error);
				System.out.println("Sent query \"" + error + "\"");
			} 
			catch (SQLException e1) { e1.printStackTrace(); }	
		}
	}

}