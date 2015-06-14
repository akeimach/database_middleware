import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
import org.moeaframework.util.statistics.OrdinalStatisticalTest;
import org.moeaframework.util.statistics.RankedObservation;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;

/*
The Kruskal-Wallis One-Way Analysis of Variance by Ranks is a non-parametric
statistical test determining if (at least) two out of K >= 2 populations have
differing medians.
	Null Hypothesis: All populations have equal medians.
	Alternative Hypothesis: Not all populations have equal medians.
	Assumptions:
		Samples are randomly selected from their corresponding populations 
		Samples are independent
		The dependent variable (value being sampled) is continuous
		The underlying distributions of the populations are identical in shape
Assumption #1: Your dependent variable should be measured at the ordinal or continuous level (i.e., interval or ratio). 
			Examples of ordinal variables include Likert scales (e.g., a 7-point scale from "strongly agree" through to "strongly disagree"), 
			amongst other ways of ranking categories (e.g., a 3-pont scale explaining how much a customer liked a product, ranging from "Not very much", to "It is OK", to "Yes, a lot"). 
			Examples of continuous variables include revision time (measured in hours), intelligence (measured using IQ score), exam performance (measured from 0 to 100), weight (measured in kg), and so forth.
Assumption #2: Your independent variable should consist of two or more categorical, independent groups. Typically, a Kruskal-Wallis H test is used when you have three or more categorical, independent groups, but it can be used for just two groups (i.e., a Mann-Whitney U test is more commonly used for two groups). Example independent variables that meet this criterion include ethnicity (e.g., three groups: Caucasian, African American and Hispanic), physical activity level (e.g., four groups: sedentary, low, moderate and high), profession (e.g., five groups: surgeon, doctor, nurse, dentist, therapist), and so forth.
Assumption #3: You should have independence of observations, which means that there is no relationship between the observations in each group or between the groups themselves. For example, there must be different participants in each group with no participant being in more than one group. This is more of a study design issue than something you can test for, but it is an important assumption of the Kruskal-Wallis H test. If your study fails this assumption, you will need to use another statistical test instead of the Kruskal-Wallis H test (e.g., a Friedman test). If you are unsure whether your study meets this assumption, you can use our Statistical Test Selector, which is part of our enhanced content.
 */

public class Experiment2 extends OrdinalStatisticalTest {

	public static String server = "jdbc:mysql://localhost:3306/";
	public static String user = "root";
	public static String password = "root";
	public static String firstFile = null;
	public static boolean invalFile = false;
	public static HashMap<String, double[]> ksMap1 = new HashMap<String, double[]>();
	public static HashMap<String, double[]> ksMap2 = new HashMap<String, double[]>();

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
	public static void splitFile(File file, String directory, int S_i) {
		PrintWriter splitexe = null;
		Process p = null;
		try { splitexe = new PrintWriter("/Users/alyssakeimach/split.exe", "UTF-8"); } 
		catch (FileNotFoundException | UnsupportedEncodingException e) { e.printStackTrace(); }
		try { Runtime.getRuntime().exec("chmod a+x /Users/alyssakeimach/split.exe"); } 
		catch (IOException e) { e.printStackTrace(); }
		splitexe.println("split -a3 -l" + S_i + " " + file); //-a3 for three letter file names
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

	public static void loadRandom(String tableName, String loadStmt, String directory) {
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
	}

	////// KS RESULT SETS //////
	public static void getKSnums(ResultSet rs, HashMap<String, double[]> ksMap, int S_i) {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			// Get the column names, only show RS which matches userFields
			for (int col = 1; col <= numberOfColumns; col++) {
				int type = metaData.getColumnType(col);
				if ((type == Types.BIGINT) || (type == Types.DECIMAL) || (type == Types.DOUBLE) || 
						(type == Types.FLOAT) || (type == Types.NUMERIC) || (type == Types.INTEGER) || (type == Types.BOOLEAN)) {
					double[] statNums = new double[S_i];
					ksMap.put(metaData.getColumnLabel(col), statNums); 
				}
			}
			// Get all rows.
			int row = 0;
			while (rs.next()) {
				for (int i = 1; i <= numberOfColumns; i++) { 	
					if (ksMap.containsKey(metaData.getColumnLabel(i))) {
						double[] dubs = ksMap.get(metaData.getColumnLabel(i));
						dubs[row] = rs.getInt(i);
						ksMap.put(metaData.getColumnLabel(i), dubs);
					}
				}
				row++;
			} 
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * Constructs a Kruskal-Wallis test with the specified number of groups.
	 * 
	 * @param numberOfGroups the number of groups being tested
	 */
	public Experiment2(int numberOfGroups) {
		super(numberOfGroups);
		if (numberOfGroups <= 1) { throw new IllegalArgumentException("requires two or more groups"); }
	}

	// make method public
	@Override
	public void add(double value, int group) {
		super.add(value, group);
	}

	// make method public
	@Override
	public void addAll(double[] values, int group) {
		super.addAll(values, group);
	}

	//returns the chi-squared approximation of the Kruskal-Wallis test
	double H() {
		int[] n = new int[numberOfGroups];
		double[] rbar = new double[numberOfGroups];
		for (RankedObservation observation : data) {
			n[observation.getGroup()]++;
			rbar[observation.getGroup()] += observation.getRank();
		}
		double H = 0.0;
		for (int i = 0; i < numberOfGroups; i++) {
			H += Math.pow(rbar[i], 2.0) / n[i];
		}
		int N = data.size();
		return 12.0 / (N * (N + 1)) * H - 3.0 * (N + 1);
	}

	//Computes + returns the correction factor for ties
	double C() {
		int N = data.size();
		double C = 0.0;
		int i = 0;
		while (i < N) {
			int j = i + 1;
			while ((j < N) && (data.get(i).getValue() == data.get(j).getValue())) { j++; }
			C += Math.pow(j - i, 3.0) - (j - i);
			i = j;
		}
		return 1 - C / (Math.pow(N, 3.0) - N);
	}

	public boolean test(double alpha) {
		System.out.println("alpha: " + alpha);
		update();
		ChiSquaredDistribution dist = new ChiSquaredDistribution(numberOfGroups - 1);
		double H = H();
		double C = C();
		System.out.println("H: " + H);
		System.out.println("C: " + C);
		if (C == 0.0) { return false; } // all observations the same
		System.out.println(1.0 - dist.cumulativeProbability(H / C));
		boolean i = 1.0 - dist.cumulativeProbability(H / C) < alpha;
		System.out.println(i);
		return i;
	}

	////// KS MATH //////
	private static double cdf(final double[] array, final double x) {
		final int n = array.length;
		int index = Arrays.binarySearch(array, x);
		if (index >= 0) {
			while ((index < (n - 1)) && (array[index+1] == x)) { ++index; }
		}
		return index >= 0 ? (index + 1d) / n : (-index - 1d) / n;
	}

	public static double kolmogorovSmirnovStatistic(double[] x, double[] y) {
		//verify arrays
		if ((x == null) || (y == null)) { throw new NullArgumentException(LocalizedFormats.NULL_NOT_ALLOWED); }
		if ((x.length < 2) || (y.length < 2)) { throw new InsufficientDataException(LocalizedFormats.INSUFFICIENT_OBSERVED_POINTS_IN_SAMPLE, x.length, 2); }
		//sort arrays
		final double[] x_sort = MathArrays.copyOf(x);
		final double[] y_sort = MathArrays.copyOf(y);
		Arrays.sort(x_sort);
		Arrays.sort(y_sort);
		//max difference between cdf_x and cdf_y
		double max_d = 0d;
		for (int i = 0; i < x_sort.length; i++) {
			final double x_i = x_sort[i];
			if ((i > 0) && (x_i == x_sort[i-1])) { continue; }
			final double cdf_x = cdf(x_sort, x_i);
			final double cdf_y = cdf(y_sort, x_i);
			final double curr_d = FastMath.abs(cdf_x - cdf_y);
			if (curr_d > max_d) { max_d = curr_d; }
		}
		for (int i = 0; i < y_sort.length; i++) {
			final double y_i = y_sort[i];
			if ((i > 0) && (y_i == y_sort[i-1])) { continue; }
			final double cdf_x = cdf(y_sort, y_i);
			final double cdf_y = cdf(y_sort, y_i);
			final double curr_d = FastMath.abs(cdf_x - cdf_y);
			if (curr_d > max_d) { max_d = curr_d; }
		}   
		return max_d;
	}

	
	////// MAINS //////
	public static void mainSplit(final String fileName, String directory, final int S_i) {
		final File file = new File("/Users/alyssakeimach/" + fileName);
		splitFile(file, directory, S_i); 
	}

	public static void mainStats(String directory, String tableName, String tableStmt, String loadStmt, int S_i) throws SQLException, FileNotFoundException {
		for (int test = 0; test < 10; test++) {
			for (int i = 1; i <= 2; i++) {
				String ks_tableName = tableName + i;
				tableInit(ks_tableName, tableStmt);
				loadRandom(ks_tableName, loadStmt, directory); 
			}
			if (invalFile) return;
			Connection conn1 = getConnection();
			Connection conn2 = getConnection();
			ksMap1 = new HashMap<String, double[]>();
			ksMap2 = new HashMap<String, double[]>();
			try { 
				getKSnums(executeQuery(conn1, "SELECT * FROM " + tableName + "1"), ksMap1, S_i);
				getKSnums(executeQuery(conn2, "SELECT * FROM " + tableName + "2"), ksMap2, S_i);
			} 
			catch (SQLException e) { e.printStackTrace(); }
			System.out.print(S_i);
			for (Entry<String, double[]> entry : ksMap1.entrySet()) {    
				String key = entry.getKey();
				double[] values1 = entry.getValue();
				double[] values2 = ksMap2.get(key);
				double statistic = kolmogorovSmirnovStatistic(values1, values2);
				System.out.print("\t" + statistic);
			}
			System.out.println();
			conn1.close();
			conn2.close();
		}
	}

	public static void restart(String directory) {
		File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/" + directory + "/");
		for(File file: folder.listFiles()) file.delete();
	}

	public static void main(String args[]) throws SQLException, InterruptedException, FileNotFoundException  {

		////// TRIP DATA //////
		int A = 15; //percentage of N tuples
		final String directory = "trip" + A;
		final String fileName = directory + ".csv";
		final String tableName = "KS_" + directory + "_";
		final String tableStmt = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, Trip_ID BIGINT, Duration BIGINT, Start_Date VARCHAR(100), Start_Station VARCHAR(100), Start_Terminal BIGINT, End_Date VARCHAR(100), End_Station VARCHAR(100), End_Terminal BIGINT, Bike_ BIGINT, Subscription_Type VARCHAR(100), Zip_Code BIGINT, PRIMARY KEY (id_0))";
		final String loadStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (Trip_ID, Duration, Start_Date, Start_Station, Start_Terminal, End_Date, End_Station, End_Terminal, Bike_, Subscription_Type, Zip_Code) SET id_0 = NULL";

		
		////// REBALANCING DATA //////
		/*
		int A = 15; //percentage of N tuples
		final String directory = "rebal" + A;
		final String fileName = directory + ".csv";
		final String tableName = "KS_" + directory + "_";
		final String tableStmt = "(id_0 INT UNSIGNED NOT NULL AUTO_INCREMENT, _station_id_ BIGINT, _bikes_available_ BIGINT, _docks_available_ BIGINT, _time_ TIMESTAMP, PRIMARY KEY (id_0))";
		final String loadStmt = "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' (_station_id_, _bikes_available_, _docks_available_, _time_) SET id_0 = NULL";
		*/
		
		PrintStream out = new PrintStream(new FileOutputStream(directory + "_output.txt"));
		System.setOut(out);
		
		for (int S_i = 10; S_i <= 3000; S_i += 10) {
			restart(directory);
			Thread.sleep(1000);
			mainSplit(fileName, directory, S_i);
			Thread.sleep(3000);
			mainStats(directory, tableName, tableStmt, loadStmt, S_i);
		}
	

		Experiment2 kw = new Experiment2(3);
		double[] vals0 = {0.19999999999999996, 0.6000000000000001, 1.0, 0.8, 0.0, 0.39999999999999997, 0.4, 0.30000000000000004, 0.4, 0.9, 0.7, 0.0, 0.4, 0.30000000000000004};
		double[] vals1 = {0.4, 0.4, 1.0, 0.5, 0.0, 0.2, 0.20000000000000007, 0.3, 0.2, 0.9, 0.39999999999999997, 0.0, 0.3, 0.2};
		double[] vals2 = {0.6000000000000001, 1.0, 0.8, 0.0, 0.39999999999999997, 0.30000000000000004, 0.2, 1.0, 0.30000000000000004, 0.0, 0.5, 0.5};

		kw.addAll(vals0, 0);
		kw.addAll(vals1, 1);
		kw.addAll(vals2, 2);

		kw.test(0.5);	

		/*
		Returns true if the null hypothesis is rejected; false otherwise. 
		The meaning of the null hypothesis and alternative hypothesis depends on the specific test.
		The prespecified level of confidence, alpha, can be used for either one-tailed or two-tailed (directional or nondirectional) distributions, depending on the specific test. 
		Some tests may only support specific values for alpha.
		 */
	}

}


