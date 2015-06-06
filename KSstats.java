import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;


public class KSstats {
	
	public static HashMap<String, double[]> ks1Map = new HashMap<String, double[]>();
	public static HashMap<String, double[]> ks2Map = new HashMap<String, double[]>();
	

	//DO KS STAT MATH
	private static void checkArray(double[] array) {
		if (array == null) {
			throw new NullArgumentException(LocalizedFormats.NULL_NOT_ALLOWED);
		}
		if (array.length < 2) {
			throw new InsufficientDataException(LocalizedFormats.INSUFFICIENT_OBSERVED_POINTS_IN_SAMPLE, array.length,
					2);
		}
	}

	private static double edf(final double x, final double[] samples) {
		final int n = samples.length;
		int index = Arrays.binarySearch(samples, x);
		if (index >= 0) {
			while(index < (n - 1) && samples[index+1] == x) {
				++index;
			}
		}
		return index >= 0 ? (index + 1d) / n : (-index - 1d) / n;
	}

	public static double kolmogorovSmirnovStatistic(double[] x, double[] y) {
		checkArray(x);
		checkArray(y);
		// Copy and sort the sample arrays
		final double[] sx = MathArrays.copyOf(x);
		final double[] sy = MathArrays.copyOf(y);
		Arrays.sort(sx);
		Arrays.sort(sy);
		final int n = sx.length;
		final int m = sy.length;

		// Find the max difference between cdf_x and cdf_y
		double supD = 0d;
		// First walk x points
		for (int i = 0; i < n; i++) {
			final double x_i = sx[i];
			// ties can be safely ignored
			if (i > 0 && x_i == sx[i-1]) {
				continue;
			}
			final double cdf_x = edf(x_i, sx);
			final double cdf_y = edf(x_i, sy);
			final double curD = FastMath.abs(cdf_x - cdf_y);
			if (curD > supD) {
				supD = curD;
			}
		}
		// Now look at y
		for (int i = 0; i < m; i++) {
			final double y_i = sy[i];
			// ties can be safely ignored
			if (i > 0 && y_i == sy[i-1]) {
				continue;
			}
			final double cdf_x = edf(y_i, sx);
			final double cdf_y = edf(y_i, sy);
			final double curD = FastMath.abs(cdf_x - cdf_y);
			if (curD > supD) {
				supD = curD;
			}
		}   
		return supD;
	}

	
	//GET KS ARRAYS
	public static double[] rsArray(ResultSet rs) {
		double[] nums = null;
		
		
		
		return nums;
	}
	
	public static void getKSnums(ResultSet rs, HashMap<String, double[]> ks) throws SQLException {
		
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 

			// Get the column names, only show RS which matches userFields
			for (int col = 1; col <= numberOfColumns; col++) {

				int type = metaData.getColumnType(col);
				if ((type == Types.BIGINT) || (type == Types.DECIMAL) || (type == Types.DOUBLE) || 
						(type == Types.FLOAT) || (type == Types.NUMERIC) || (type == Types.INTEGER) || (type == Types.BOOLEAN)) {
					//statNums = new ArrayList<Object>();
					double[] statNums = new double[(int)10000];
					ks.put(metaData.getColumnLabel(col), statNums); 
				}
			}

			// Get all rows.
			int row = 0;
			while (rs.next()) {
				for (int i = 1; i <= numberOfColumns; i++) { 	
					if (ks.containsKey(metaData.getColumnLabel(i))) {
						double[] dubs = ks.get(metaData.getColumnLabel(i));
						dubs[row] = rs.getInt(i);
						ks.put(metaData.getColumnLabel(i), dubs);
					}
				}
				row++;
			} 
			//System.out.println();
		}
		catch (Exception e) { e.printStackTrace(); }

	}


	public static void startKS() throws SQLException {
		
		getKSnums(Connect.executeQuery("SELECT * FROM table1_ks1"), ks1Map);
		getKSnums(Connect.executeQuery("SELECT * FROM table1_ks2"), ks2Map);
		
		
		for (Entry<String, double[]> entry : ks1Map.entrySet()) {
		    String key = entry.getKey();
		    
		    double[] values1 = entry.getValue();
		    double[] values2 = ks2Map.get(key);
		   
		    double statistic = kolmogorovSmirnovStatistic(values1, values2);
		    System.out.println(statistic);
		}

		//double statistic = kolmogorovSmirnovStatistic(gaussian, gaussian2);
		//System.out.println(statistic);

		return;
	}


}