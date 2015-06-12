import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ProcessBuilder.Redirect;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;


public class Experiment5 {

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
		Connection conn = null;
		conn = getConnection();
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(command);
		return;
	}

	public static ResultSet executeQuery(String command) throws SQLException {
		Connection conn = null;
		conn = getConnection();
		Statement stmt = conn.createStatement();
		return stmt.executeQuery(command);
	}

	public static long executeCountQuery(String command) throws SQLException {
		Connection conn = null;
		conn = getConnection();
		Statement stmt = conn.createStatement();
		return stmt.executeUpdate(command);
		//return stmt.getUpdateCount();
	}


	////// SPLIT/RANDOM FILE //////
	public static void splitFile(File file, int S_i, String subDir) {
		PrintWriter splitexe = null;
		Process p = null;
		try { splitexe = new PrintWriter("/Users/alyssakeimach/split.exe", "UTF-8"); } 
		catch (FileNotFoundException | UnsupportedEncodingException e) { e.printStackTrace(); }
		try { Runtime.getRuntime().exec("chmod a+x /Users/alyssakeimach/split.exe"); } 
		catch (IOException e) { e.printStackTrace(); }
		splitexe.println("split -a3 -l" + S_i + " " + file); //-a3 for three letter file names
		splitexe.close();
		ProcessBuilder pb = new ProcessBuilder("/Users/alyssakeimach/split.exe");
		pb.directory(new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/" + subDir + "/"));
		pb.redirectErrorStream(true);
		try { p = pb.start(); } 
		catch (IOException e) { e.printStackTrace(); }
		assert pb.redirectInput() == Redirect.PIPE;
		try { assert p.getInputStream().read() == -1; }
		catch (IOException e) { e.printStackTrace(); }
	}

	public static File getRandFile(File roots) {
		//check if multiple files in directory
		if (roots.isFile() || roots.list().length == 0) { return roots; }
		File[] files = roots.listFiles();
		List<File> sub_dir = new ArrayList<File>(Arrays.asList(files));
		Iterator<File> fit = sub_dir.iterator();
		while (fit.hasNext()) {
			if (!fit.next().isDirectory()) { fit.remove(); }
		}
		Random rand = new Random();
		while (!sub_dir.isEmpty()) {
			File rndSubDir = sub_dir.get(rand.nextInt(sub_dir.size()));
			File rndSubFile = getRandFile(rndSubDir);
			if (rndSubFile != null) {
				System.out.println(rndSubFile.getAbsolutePath() + 2);
				return rndSubFile;
			}
			sub_dir.remove(rndSubDir);
		}
		return roots;
	}

	//////LOAD FILE //////
	public static void tableInit(String tableName, String tableStmt) {
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

	public static void loadRandom(String tableName, String loadStmt, String directory) {
		File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/" + directory + "/");
		File[] roots = folder.listFiles();
		Random rand = new Random();
		File rndFile = getRandFile(roots[rand.nextInt(roots.length)]);
		String loadFile = "LOAD DATA CONCURRENT LOCAL INFILE '" +  rndFile.getAbsolutePath()  + "' INTO TABLE " + tableName + " " + loadStmt;
		try { executeQuery(loadFile); }
		catch (SQLException e)  { e.printStackTrace(); }
	}

	public static void mainSplit(final String directory, final int tuples) {
		final File file = new File("/Users/alyssakeimach/" + directory + ".csv");
		splitFile(file, tuples, directory); 
		System.out.println("DONE");
	}

	public static void mainLoad(String directory, String tableName, String tableStmt, String loadStmt) {
		tableInit(tableName, tableStmt);
		loadRandom(tableName, loadStmt, directory); 
	}

	public static void print(Object in) {
		System.out.println(in);
	}
	
	public static ResultSet infoSchema(String tableName) throws SQLException {
		String orig = Struct.dbName;
		Struct.dbName = "information_schema";
		String command = "SELECT COLUMN_NAME FROM information_schema.columns WHERE  table_name = '" + tableName + "' ORDER  BY ordinal_position";
		ResultSet stats = executeQuery(command);
		Struct.dbName = orig;
		//while (stats.next()) { schemainfo = stats.getLong(1); }
		return stats;
	}
	////// MAIN //////
	public static void main(String args[]) throws SQLException  {

		////// TRIP DATA //////
		int percent = 10;
		final int tuples = 10000;
		final String directory = "trip" + percent;
		String tableName = "predictive_" + directory;
		String tableStmt = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, Trip_ID BIGINT, Duration BIGINT, Start_Date VARCHAR(100), Start_Station VARCHAR(100), Start_Terminal BIGINT, End_Date VARCHAR(100), End_Station VARCHAR(100), End_Terminal BIGINT, Bike_ BIGINT, Subscription_Type VARCHAR(100), Zip_Code BIGINT, PRIMARY KEY (id_0))";
		String loadStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' IGNORE 1 LINES (Trip_ID, Duration, Start_Date, Start_Station, Start_Terminal, End_Date, End_Station, End_Terminal, Bike_, Subscription_Type, Zip_Code) SET id_0 = NULL";

		//mainSplit(directory, tuples);
		//mainLoad(directory, tableName, tableStmt, loadStmt);



		ResultSet rs = executeQuery("SELECT COUNT(*) AS total FROM " + tableName);
		long totalRows = 0;
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			System.out.println(numberOfColumns);
			while (rs.next()) {
				for (int i = 1; i <= numberOfColumns; i++) { 
					totalRows = (long) rs.getObject(i);
					System.out.println(rs.getObject(i));
				}
			} 
		}
			catch (Exception e) {
				e.printStackTrace();
			}

		//set chunk size
		  
		  int chunkSize = 100;

		  //Total number of loops
		 double totalLoops = Math.floor(totalRows/chunkSize);

		 System.out.println(totalLoops);
		 
		long m = totalRows;
		ResultSet stats = infoSchema(tableName);
		//ResultSet vars = executeQuery("SELECT COLUMN_NAME FROM information_schema.columns WHERE  table_name = '" + tableName + "' ORDER  BY ordinal_position");
		ResultSetMetaData md = stats.getMetaData();
		int numCols = md.getColumnCount();
		
		int theta = numCols;
		 print(theta);
		System.out.println(theta);
		 double alpha = 0.01;



		}
	}