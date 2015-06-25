import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
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
import java.util.Map.Entry;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class Experiment3 {

	public static String server = "jdbc:mysql://localhost:3306/";
	public static String user = "root";
	public static String password = "root";
	public static HashMap<String, double[]> s_avgs = new HashMap<String, double[]>();
	public static HashMap<String, double[]> s_stdev = new HashMap<String, double[]>();
	public static boolean first = true; //for printing headers


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

	////// SPLIT FILE //////
	public static void splitFile(File file, String directory, int b) {
		PrintWriter splitexe = null;
		Process p = null;
		try { splitexe = new PrintWriter("/Users/alyssakeimach/split.exe", "UTF-8"); } 
		catch (FileNotFoundException | UnsupportedEncodingException e) { e.printStackTrace(); }
		try { Runtime.getRuntime().exec("chmod a+x /Users/alyssakeimach/split.exe"); } 
		catch (IOException e) { e.printStackTrace(); }
		splitexe.println("split -a3 -l" + b + " " + file); //-a3 for three letter file names
		splitexe.close();
		ProcessBuilder pb = new ProcessBuilder("/Users/alyssakeimach/split.exe");
		pb.directory(new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/" + directory + "/"));
		pb.redirectErrorStream(true);
		try { p = pb.start(); } 
		catch (IOException e) { e.printStackTrace(); }
		assert pb.redirectInput() == Redirect.PIPE;
		try { assert p.getInputStream().read() == -1; }
		catch (IOException e) { e.printStackTrace(); }
	}

	public static void deleteShortFile(String directory) {
		//delete the last file in directory because likely it isnt the same length of tuples
		File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/" + directory + "/");
		File[] roots = folder.listFiles();
		Arrays.sort(roots);
		int last = roots.length - 1;
		Path shortFile = roots[last].toPath();
		try { Files.delete(shortFile); } 
		catch (IOException e) { e.printStackTrace(); }
	}

	////// LOAD RANDOM FILE //////
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

	public static File getRandom(File roots) {
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
			File rndSubFile = getRandom(rndSubDir);
			if (rndSubFile != null) {
				System.out.println(rndSubFile.getAbsolutePath() + 2);
				return rndSubFile;
			}
			sub_dir.remove(rndSubDir);
		}
		return roots;
	}

	public static File loadRandom(String tableName, String loadStmt, String directory) {
		File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/" + directory + "/");
		File[] roots = folder.listFiles();
		Random rand = new Random();
		File rndFile = getRandom(roots[rand.nextInt(roots.length)]);
		String loadFile = "LOAD DATA CONCURRENT LOCAL INFILE '" +  rndFile.getAbsolutePath()  + "' INTO TABLE " + tableName + " " + loadStmt;
		try { 
			Connection conn = getConnection();
			executeQuery(conn, loadFile); 
			conn.close();
		}
		catch (SQLException e)  { e.printStackTrace(); }
		return rndFile;
	}

	public static void removeRandom(File rndFile) {
		try {
			Path source = (Path) rndFile.toPath();
			Path target = (Path) Paths.get("/Users/alyssakeimach/Eclipse/DBconnector/splits/replacement/", rndFile.getName());
			Files.move(source, target, REPLACE_EXISTING);
		}
		catch (IOException e) { e.printStackTrace(); } //could not move file
	}

	////// BLB //////
	public static HashMap<String, double[]> getRS(ResultSet rs, int b) {
		HashMap<String, double[]> bsMap = new HashMap<String, double[]>();
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			// Get the column names, only show RS which matches userFields
			for (int col = 1; col <= numberOfColumns; col++) {
				int type = metaData.getColumnType(col);
				if ((type == Types.BIGINT) || (type == Types.DECIMAL) || (type == Types.DOUBLE) || 
						(type == Types.FLOAT) || (type == Types.NUMERIC) || (type == Types.INTEGER) || (type == Types.BOOLEAN)) {
					double[] statNums = new double[b];
					bsMap.put(metaData.getColumnLabel(col), statNums); 
				}
			}
			int row = 0;
			while (rs.next()) {
				for (int i = 1; i <= numberOfColumns; i++) { 	
					if (bsMap.containsKey(metaData.getColumnLabel(i))) {
						double[] dubs = bsMap.get(metaData.getColumnLabel(i));
						dubs[row] = rs.getInt(i);
						bsMap.put(metaData.getColumnLabel(i), dubs);
					}
				}
				row++;
			} 
		}
		catch (Exception e) { e.printStackTrace(); }
		return bsMap;
	}

	public static double mean(double[] nums, int dataPoints) {
		double mean = 0.0;
		for (int i = 0; i < dataPoints; i++) { mean += nums[i]; }
		return (mean / dataPoints);
	}

	public static void addValue(HashMap<String, double[]> map, String field, int s, int s_index, double add_value) {
		if (!map.containsKey(field)) { 
			double[] values = new double[s];
			values[s_index] = add_value;
			map.put(field, values);	
		}
		else {
			double[] values = map.get(field);
			values[s_index] = add_value;
			map.put(field, values);
		}
	}

	public static HashMap<String, double[]> standardDeviation(HashMap<String, double[]> s_avgs, int s, int s_index) {
		double var2 = 0.0;
		for (Entry<String, double[]> entry : s_avgs.entrySet()) {  
			double[] field_means = entry.getValue();
			double s_field_mean = mean(field_means, s_index + 1);
			for (int i = 0; i < field_means.length; i++) {
				var2 += Math.pow((field_means[i] - s_field_mean), 2.0);
			}
			var2 = (var2 / (s - 1)); //unbiased sample variance
			double std_dev = Math.sqrt(var2);
			addValue(s_stdev, entry.getKey(), s, s_index, std_dev);
		}
		return s_stdev;
	}

	////// MAINS //////
	public static void mainSplit(String directory, final String fileName, final int b) throws InterruptedException {
		//splitFile, deleteShortFile
		File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/" + directory + "/");
		for(File file: folder.listFiles()) file.delete();
		Thread.sleep(1000);
		final File file = new File("/Users/alyssakeimach/" + fileName);
		splitFile(file, directory, b); 
		Thread.sleep(3000);
		deleteShortFile(directory);
		Thread.sleep(500); //half second
	}

	public static void mainLoad(String directory, String tableName, String tableStmt, String loadStmt) {
		//take random sample size b from dataset
		tableInit(tableName, tableStmt);
		File rnd = loadRandom(tableName, loadStmt, directory);
		removeRandom(rnd); //without replacement)
	}

	public static void mainBLB(String tableName, int n, int b, int s, int s_index, long startTime) {
		
		//for each s make a bootstrapped sample size n
		HashMap<String, double[]> rs = new HashMap<String, double[]>();
		Connection conn = getConnection();
		try { rs = getRS(executeQuery(conn, "SELECT * FROM " + tableName), b); } 
		catch (SQLException e) { e.printStackTrace(); }
		
		//get the averages
		s_avgs = new HashMap<String, double[]>();
		for (Entry<String, double[]> entry : rs.entrySet()) {  
			double[] n_durations = new double[n];
			double[] b_durations = entry.getValue();
			//select n random durations and find the average over all n
			for (int i = 0; i < n; i++) {
				Random rand = new Random();
				int randomNum = rand.nextInt(b);
				n_durations[i] = b_durations[randomNum];
			}
			double mean = mean(n_durations, n);
			addValue(s_avgs, entry.getKey(), s, s_index, mean);
		}
		try { conn.close(); } 
		catch (SQLException e) { e.printStackTrace(); }
		
		s_stdev = standardDeviation(s_avgs, s, s_index); 
		//print out stuff
		if (first) {
			for (Entry<String, double[]> headers : s_stdev.entrySet()) { 
				System.out.print(headers.getKey() + "\tn\tb\ts\ttime\tmean\tstdev\t\t\t\t"); 
			}
			first = false;
		}
		System.out.println();

		long manualMili = 4500;
		//for (int i = 0; i < s; i++) {
			for (Entry<String, double[]> entry2 : s_stdev.entrySet()) { 
				double[] stdev = entry2.getValue();
				System.out.print("\t\t" + n + "\t" + b + "\t" + (s_index + 1) + "\t" + (((System.nanoTime() - startTime)/1000000) - manualMili) + "\t\t" + stdev[s_index] + "\t\t\t\t"); 
			}
		
		
		
	}

	public static void main(String args[]) throws SQLException, InterruptedException, FileNotFoundException  {

		int n = 21000; //tuples in 15% of original dataset
		int b = 600; //size of s in tuples
		int s = 10; //number of subsamples taken from n


		////// TRIP DATA //////
		int A = 15; //percentage of original dataset
		final String directory = "trip" + A;
		final String fileName = directory + ".csv";
		final String tableName = "Bootstrap_" + directory + "_" + s;
		final String tableStmt = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, Trip_ID BIGINT, Duration BIGINT, Start_Date VARCHAR(100), Start_Station VARCHAR(100), Start_Terminal BIGINT, End_Date VARCHAR(100), End_Station VARCHAR(100), End_Terminal BIGINT, Bike_ BIGINT, Subscription_Type VARCHAR(100), Zip_Code BIGINT, PRIMARY KEY (id_0))";
		final String loadStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (Trip_ID, Duration, Start_Date, Start_Station, Start_Terminal, End_Date, End_Station, End_Terminal, Bike_, Subscription_Type, Zip_Code) SET id_0 = NULL";

		/*
		////// REBALANCING DATA //////
		int A = 15; //percentage of N tuples
		final String directory = "rebal" + A;
		final String fileName = directory + ".csv";
		final String tableName = "Bootstrap_" + directory + "_" + k;
		final String tableStmt = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, _station_id_ BIGINT, _bikes_available_ BIGINT, _docks_available_ BIGINT, _time_ TIMESTAMP, PRIMARY KEY (id_0))";
		final String loadStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (_station_id_, _bikes_available_, _docks_available_, _time_) SET id_0 = NULL";
		 */


		long startTime = System.nanoTime();
		mainSplit(directory, fileName, b); //deletes old shit

		for (int s_index = 0; s_index < s; s_index++) {
			mainLoad(directory,tableName, tableStmt, loadStmt);
			mainBLB(tableName, n, b, s, s_index, startTime);
		}
	}

}


