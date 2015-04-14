import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class LoadFile extends Connect {

	public static File file;
	public static char delimiter = ','; //default
	public static String terminator = "\\n"; //default

	public static void initUpload() throws SQLException, IOException {
		conn = Connect.getConnection();
		tableInit();
		Parser.getFormat(file); //get sample data and default schema
		createTable();
		startBulkLoad();
	}

	public static void tableInit() throws SQLException {
		try {
			String dropString = "DROP TABLE IF EXISTS " + tableName; //Drop table
			executeUpdate(conn, dropString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not drop the table");
			e.printStackTrace();
		}
	}

	public static void createTable() throws SQLException {
		try {
			String createString = "CREATE TABLE " + tableName + " (id INT UNSIGNED NOT NULL AUTO_INCREMENT, "; //Create new
			for (int i = 0; i < Parser.numCols; i++ ) {
				createString += Parser.defaultFields[i] + " " + Parser.defaultTypes[i] + ", ";
			}
			createString += "version INT NULL, PRIMARY KEY (id))";
			executeUpdate(conn, createString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
		}
	}

	public static void startBulkLoad() {
		String bulkLoad = "LOAD DATA CONCURRENT LOCAL INFILE '" + file.getAbsolutePath() + "' INTO TABLE " + tableName + " FIELDS TERMINATED BY '" + delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + terminator + "'";
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
		} 
		catch (SQLException e) {
			String error = "SELECT * INTO OUTFILE '" + file.getAbsolutePath() + ".out' FIELDS TERMINATED BY '" + delimiter +"' LINES TERMINATED BY '" + terminator + "' FROM " + tableName;
			try {
				stmt.executeQuery(error);
				System.out.println("Sent query \"" + error + "\"");
			} 
			catch (SQLException e1) { e1.printStackTrace(); }	
		}
	}

}