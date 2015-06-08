import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import static java.nio.file.StandardCopyOption.*;

//USES DB_TABLE_SIZE, NUM_DUMMY_COLS
public class LoadFile extends Connect {

	//****** LOAD FILE BULK ******//
	public static void BULKtableInit() throws SQLException {

		try {
			String dropString = "DROP TABLE IF EXISTS " + Struct.tableName;
			executeUpdate(dropString);
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not drop the table");
			e.printStackTrace();
		}

		try {
			String createTableString = "CREATE TABLE " + Struct.tableName + " (" + Struct.dbFields[0] + " " + Struct.dbTypes[0] + " UNSIGNED NOT NULL AUTO_INCREMENT, "; //Create new
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
	
	public static void startBulkLoad() throws SQLException {

		String bulkLoad = loaderStmt(Struct.dataFile.getAbsoluteFile(), Struct.tableName);

		try {
			executeQuery(bulkLoad);
			System.out.println("Uploading file: " + Struct.dataFile.getAbsolutePath());	
		}
		catch (SQLException e) {
			try {
				String error = unloaderStmt(Struct.dataFile.getAbsolutePath());
				executeQuery(error);
				System.out.println("Error, sent query \"" + error + "\"");
			} 
			catch (SQLException e2) { e2.printStackTrace(); }
		}
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
	
	public static void startKSload(String KStableName) throws SQLException {

		File folder = new File("/Users/alyssakeimach/Eclipse/DBconnector/splits/");
		File[] roots = folder.listFiles();
		Random rand = new Random();
		File rndFile = getRandFile(roots[rand.nextInt(roots.length)]);

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
		String bulkLoad = "LOAD DATA CONCURRENT LOCAL INFILE '" + loadFile + "' INTO TABLE " + tableName + " FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + Parser.terminator + "' ";
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
						String KStableName = Struct.tableName + "_ks" + Struct.ks_num;
						Struct.ks_num++; //increment for next ks load
						KStableInit(KStableName);
						startKSload(KStableName); 
					}
					KSstats.startKS();
				}
				catch (SQLException e) { e.printStackTrace(); }
			}
		};
		KSloaderThread.setName("KSloaderThread");
		KSloaderThread.start();
		
		
		BULKtableInit();
		Thread BULKloaderThread = new Thread() {
			public void run() {
				try { startBulkLoad(); }
				catch (SQLException e) { e.printStackTrace(); }
			}
		};
		BULKloaderThread.setName("BULKloaderThread");
		BULKloaderThread.start();

	}


}