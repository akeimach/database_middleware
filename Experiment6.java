import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Experiment6 {

	public static String server = "jdbc:mysql://localhost:3306/";
	public static String user = "root";
	public static String password = "root";
	

	////// CONNECTION //////
	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(server + Struct.dbName, user, password);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} 
		catch (SQLException e) { throw new IllegalStateException("ERROR: " + e.getMessage(), e); } 
		catch (InstantiationException e) { e.printStackTrace(); } 
		catch (IllegalAccessException e) { e.printStackTrace(); } 
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		return conn;
	}

	public static void executeUpdate(String command) throws SQLException {
		Connection conn = getConnection();
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(command);
		conn.close();
	}

	public static ResultSet executeQuery(Connection conn, String command) throws SQLException {
		Statement stmt = conn.createStatement();
		return stmt.executeQuery(command);
	}

	////// LOAD DATA TO TABLE //////
	public static void initTable(String tableName, String tableStmt) {
		try {
			String dropString = "DROP TABLE IF EXISTS " + tableName;
			executeUpdate(dropString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not drop the table");
			e.printStackTrace();
		}
		String createTableString = "CREATE TABLE " + tableName + " " + tableStmt;
		try { executeUpdate(createTableString); }
		catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
		}
	}
	
	public static void loadFile(File fileName, String tableName, String loadStmt) {
		String loadFile = "LOAD DATA CONCURRENT LOCAL INFILE '" +  fileName.getAbsolutePath()  + "' INTO TABLE " + tableName + " " + loadStmt;
		try { 
			Connection conn = getConnection();
			executeQuery(conn, loadFile); 
			conn.close();
		}
		catch (SQLException e)  { e.printStackTrace(); }
	}

		
	////// MAIN //////
	public static void main(String args[]) throws SQLException, InterruptedException, FileNotFoundException  {

		////// TRIP DATA //////
		final String subSet = "trip" + 15;
		File fileName = new File("/Users/alyssakeimach/Eclipse/DBconnector/data/" + subSet + ".csv");
		final String tableName = "randScore_" + subSet;
		final String tableStmt = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, Trip_ID BIGINT, Duration BIGINT, Start_Date VARCHAR(100), Start_Station VARCHAR(100), Start_Terminal BIGINT, End_Date VARCHAR(100), End_Station VARCHAR(100), End_Terminal BIGINT, Bike_ BIGINT, Subscription_Type VARCHAR(100), Zip_Code BIGINT, PRIMARY KEY (id_0))";
		final String loadStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (Trip_ID, Duration, Start_Date, Start_Station, Start_Terminal, End_Date, End_Station, End_Terminal, Bike_, Subscription_Type, Zip_Code) SET id_0 = NULL";
		
		initTable(tableName, tableStmt);
		loadFile(fileName, tableName, loadStmt);
	
		
		for (int i = 100; i < 20900; i += 500) {

			Connection conn = getConnection();
			ResultSet rs = executeQuery(conn, "SELECT AVG(sampleSize.id_0) FROM (SELECT * FROM " + tableName + " LIMIT " + i + ") sampleSize");
			while (rs.next()) { System.out.println(i + " " + rs.getDouble(1)); }
			conn.close(); 
			
		}
		
	}

}


