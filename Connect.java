import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.csv.CSVParser;

public class Connect {

	private final static String userName = "root";
	private final static String password = "root";
	private final static String serverName = "localhost";
	private final static int portNumber = 8889;
	private final static String dbName = "testDynamic";
	private final static String tableName = "JDBC_TEST"; //set to filename

	public static Connection getConnection() throws SQLException {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", userName);
		connectionProps.put("password", password);

		conn = DriverManager.getConnection("jdbc:mysql://"
				+ serverName + ":" + portNumber + "/" + dbName,
				connectionProps);

		return conn;
	}

	//Run a SQL command which does not return a recordset:
	//CREATE/INSERT/UPDATE/DELETE/DROP/etc.
	public static boolean executeUpdate(Connection conn, String command) throws SQLException {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(command); // This will throw a SQLException if it fails
			return true;
		} finally {

			// This will run whether we throw an exception or not
			if (stmt != null) { stmt.close(); }
		}
	}

	// Connect to MySQL and do some stuff.
	@SuppressWarnings("finally")
	public static void runDB() {
		// Connect to MySQL
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			System.out.println("Connected to database");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not connect to the database");
			e.printStackTrace();
			return;
		}
		// Drop the table
		try {
			String dropString = "DROP TABLE " + tableName;
			//stmt.executeQuery(dropString);
			executeUpdate(conn, dropString);
			System.out.println("Dropped the table");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not drop the table");
			e.printStackTrace();
			return;
		}

		// Create a table
		try {
			String createString = "CREATE TABLE " + tableName + " (";
			for (int i = 0; i < GUI.maxCols - 1; i++ ) {
				createString += GUI.finalNames[i] + " " + GUI.finalVals[i] + " NOT NULL, ";
			}
			createString += "PRIMARY KEY (" + GUI.finalNames[0] + "))";
			executeUpdate(conn, createString);
			System.out.println(createString);
		} catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
			return;
		}

		try {	
			File file = new File(GUI.path.getAbsolutePath());
			file.createNewFile();
			FileReader input = new FileReader(file); 
			CSVParser parser = new CSVParser(input);
			// String[] values = parser.getLine(); 
			String query = null;
			String[] val = parser.getLine();
			while (val != null) {

				query = "INSERT INTO " + tableName + " VALUES ('";
				int colCount = 0;
				// System.out.println(parser.getLine());
				for (String col : val) {
					//if (col.equals(null)) { break; }
					if (colCount < GUI.maxCols - 1) { query += col + "', '"; }
					else { query += col + "')"; }
					colCount++;
				}

				System.out.println(query);
				stmt.addBatch(query);

				val = parser.getLine();
				if (val == null) {  return; }
			}

			stmt.executeBatch();
			stmt.close();
			input.close();
		} finally {
			System.out.println("Insert data complete");
			return;
		}
	}
}