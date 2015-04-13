import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoadData {

	public static File file;
	public static String tableName;
	public static String dbName = "testDynamic";
	public static Connection conn;
	public static Statement stmt;
	public static PreparedStatement pstmt;
	public static ResultSet rs;
	public static char delimiter = ','; //default
	public static char terminator = '\n'; //default
	
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
			System.out.println("Complete: " + command);
			return true;
		
		//finally { if (stmt != null) stmt.close(); }
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
			String createString = "CREATE TABLE " + tableName + " ("; //Create new
			for (int i = 0; i < AnalyzeFile.defaultFields.length; i++ ) {
				createString += AnalyzeFile.defaultFields[i] + " " + AnalyzeFile.defaultTypes[i] + ", ";
			}
			createString += "PRIMARY KEY (" + AnalyzeFile.defaultFields[0] + "))";
			executeUpdate(conn, createString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
		}
	}
	
	public static void startBulkLoad() throws SQLException {
		String bulkLoad = "LOAD DATA INFILE '" + file.getPath() + "' INTO TABLE " + tableName + 
				" FIELDS TERMINATED BY '" + delimiter + "' LINES TERMINATED BY '" + terminator + "'";
		if (AnalyzeFile.hasTitle == true) { bulkLoad += " IGNORE 1 LINES"; }
		stmt.executeQuery(bulkLoad);
		System.out.println("Uploading file");
	}

}