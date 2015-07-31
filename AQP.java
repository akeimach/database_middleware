import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Random;

// EXPERIMENT 6
// Experiment to measure error in approximate query processing at varying k
public class AQP {

	////// GET SEQUENTIAL TUPLES FROM RANDOM POSITION //////
	public static void getSequentialTuples(File fileName, File k_fileName) throws IOException {
		Random position = new Random();
		int i = position.nextInt(1000);
		LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(fileName)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(k_fileName, true));
		while (lnr.readLine() != null) {
			if (lnr.getLineNumber() == i) { 	
				for (int tuple = 0; tuple < 100; tuple++) {
					String line = lnr.readLine() + '\n';
					bw.write(line, 0, line.length()); 
				}
			}
		}
		bw.close();
	}

	////// GET CONNECTION //////
	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dynamicDB", "root", "root");
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} 
		catch (SQLException e) { throw new IllegalStateException("ERROR: " + e.getMessage(), e); } 
		catch (InstantiationException e) { e.printStackTrace(); } 
		catch (IllegalAccessException e) { e.printStackTrace(); } 
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		return conn;
	}

	////// SEND COMMANDS TO DB //////
	public static void SQLupdate(Connection conn, String command) throws SQLException {
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(command);
	}

	////// LOAD FILE TO NEW SQL TABLE //////
	public static void SQLload(File fileName, String tableName, String createTableStmt, String loadDataStmt) throws SQLException {
		// GET CONNECTION
		Connection conn = getConnection();
		// DROP OLD TABLE
		String dropTableString = "DROP TABLE IF EXISTS " + tableName;
		SQLupdate(conn, dropTableString);
		// CREATE NEW TABLE
		String createTableString = "CREATE TABLE " + tableName + " " + createTableStmt;
		SQLupdate(conn, createTableString);
		// LOAD FILE TO TABLE
		String loadFile = "LOAD DATA CONCURRENT LOCAL INFILE '" +  fileName.getAbsolutePath()  + "' INTO TABLE " + tableName + " " + loadDataStmt;
		SQLupdate(conn, loadFile);
		// CLOSE CONNECTION
		conn.close();
	}

	////// GET RESULT SET TABLES //////
	public static void SQLresultSet(String command) throws SQLException {
		// RETRIEVE RESULT SET
		Connection conn = getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(command);
		ResultSetMetaData metaData = rs.getMetaData();
		Integer numberOfColumns = metaData.getColumnCount();
		// COLLECT COLUMN TITLES FOR NUMERICAL FIELDS
		ArrayList<String> fields = new ArrayList<>();
		for (int col = 1; col <= numberOfColumns; col++) {
			Integer type = metaData.getColumnType(col);
			if ((type == Types.BIGINT) || (type == Types.DECIMAL) || (type == Types.DOUBLE) || (type == Types.FLOAT) || (type == Types.NUMERIC) || (type == Types.INTEGER) || (type == Types.BOOLEAN)) {
				//System.out.print(metaData.getColumnLabel(col) + "\t"); 
				fields.add(metaData.getColumnLabel(col));
			}
		}
		//System.out.println();
		// PRINT THE NUMERICAL COLUMNS
		while (rs.next()) {
			for (int i = 1; i <= numberOfColumns; i++) { 	
				if (fields.contains(metaData.getColumnLabel(i))) { System.out.print(rs.getInt(i) + "\t"); }
			}
			System.out.println();
		} 
		conn.close();
	}

	////// MAIN //////
	public static void main(String args[]) throws IOException, SQLException  {

		String directory = "/Users/alyssakeimach/Eclipse/DBconnector/data/";
		String createTableStmt = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, Trip_ID BIGINT, Duration BIGINT, Start_Date VARCHAR(100), Start_Station VARCHAR(100), Start_Terminal BIGINT, End_Date VARCHAR(100), End_Station VARCHAR(100), End_Terminal BIGINT, Bike_ BIGINT, Subscription_Type VARCHAR(100), Zip_Code BIGINT, PRIMARY KEY (id_0))";
		String loadDataStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (Trip_ID, Duration, Start_Date, Start_Station, Start_Terminal, End_Date, End_Station, End_Terminal, Bike_, Subscription_Type, Zip_Code) SET id_0 = NULL";

		String tableName = "trip_data";
		String filePath = directory + tableName + ".csv";
		File fileName = new File(filePath);
		SQLload(fileName, tableName, createTableStmt, loadDataStmt);
		SQLresultSet("SELECT  MAX(Trip_ID), MAX(Duration), MAX(Start_Terminal), MAX(End_Terminal), MAX(Bike_), MAX(Zip_Code) FROM " + tableName);
		for (int j = 0; j < 25; j++) {
			Integer k = 2;
			String k_tableName = "data_k" + k;
			String k_filePath = directory + k_tableName + ".csv";
			Files.deleteIfExists(Paths.get(k_filePath));
			File k_fileName = new File(k_filePath);
			for (int i = 1; i <= k; i++) { getSequentialTuples(fileName, k_fileName); }
			SQLload(k_fileName, k_tableName, createTableStmt, loadDataStmt);
			SQLresultSet("SELECT  MAX(Trip_ID), MAX(Duration), MAX(Start_Terminal), MAX(End_Terminal), MAX(Bike_), MAX(Zip_Code) FROM " + k_tableName);
		}
	}

}


