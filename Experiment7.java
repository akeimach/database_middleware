import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;


//Experiment to get cluster sample from text file--looking for efficient way to jump to lines in file

public class Experiment7 {

	public static String server = "jdbc:mysql://localhost:3306/";
	public static String user = "root";
	public static String password = "root";

	////// MAKE CLUSTER SAMPLE //////
	public static void systematicSample(File N, File S2, Integer spacing) throws IOException {
		LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(N)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(S2));
		while (lnr.readLine() != null) {
			if (((lnr.getLineNumber()) % spacing) == 0) { 
				String line = lnr.readLine() + '\n';
				bw.write(line, 0, line.length()); 
			}
		}
		bw.close();
	}

	//////CONNECTION //////
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

	public static void SQLupdate(Connection conn, String command) throws SQLException {
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(command);
	}

	public static ResultSet SQLresultSet(Connection conn, String command) throws SQLException {
		Statement stmt = conn.createStatement();
		return stmt.executeQuery(command);
	}

	
	public static void initTable(File fileName, String tableName, String createTableStmt, String loadDataStmt) throws SQLException {
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
	public static void getRS(ResultSet rs) {
		try {
			ArrayList<String> fields = new ArrayList<>();
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			for (int col = 1; col <= numberOfColumns; col++) {
				int type = metaData.getColumnType(col);
				if ((type == Types.BIGINT) || (type == Types.DECIMAL) || (type == Types.DOUBLE) || 
						(type == Types.FLOAT) || (type == Types.NUMERIC) || (type == Types.INTEGER) || (type == Types.BOOLEAN)) {
					System.out.print(metaData.getColumnLabel(col) + "\t\t"); 
					fields.add(metaData.getColumnLabel(col));
				}
			}
			System.out.println();
			while (rs.next()) {
				for (int i = 1; i <= numberOfColumns; i++) { 	
					if (fields.contains(metaData.getColumnLabel(i))) { System.out.print(rs.getInt(i) + "\t\t"); }
				}
				System.out.println();
			} 
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	////// MAIN //////
	public static void main(String args[]) throws IOException  {

		//String dataSubSet = "trip5";
		//String dataSet = "trip_data";
		//String clusterSet = "clustered_trip_data";
		String directory = "/Users/alyssakeimach/Eclipse/DBconnector/data/";
		File N = new File(directory + "trip_data.csv");
		File S1 = new File(directory + "trip_five.csv");
		File S2 = new File(directory + "trip_cluster.csv");
		Integer spacing = 100;
		String S1_tableName = "S1_trip";
		String S2_tableName = "S2_trip";
		String createTableStmt = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, Trip_ID BIGINT, Duration BIGINT, Start_Date VARCHAR(100), Start_Station VARCHAR(100), Start_Terminal BIGINT, End_Date VARCHAR(100), End_Station VARCHAR(100), End_Terminal BIGINT, Bike_ BIGINT, Subscription_Type VARCHAR(100), Zip_Code BIGINT, PRIMARY KEY (id_0))";
		String loadDataStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (Trip_ID, Duration, Start_Date, Start_Station, Start_Terminal, End_Date, End_Station, End_Terminal, Bike_, Subscription_Type, Zip_Code) SET id_0 = NULL";


		systematicSample(N, S2, spacing);

		initTable(dataSubSet, createTableStmt);
		loadFile(new File(directory + dataSubSet + ".csv"), dataSubSet, loadDataStmt);
		
		initTable(clusterSet, createTableStmt);
		loadFile(new File(directory + clusterSet + ".csv"), clusterSet, loadDataStmt);

		PrintStream out1 = new PrintStream(new FileOutputStream(directory + "trip5_output.txt"));
		System.setOut(out1);

		try {
			Connection conn = getConnection();
			getRS(executeQuery(conn, "SELECT * FROM " + dataSubSet)); 
			conn.close();
		} 
		catch (SQLException e) { e.printStackTrace(); }


		PrintStream out2 = new PrintStream(new FileOutputStream(directory + "clustered_trip_data_output.txt"));
		System.setOut(out2);

		try {
			Connection conn = getConnection();
			getRS(executeQuery(conn, "SELECT * FROM " + clusterSet)); 
			conn.close();
		} 
		catch (SQLException e) { e.printStackTrace(); }


	}

}


