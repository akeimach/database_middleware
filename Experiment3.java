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
import java.util.Map.Entry;

import com.dicelocksecurity.jdicelock.CryptoRandomStream.BaseCryptoRandomStream;
import com.dicelocksecurity.jdicelock.RandomTest.MathematicalFunctions;
import com.dicelocksecurity.jdicelock.RandomTest.RandomTestErrors;
import com.dicelocksecurity.jdicelock.RandomTest.RunsTest;


public class Experiment3 {

	public static String server = "jdbc:mysql://localhost:3306/";
	public static String user = "root";
	public static String password = "root";
	public static HashMap<String, double[]> runsMap = new HashMap<String, double[]>();
	public static int up_run = 0;
	public static int down_run = 0;
	public static int total_runs = 0;
	public static int N = 0;

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

	////// SPLIT/RANDOM FILE //////
	public static void splitFile(File file, int tuples, String subDir) {
		PrintWriter splitexe = null;
		Process p = null;
		try { splitexe = new PrintWriter("/Users/alyssakeimach/split.exe", "UTF-8"); } 
		catch (FileNotFoundException | UnsupportedEncodingException e) { e.printStackTrace(); }
		try { Runtime.getRuntime().exec("chmod a+x /Users/alyssakeimach/split.exe"); } 
		catch (IOException e) { e.printStackTrace(); }
		splitexe.println("split -a3 -l" + tuples + " " + file); //-a3 for three letter file names
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
	public static void runsTableInit(String RunsTableName, String filenameTable) {
		try {
			String dropString = "DROP TABLE IF EXISTS " + RunsTableName;
			executeUpdate(dropString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not drop the table");
			e.printStackTrace();
		}
		String createTableString = "CREATE TABLE " + RunsTableName + " " + filenameTable;
		try { executeUpdate(createTableString); }
		catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
		}
	}

	public static void startRunsLoad(String RunsTableName, String filenameLoad, String subDir) {
		File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/" + subDir + "/");
		File[] roots = folder.listFiles();
		Random rand = new Random();
		File rndFile = getRandFile(roots[rand.nextInt(roots.length)]);
		String loadFile = "LOAD DATA CONCURRENT LOCAL INFILE '" +  rndFile.getAbsolutePath()  + "' INTO TABLE " + RunsTableName + " " + filenameLoad;
		try { executeQuery(loadFile); }
		catch (SQLException e)  { e.printStackTrace(); }
	}

	////// RUNS RESULT SETS //////
	public static void getRUNSnums(ResultSet rs, HashMap<String, double[]> runsMap, int tuples) {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			// Get the column names, only show RS which matches userFields
			for (int col = 1; col <= numberOfColumns; col++) {
				int type = metaData.getColumnType(col);
				if ((type == Types.BIGINT) || (type == Types.DECIMAL) || (type == Types.DOUBLE) || 
						(type == Types.FLOAT) || (type == Types.NUMERIC) || (type == Types.INTEGER) || (type == Types.BOOLEAN)) {
					double[] statNums = new double[tuples];
					runsMap.put(metaData.getColumnLabel(col), statNums); 
				}
			}
			// Get all rows.
			int row = 0;
			while (rs.next()) {
				for (int i = 1; i <= numberOfColumns; i++) { 	
					if (runsMap.containsKey(metaData.getColumnLabel(i))) {
						double[] dubs = runsMap.get(metaData.getColumnLabel(i));
						dubs[row] = rs.getInt(i);
						runsMap.put(metaData.getColumnLabel(i), dubs);
					}
				}
				row++;
			} 
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	////// RUNS MATH //////
	public static double calculateMean(double[] x) {
		double mean = 0;
		for (int i = 0; i < N; i++) { mean += x[i]; }
		return (mean / N);
	}

	public static void calculateRuns(double[] x, double mean) {
		boolean prev_up = false;
		boolean prev_down = false;
		for (int i = 0; i < N; i++) {
			if (x[i] > mean) { 
				up_run++;
				if (prev_down) { total_runs++; }
				prev_up = true;
			}
			else if (x[i] < mean) { 
				down_run++;
				if (prev_up) { total_runs++; }
				prev_down = true;
			}	
		}
		System.out.println("Up runs: " + up_run + ", Down runs: " + down_run + ", Total runs: " + total_runs);
	}

	public static void calculateVariance() {
		double a = 0.5 + (2.0 * up_run * down_run)/(N); //1 instead of .5?
		System.out.println("Expected mean: " + a);
		double s2 = ((2.0 * up_run * down_run) * ((2.0 * up_run * down_run) - N))/(Math.pow(N, 2) * (N - 1));
		System.out.println(Math.sqrt(s2));
		double z = (total_runs - ((2 * up_run * down_run) / N) - 0.5) / (Math.sqrt(s2));
		System.out.println(z);
		//double p = mathFuncs;

	}

	@SuppressWarnings("unused")
	public static boolean IsRandom(double[] bitStream) {
		int i, r[];
		double product, sum;
		MathematicalFunctions mathFuncs = new MathematicalFunctions();
		double pi = 0.0;
		boolean random = true;
		//bitStream.SetBitPosition(0);
		RandomTestErrors error = RandomTestErrors.NoError;
		r = new int[bitStream.length];
		double alpha = 0.0;
		sum = 0.0;
		for (i = 0; i < bitStream.length; i++)
			sum += bitStream[i];
		pi = sum / bitStream.length;
		for (i = 0; i < bitStream.length - 1; i++) {
			if (bitStream[i] == bitStream[i + 1])
				r[i] = 0;
			else
				r[i] = 1;
		}
		total_runs = 0;
		for (i = 0; i < bitStream.length - 1; i++)
			total_runs += r[i];
		total_runs++;
		product = pi * (1.e0 - pi);
		double argument = Math.abs(total_runs - 2.e0 * bitStream.length * product) / (2.e0 * Math.sqrt(2.e0 * bitStream.length) * product);
		double pValue = mathFuncs.ErFc(argument);
		if (pValue < alpha) {
			random = false;
		} else {
			random = true;
		}
		r = null;
		if (mathFuncs.isNegative(pValue) || mathFuncs.isGreaterThanOne(pValue)) {
			random = false;
			error = RandomTestErrors.PValueOutOfRange;
		}
		System.out.println(total_runs); //RUNS
		System.out.println(product); //"EXPECTED MEAN: " + 
		System.out.println(argument); //"VARIANCE: " + 
		System.out.println(pValue); //"Z-SCORE: " + 
		//System.out.println("ALPHA: " + alpha);
		if (random) System.out.println("RANDOM");
		else { System.out.println("NOT RANDOM"); }
		return random;
	}

	////// MAIN //////
	public static void main(String args[])  {


		//double[] rTest = {0.5, 0.46710000000000007, 0.0, 0.12369999999999992,0.5,0.10614999999999997,0.0,0.10504999999999998,0.1875,0.0,0.1875};

		//IsRandom(rTest);

		/*
		N = rTest.length;
		double mean = calculateMean(rTest);
		calculateRuns(rTest, mean);
		calculateVariance();

		 */
		////// TRIP DATA //////
		int percent = 15;
		final String subDir = "trip" + percent;
		final int tuples = 100;
		/*
		final File file = new File("/Users/alyssakeimach/" + subDir + ".csv");
		Thread KSsplitThread = new Thread() {
			public void run() { 
				splitFile(file, tuples, subDir); 
				System.out.println("DONE");
			}
		};
		KSsplitThread.setName("KSsplitThread");
		KSsplitThread.start();

		 */
		final String tableName = subDir + "_runsTest";
		final String createFiletable = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, Trip_ID BIGINT, Duration BIGINT, Start_Date VARCHAR(100), Start_Station VARCHAR(100), Start_Terminal BIGINT, End_Date VARCHAR(100), End_Station VARCHAR(100), End_Terminal BIGINT, Bike_ BIGINT, Subscription_Type VARCHAR(100), Zip_Code BIGINT, PRIMARY KEY (id_0))";
		final String loadFiletable =  "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (Trip_ID, Duration, Start_Date, Start_Station, Start_Terminal, End_Date, End_Station, End_Terminal, Bike_, Subscription_Type, Zip_Code) SET id_0 = NULL";
		Thread KSstatsThread = new Thread() {
			public void run() {
				String RunsTableName = subDir + "_runsTest";
				runsTableInit(RunsTableName, createFiletable);
				startRunsLoad(RunsTableName, loadFiletable, subDir); 
				runsMap = new HashMap<String, double[]>();
				try { getRUNSnums(executeQuery("SELECT * FROM " + RunsTableName), runsMap, tuples); } 
				catch (SQLException e) { e.printStackTrace(); }
				for (Entry<String, double[]> entry : runsMap.entrySet()) {    
					String key = entry.getKey();
					double[] values = entry.getValue();
					
					System.out.println(key + " " + values.length);
					IsRandom(values);
				}
				System.out.println();
			}

		};
		KSstatsThread.setName("KSstatsThread");
		KSstatsThread.start();

	}


}
