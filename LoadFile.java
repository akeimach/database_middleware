import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoadFile {

	public static File file;
	public static String tableName = "defaultTable"; //default
	public static String dbName = "testDynamic";
	public static Connection conn;
	public static Statement stmt;
	public static PreparedStatement pstmt;
	public static ResultSet rs;
	public static char delimiter = ','; //default
	public static String terminator = "\\n"; //default

	public static void initUpload(File file) throws SQLException, IOException {
		conn = Connect.getConnection();
		tableInit();
		//get sample data and default schema
		AnalyzeFile.getFormat(file);
		createTable();
		startBulkLoad();
	}

	public static boolean executeUpdate(Connection conn, String command) throws SQLException {
		stmt = conn.createStatement();
		stmt.executeUpdate(command);
		System.out.println("Executed \"" + command + "\"");
		return true;
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
			for (int i = 0; i < AnalyzeFile.numCols; i++ ) {
				createString += AnalyzeFile.defaultFields[i] + " " + AnalyzeFile.defaultTypes[i] + ", ";
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
		if (AnalyzeFile.hasTitle == true) { bulkLoad += " IGNORE 1 LINES"; }
		bulkLoad += " (";
		for (int i = 0; i < AnalyzeFile.numCols - 1; i++) {
			bulkLoad += AnalyzeFile.defaultFields[i] + ", ";
		}
		bulkLoad += AnalyzeFile.defaultFields[AnalyzeFile.numCols - 1] + ") SET id = null, version = NULL";
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