import java.util.Arrays;
import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;



public class KSstats {


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

	
	private static void checkArray(double[] array) {
		if (array == null) {
			throw new NullArgumentException(LocalizedFormats.NULL_NOT_ALLOWED);
		}
		if (array.length < 2) {
			throw new InsufficientDataException(LocalizedFormats.INSUFFICIENT_OBSERVED_POINTS_IN_SAMPLE, array.length,
					2);
		}
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
	

	public static void main() {
		System.out.println("HI");
		double[] x = {3.0, 3.4};
		double[] y = {4.3, 4.0, 4.9};
		double statistic = kolmogorovSmirnovStatistic(x, y);
		System.out.println(statistic);
		
		return;
	}

}