import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;


public class Experiment1 {
	
	public static HashMap<String, double[]> ksMap1 = new HashMap<String, double[]>();
	public static HashMap<String, double[]> ksMap2 = new HashMap<String, double[]>();
	

	public static void splitFile(File file) throws IOException, InterruptedException {
		PrintWriter splitexe = new PrintWriter("/Users/alyssakeimach/split.exe", "UTF-8");
		Runtime.getRuntime().exec("chmod a+x /Users/alyssakeimach/split.exe");
		splitexe.println("split -a1 -l20000 " + file); //-a3 for three letter file names
		splitexe.close();
		ProcessBuilder pb = new ProcessBuilder("/Users/alyssakeimach/split.exe");
		pb.directory(new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/"));
		pb.redirectErrorStream(true);
		Process p = pb.start();
		assert pb.redirectInput() == Redirect.PIPE;
		assert p.getInputStream().read() == -1;
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

	
	//DO KS STAT MATH
	private static void checkArray(double[] array) {
		if (array == null) { throw new NullArgumentException(LocalizedFormats.NULL_NOT_ALLOWED); }
		if (array.length < 2) { throw new InsufficientDataException(LocalizedFormats.INSUFFICIENT_OBSERVED_POINTS_IN_SAMPLE, array.length, 2); }
	}

	private static double cdf(final double x, final double[] samples) {
		final int n = samples.length;
		int index = Arrays.binarySearch(samples, x);
		if (index >= 0) {
			while(index < (n - 1) && samples[index+1] == x) { ++index; }
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
			final double cdf_x = cdf(x_i, sx);
			final double cdf_y = cdf(x_i, sy);
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
			final double cdf_x = cdf(y_i, sx);
			final double cdf_y = cdf(y_i, sy);
			final double curD = FastMath.abs(cdf_x - cdf_y);
			if (curD > supD) {
				supD = curD;
			}
		}   
		return supD;
	}

	public static void getKSnums(ResultSet rs, HashMap<String, double[]> ksMap) throws SQLException {
		
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

	//****** LOAD FILE KS ******//

		public static void KStableInit(String KStableName) throws SQLException {
			
			try {
				String dropString = "DROP TABLE IF EXISTS " + KStableName;
				executeUpdate(dropString);
			}
			catch (SQLException e) {
				System.out.println("ERROR: Could not drop the table");
				e.printStackTrace();
			}

			try {
				String createTableString = "CREATE TABLE " + KStableName + " (" + Struct.dbFields[0] + " " + Struct.dbTypes[0] + " UNSIGNED NOT NULL AUTO_INCREMENT, "; //Create new
				for (int i = 1; i < Struct.dbFields.length; i++) {
					createTableString += Struct.dbFields[i] + " " + Struct.dbTypes[i] + ", ";
				}
				createTableString += "PRIMARY KEY (" + Struct.dbFields[0] + "))";
				executeUpdate(createTableString);
			}
			catch (SQLException e) {
				System.out.println("ERROR: Could not create the table");
				e.printStackTrace();
			}
			
		}

		
		
		public static void startKSload(String KStableName) throws SQLException {

			File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/");
			File[] roots = folder.listFiles();
			Random rand = new Random();
			File rndFile = Parser.getRandFile(roots[rand.nextInt(roots.length)]);

			String initLoad = loaderStmt(rndFile, KStableName);

			try {
				executeQuery(initLoad);
				System.out.println("Uploading file: " + rndFile.getAbsolutePath());	
			}
			catch (SQLException e) {
				try {
					String error = unloaderStmt(rndFile.getAbsolutePath());
					executeQuery(error);
					System.out.println("Error, sent query \"" + error + "\"");
				} 
				catch (SQLException e2) { e2.printStackTrace(); }
			} 

			try {
				
				//then move the file once uploaded
				Path source = rndFile.toPath();
				String fileTitle = rndFile.getName() + "_split";
				Path target = Paths.get("/Users/alyssakeimach/Eclipse/DBconnector/used/", fileTitle);
				Files.move(source, target, REPLACE_EXISTING);
				System.out.println("Moved " + rndFile.getName() + " to " + target.toString() + " after uploading");
			}
			catch (IOException e) { e.printStackTrace(); } //could not move file
		}


		//****** LOAD FILE STATEMENTS ******//
		public static String loaderStmt(File loadFile, String tableName) {
			
			//load data concurrent local statement
			//String bulkLoad = "LOAD DATA CONCURRENT LOCAL INFILE '" + loadFile + "' INTO TABLE " + tableName + " FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + Parser.terminator + "' ";
			String bulkLoad = "LOAD DATA CONCURRENT LOCAL INFILE '" + loadFile + "' INTO TABLE " + tableName + " FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"'";
			if (GUI.titleRow) { bulkLoad += "IGNORE 1 LINES "; }
			bulkLoad += "(";
			
			//load fields detected in file parser
			for (int i = 0; i < Struct.init_table_size - 1; i++) { bulkLoad += Struct.userFields[i] + ", "; }
			bulkLoad += Struct.userFields[Struct.init_table_size - 1] + ") ";
			
			//set dummy and default cols to be loaded
			bulkLoad += "SET " + Struct.dbFields[0] + " = NULL, ";
			for (int i = Struct.init_table_size + 1; i < Struct.db_table_size - 1; i++) {
				bulkLoad += Struct.dbFields[i] + " = NULL, ";
			}
			bulkLoad += Struct.dbFields[Struct.db_table_size - 1] + " = NULL";
			
			return bulkLoad;
		}
		
		public static String unloaderStmt(String unloadFile) {
			String error = "SELECT * INTO OUTFILE '" + unloadFile + ".out' FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + Parser.terminator + "'";
			return error;
		}
		


		public static void mainLoader() throws SQLException {
			
			Thread KSloaderThread = new Thread() {
				public void run() {
					try { 
						for (int i = 0; i < Struct.k_subsets; i++) {
							String KStableName = Struct.tableName + "_ks_" + Struct.ks_num;
							KStableInit(KStableName);
							startKSload(KStableName); 
							Struct.ks_num++; //increment for next ks load
						}
						KSstats.startKS();
					}
					catch (SQLException e) { e.printStackTrace(); }
				}
			};
			KSloaderThread.setName("KSloaderThread");
			KSloaderThread.start();

	public static void startKS() throws SQLException {
		
		ksMap1 = new HashMap<String, double[]>();
		ksMap2 = new HashMap<String, double[]>();
		
		getKSnums(Connect.executeQuery("SELECT * FROM table1_ks_1"), ksMap1);
		getKSnums(Connect.executeQuery("SELECT * FROM table1_ks_2"), ksMap2);
		
		for (Entry<String, double[]> entry : ksMap1.entrySet()) {
		    
			String key = entry.getKey();
		    double[] values1 = entry.getValue();
		    double[] values2 = ksMap2.get(key);
		    double statistic = kolmogorovSmirnovStatistic(values1, values2);
		    
		    if (statistic != 0) { System.out.println("KS statistic for " + key + ": " + statistic); }
		}
	}


}