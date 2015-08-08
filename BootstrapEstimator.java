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


// EXPERIMENT 6a
// Experiment to measure error in approximate query processing at varying k
public class BootstrapEstimator {

	////// GET SEQUENTIAL TUPLES FROM RANDOM POSITION //////
	public static void getSequentialTuples(File fileName, File k_fileName, Integer n, Integer seed) throws IOException {
		Random position = new Random();
		int i = position.nextInt(seed);
		LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(fileName)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(k_fileName, true));
		while (lnr.readLine() != null) {
			if (lnr.getLineNumber() == i) { 	
				for (int tuple = 0; tuple < n; tuple++) {
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
				fields.add(metaData.getColumnLabel(col));
			}
		}
		// PRINT THE NUMERICAL COLUMNS
		while (rs.next()) {
			for (int i = 1; i <= numberOfColumns; i++) { 	
				if (fields.contains(metaData.getColumnLabel(i))) { System.out.print(rs.getInt(i)); }
			}
			System.out.println();
		} 
		conn.close();
	}

	////// MAIN //////
	public static void main(String args[]) throws IOException, SQLException  {

		// trip_data
		// k = 100
		// n = 1000
		// seed = 144000 (full file)
		// query = MAX(Duration)
		String directory = "/Users/alyssakeimach/Eclipse/DBconnector/data/";
		String createTableStmt = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, Trip_ID BIGINT, Duration BIGINT, Start_Date VARCHAR(100), Start_Station VARCHAR(100), Start_Terminal BIGINT, End_Date VARCHAR(100), End_Station VARCHAR(100), End_Terminal BIGINT, Bike_ BIGINT, Subscription_Type VARCHAR(100), Zip_Code BIGINT, PRIMARY KEY (id_0))";
		String loadDataStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (Trip_ID, Duration, Start_Date, Start_Station, Start_Terminal, End_Date, End_Station, End_Terminal, Bike_, Subscription_Type, Zip_Code) SET id_0 = NULL";
		String selectOutputStmt = "AVG(Bike_)";
		Integer n = 500;
		Integer seed = 21000;//144000;

		// RANDOM SAMPLE S OF D
		String d_tableName = "trip_data";
		String d_filePath = directory + d_tableName + ".csv";
		File d_fileName = new File(d_filePath);
		SQLload(d_fileName, d_tableName, createTableStmt, loadDataStmt);
		System.out.print("\t\t\tGround truth of " + selectOutputStmt + '\t');
		SQLresultSet("SELECT  " + selectOutputStmt + " FROM " + d_tableName);

		String s_tableName = "trip_fifteen";
		String s_filePath = directory + s_tableName + ".csv";
		File s_fileName = new File(s_filePath);
		SQLload(s_fileName, s_tableName, createTableStmt, loadDataStmt);
		System.out.print("\t\t\tSample result of " + selectOutputStmt + '\t');
		SQLresultSet("SELECT  " + selectOutputStmt + " FROM " + s_tableName);

		System.out.println("Resampling distribution for " + selectOutputStmt + " where n = " + n);
		for (int k = 1; k <= 50; k++) {
			System.out.print(k+"\t");
			String k_tableName = "data_k";
			String k_filePath = directory + k_tableName + ".csv";
			Files.deleteIfExists(Paths.get(k_filePath));
			File k_fileName = new File(k_filePath);
			// BOOTSTRAP RESAMPLE OF S
			for (int i = 1; i <= k; i++) { getSequentialTuples(s_fileName, k_fileName, n, seed); }
			SQLload(k_fileName, k_tableName, createTableStmt, loadDataStmt);
			SQLresultSet("SELECT  " + selectOutputStmt + " FROM " + k_tableName);
		}
		/*
		// trip_data
		// k = 100
		// n = 500
		// seed = 144000 (full file)
		// query = AVG(Duration)
		String directory = "/Users/alyssakeimach/Eclipse/DBconnector/data/";
		String createTableStmt = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, Trip_ID BIGINT, Duration BIGINT, Start_Date VARCHAR(100), Start_Station VARCHAR(100), Start_Terminal BIGINT, End_Date VARCHAR(100), End_Station VARCHAR(100), End_Terminal BIGINT, Bike_ BIGINT, Subscription_Type VARCHAR(100), Zip_Code BIGINT, PRIMARY KEY (id_0))";
		String loadDataStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (Trip_ID, Duration, Start_Date, Start_Station, Start_Terminal, End_Date, End_Station, End_Terminal, Bike_, Subscription_Type, Zip_Code) SET id_0 = NULL";
		String selectOutputStmt = "AVG(Duration)";

		// RANDOM SAMPLE S OF D
		String s_tableName = "trip_data";
		String s_filePath = directory + s_tableName + ".csv";
		File s_fileName = new File(s_filePath);
		Integer n = 500;
		Integer seed = 144000;
		System.out.println("Resampling distribution for each k size " + n + " from S for " + selectOutputStmt);

		// BOOTSTRAP RESAMPLE OF S
		for (int k = 1; k <= 100; k++) {
			String k_tableName = "data_k";
			String k_filePath = directory + k_tableName + ".csv";
			Files.deleteIfExists(Paths.get(k_filePath));
			File k_fileName = new File(k_filePath);
			getSequentialTuples(s_fileName, k_fileName, n, seed);
			SQLload(k_fileName, k_tableName, createTableStmt, loadDataStmt);
			SQLresultSet("SELECT  " + selectOutputStmt + " FROM " + k_tableName);
		}

		// rebalancing_data
		// k = 200
		// n = 500
		// seed = 8000000 (full file)
		// query = AVG(bikes_available)
		String directory = "/Users/alyssakeimach/Eclipse/DBconnector/data/";
		String createTableStmt= "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, _station_id_ BIGINT, _bikes_available_ BIGINT, _docks_available_ BIGINT, _time_ TIMESTAMP, PRIMARY KEY (id_0))";
		String loadDataStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (_station_id_, _bikes_available_, _docks_available_, _time_) SET id_0 = NULL";
		String selectOutputStmt = "AVG(_bikes_available_)";

		// RANDOM SAMPLE S OF D
		String s_tableName = "rebalancing_data";
		String s_filePath = directory + s_tableName + ".csv";
		File s_fileName = new File(s_filePath);
		Integer n = 500;
		Integer seed = 8000000;
		System.out.println("Resampling distribution for each k size " + n + " from S for " + selectOutputStmt);

		// BOOTSTRAP RESAMPLE OF S
		for (int k = 1; k <= 200; k++) {
			String k_tableName = "data_k";
			String k_filePath = directory + k_tableName + ".csv";
			Files.deleteIfExists(Paths.get(k_filePath));
			File k_fileName = new File(k_filePath);
			getSequentialTuples(s_fileName, k_fileName, n, seed);
			SQLload(k_fileName, k_tableName, createTableStmt, loadDataStmt);
			SQLresultSet("SELECT  " + selectOutputStmt + " FROM " + k_tableName);
		}
		 */


	}

}


