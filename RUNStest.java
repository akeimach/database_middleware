import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.HashMap;

//EXPERIMENT 4
public class RUNStest {

	public static String server = "jdbc:mysql://localhost:3306/";
	public static String user = "root";
	public static String password = "root";
	public static HashMap<String, double[]> runsMap = new HashMap<String, double[]>();
	public static int up_run = 0;
	public static int down_run = 0;
	public static int total_runs = 0;
	public static int N = 0;


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



}
