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
//import org.eclipse.core.runtime.Path;
//import java.io.FileOutputStream;
//import java.io.PrintStream;

public class Experiment3 {

	public static String server = "jdbc:mysql://localhost:3306/";
	public static String user = "root";
	public static String password = "root";
	public static HashMap<String, double[]> k_avgs = new HashMap<String, double[]>();
	public static HashMap<String, double[]> k_bootstrap_variance = new HashMap<String, double[]>();


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

	////// MAKE TABLE //////
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

	//////LOAD RANDOM FILE //////
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

	public static File loadRandom(String tableName, String loadStmt, String directory) {
		File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/" + directory + "/");
		File[] roots = folder.listFiles();
		Random rand = new Random();
		File rndFile = getRandFile(roots[rand.nextInt(roots.length)]);
		String loadFile = "LOAD DATA CONCURRENT LOCAL INFILE '" +  rndFile.getAbsolutePath()  + "' INTO TABLE " + tableName + " " + loadStmt;
		try { 
			Connection conn = getConnection();
			executeQuery(conn, loadFile); 
			conn.close();
		}
		catch (SQLException e)  { e.printStackTrace(); }
		return rndFile;
	}
	
	public static void removeRandom(File rndFile, String directory) {
		try {
			Path source = (Path) rndFile.toPath();
			Path target = (Path) Paths.get("/Users/alyssakeimach/Eclipse/DBconnector/splits/replacement/", rndFile.getName());
			Files.move(source, target, REPLACE_EXISTING);
		}
		catch (IOException e) { e.printStackTrace(); } //could not move file
	}

	/*
	////// BOOTSTRAP MATH //////
	public static HashMap<String, double[]> getRS(ResultSet rs, int S_i) {
		HashMap<String, double[]> bsMap = new HashMap<String, double[]>();
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			// Get the column names, only show RS which matches userFields
			for (int col = 1; col <= numberOfColumns; col++) {
				int type = metaData.getColumnType(col);
				if ((type == Types.BIGINT) || (type == Types.DECIMAL) || (type == Types.DOUBLE) || 
						(type == Types.FLOAT) || (type == Types.NUMERIC) || (type == Types.INTEGER) || (type == Types.BOOLEAN)) {
					double[] statNums = new double[S_i];
					bsMap.put(metaData.getColumnLabel(col), statNums); 
				}
			}
			// Get all rows
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

	public static double mean(double[] nums) {
		double mean = 0.0;
		for (int i = 0; i < nums.length; i++) { mean += nums[i]; }
		return (mean / nums.length);
	}

	public static void addValue(HashMap<String, double[]> map, String field, int k, int k_index, double add_value) {
		if (!map.containsKey(field)) { 
			double[] values = new double[k];
			values[k_index] = add_value;
			map.put(field, values);	
		}
		else {
			double[] values = map.get(field);
			values[k_index] = add_value;
			map.put(field, values);
		}
	}

	public static HashMap<String, double[]> bootstrapVariance(HashMap<String, double[]> k_avgs, int k, int k_index) {

		double var2 = 0.0;
		for (Entry<String, double[]> entry : k_avgs.entrySet()) {  

			double[] field_means = entry.getValue();
			double k_field_mean = mean(field_means);

			for (int i = 0; i < field_means.length; i++) {
				var2 += Math.pow((field_means[i] - k_field_mean), 2.0);
				addValue(k_bootstrap_variance, entry.getKey(), k, k_index, var2);
			}
			var2 = (var2 / (k - 1));
		}		
		System.out.println(var2 + "\t" + Math.sqrt(var2) + "\t" + mean(k_avgs.get("Duration")));
		return k_bootstrap_variance;
	}

*/
	////// MAINS //////
	public static void mainRestart(String directory) {
		File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/" + directory + "/");
		for(File file: folder.listFiles()) file.delete();
	}

	public static void mainSplit(final String fileName, String directory, final int S_i) {
		final File file = new File("/Users/alyssakeimach/" + fileName);
		splitFile(file, directory, S_i); 
	}

	public static void mainStats(String directory, String k_tableName, String tableStmt, String loadStmt, int n, int s, int b) throws SQLException, FileNotFoundException {

		k_avgs = new HashMap<String, double[]>();
		k_bootstrap_variance = new HashMap<String, double[]>();

		//load and measure k tables
		//System.out.println("SUM(Trip_ID)\tSUM(Duration)\tSUM(Start_Terminal)\tSUM(End_Terminal)\tSUM(Bike_)\tSUM(Zip_Code)");
		for (int s_index = 0; s_index < s; s_index++) {

			tableInit(k_tableName, tableStmt);
			loadRandom(k_tableName, loadStmt, directory); //removes used file to other folder after

			HashMap<String, double[]> rs = new HashMap<String, double[]>();
			Connection conn = getConnection();
			rs = getRS(executeQuery(conn, "SELECT Duration FROM " + k_tableName), b);
			//rs = getRS(executeQuery(conn, "SELECT AVG(Trip_ID), AVG(Duration), AVG(Start_Terminal), AVG(End_Terminal), AVG(Bike_), AVG(Zip_Code) FROM " + k_tableName), b);
			//rs = getRS(executeQuery(conn, "SELECT SUM(Trip_ID), SUM(Duration), SUM(Start_Terminal), SUM(End_Terminal), SUM(Bike_), SUM(Zip_Code) FROM " + k_tableName), S_i);
			//for (Entry<String, double[]> entry : rs.entrySet()) {  

			double[] b_durations = rs.get("Duration");
			//String field = entry.getKey();
			//double[] b_durations = entry.getValue();

			//select n random durations and find the average over all n
			double[] n_durations = new double[n];
			for (int i = 0; i < n; i++) {
				Random rand = new Random();
				int randomNum = rand.nextInt(b);
				//System.out.println(i + " " + randomNum);
				n_durations[i] = b_durations[randomNum];
			}

			double mean = mean(n_durations);
			addValue(k_avgs, "Duration", s, s_index, mean);
			
		conn.close();
		//System.out.println();
		k_bootstrap_variance = bootstrapVariance(k_avgs, s, s_index);
	}

	/*
		System.out.println();
		///PRINT TRIP DATA///
		double[] zip = new double[k];
		double[] start = new double[k];
		double[] trip = new double[k];
		double[] dur = new double[k];
		//double[] id = new double[k];
		double[] bike = new double[k];
		double[] end = new double[k];
		//, start, trip, dur, id, bike, end;
		System.out.print("k" + "\t");
		/*
		for (Entry<String, double[]> entry : k_bootstrap_variance.entrySet()) {  

			if (entry.getKey().equals("AVG(Zip_Code)")) {
				zip = entry.getValue();
			}
			else if (entry.getKey().equals("AVG(Start_Terminal)")) {
				start = entry.getValue();
			}
			else if (entry.getKey().equals("AVG(Trip_ID)")) {
				trip = entry.getValue();
			}
			else if (entry.getKey().equals("AVG(Duration)")) {
				dur = entry.getValue();
			}
			else if (entry.getKey().equals("AVG(Bike_)")) {
				bike = entry.getValue();
			}
			else if (entry.getKey().equals("AVG(End_Terminal)")) {
				end = entry.getValue();
			}
			System.out.print(entry.getKey() + "\t");
		}

		for (Entry<String, double[]> entry : k_bootstrap_variance.entrySet()) {  

			if (entry.getKey().equals("SUM(Zip_Code)")) {
				zip = entry.getValue();
			}
			else if (entry.getKey().equals("SUM(Start_Terminal)")) {
				start = entry.getValue();
			}
			else if (entry.getKey().equals("SUM(Trip_ID)")) {
				trip = entry.getValue();
			}
			else if (entry.getKey().equals("SUM(Duration)")) {
				dur = entry.getValue();
			}
			else if (entry.getKey().equals("SUM(Bike_)")) {
				bike = entry.getValue();
			}
			else if (entry.getKey().equals("SUM(End_Terminal)")) {
				end = entry.getValue();
			}
			System.out.print(entry.getKey() + "\t");
		}
		for (int i = 0; i < k; i++) {
			System.out.println();
			System.out.print(i+1 + "\t" + zip[i] + "\t" + start[i] + "\t" + trip[i] + "\t" + dur[i] + "\t" + bike[i] + "\t" + end[i] );
		}
	 */
}



public static void main(String args[]) throws SQLException, InterruptedException, FileNotFoundException  {


	int n = 21000;
	int s = 30;
	int b = 500;

	////// TRIP DATA //////
	int A = 15; //percentage of N tuples
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

	//PrintStream out = new PrintStream(new FileOutputStream(directory + "_exp2_output.txt"));
	//System.setOut(out);



	//for (int k = 2; k < 20; k++) {
	mainRestart(directory);
	Thread.sleep(1000);
	mainSplit(fileName, directory, b);
	Thread.sleep(3000);
	mainStats(directory, tableName, tableStmt, loadStmt, n, s, b);
	//}
}

}


