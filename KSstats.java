import java.util.Arrays;
import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;



public class KSstats {


	public static double[] gaussian = { 0.26055895, -0.63665233, 1.51221323, 0.61246988, -0.03013003, -1.73025682, -0.51435805, 0.70494168, 0.18242945,
		0.94734336, -0.04286604, -0.37931719, -1.07026403, -2.05861425, 0.11201862, 0.71400136, -0.52122185,
		-0.02478725, -1.86811649, -1.79907688, 0.15046279, 1.32390193, 1.55889719, 1.83149171, -0.03948003,
		-0.98579207, -0.76790540, 0.89080682, 0.19532153, 0.40692841, 0.15047336, -0.58546562, -0.39865469, 0.77604271,
		-0.65188221, -1.80368554, 0.65273365, -0.75283102, -1.91022150, -0.07640869, -1.08681188, -0.89270600,
		2.09017508, 0.43907981, 0.10744033, -0.70961218, 1.15707300, 0.44560525, -2.04593349, 0.53816843, -0.08366640,
		0.24652218, 1.80549401, -0.99220707, -1.14589408, -0.27170290, -0.49696855, 0.00968353, -1.87113545,
		-1.91116529, 0.97151891, -0.73576115, -0.59437029, 0.72148436, 0.01747695, -0.62601157, -1.00971538,
		-1.42691397, 1.03250131, -0.30672627, -0.15353992, -1.19976069, -0.68364218, 0.37525652, -0.46592881,
		-0.52116168, -0.17162202, 1.04679215, 0.25165971, -0.04125231, -0.23756244, -0.93389975, 0.75551407,
		0.08347445, -0.27482228, -0.4717632, -0.1867746, -0.1166976, 0.5763333, 0.1307952, 0.7630584, -0.3616248,
		2.1383790, -0.7946630, 0.0231885, 0.7919195, 1.6057144, -0.3802508, 0.1229078, 1.5252901, -0.8543149, 0.3025040
	};

	public static double[] gaussian2 = {
		2.88041498038308, -0.632349445671017, 0.402121295225571, 0.692626364613243, 1.30693446815426,
		-0.714176317131286, -0.233169206599583, 1.09113298322107, -1.53149079994305, 1.23259966205809,
		1.01389927412503, 0.0143898711497477, -0.512813545447559, 2.79364360835469, 0.662008875538092,
		1.04861546834788, -0.321280099931466, 0.250296656278743, 1.75820367603736, -2.31433523590905,
		-0.462694696086403, 0.187725700950191, -2.24410950019152, 2.83473751105445, 0.252460174391016,
		1.39051945380281, -1.56270144203134, 0.998522814471644, -1.50147469080896, 0.145307533554146,
		0.469089457043406, -0.0914780723809334, -0.123446939266548, -0.610513388160565, -3.71548343891957,
		-0.329577317349478, -0.312973794075871, 2.02051909758923, 2.85214308266271, 0.0193222002327237,
		-0.0322422268266562, 0.514736012106768, 0.231484953375887, -2.22468798953629, 1.42197716075595,
		2.69988043856357, 0.0443757119128293, 0.721536984407798, -0.0445688839903234, -0.294372724550705,
		0.234041580912698, -0.868973119365727, 1.3524893453845, -0.931054600134503, -0.263514296006792,
		0.540949457402918, -0.882544288773685, -0.34148675747989, 1.56664494810034, 2.19850536566584,
		-0.667972122928022, -0.70889669526203, -0.00251758193079668, 2.39527162977682, -2.7559594317269,
		-0.547393502656671, -2.62144031572617, 2.81504147017922, -1.02036850201042, -1.00713927602786,
		-0.520197775122254, 1.00625480138649, 2.46756916531313, 1.64364743727799, 0.704545210648595,
		-0.425885789416992, -1.78387854908546, -0.286783886710481, 0.404183648369076, -0.369324280845769,
		-0.0391185138840443, 2.41257787857293, 2.49744281317859, -0.826964496939021, -0.792555379958975,
		1.81097685787403, -0.475014580016638, 1.23387615291805, 0.646615294802053, 1.88496377454523, 1.20390698380814,
		-0.27812153371728, 2.50149494533101, 0.406964323253817, -1.72253451309982, 1.98432494184332, 2.2223658560333,
		0.393086362404685, -0.504073151377089, -0.0484610869883821
	};
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



	public static void startKS() {




		double statistic = kolmogorovSmirnovStatistic(gaussian, gaussian2);
		System.out.println(statistic);

		double[] x = {1, 2, 0, 1, 0, 3, 1};
		double[] y = {2, 1, 2, 0, 1, 1};
		double xy = kolmogorovSmirnovStatistic(x, y);
		System.out.println(xy);

		return;
	}


}