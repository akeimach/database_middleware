package net;

import data.DBView;
import data.Parser;

import java.io.File;
import java.sql.SQLException;


//connect to the db and load the dbView of the data
public class LoadData {


	//initialize the table (or drop if one exists already)
	public static void tableInit() throws SQLException {

		try {
			String dropString = "DROP TABLE IF EXISTS " + Connect.tableName;
			Connect.executeUpdate(dropString);
		} catch (SQLException e) {
			System.out.println("ERROR: Could not drop the table");
			e.printStackTrace();
		}

		try {
			String createTableString = "CREATE TABLE " + Connect.tableName + " (" + DBView.fields[0] + " " + DBView.types[0] + " UNSIGNED NOT NULL AUTO_INCREMENT, "; //Create new
			for (int i = 1; i < DBView.fields.length; i++) {
				createTableString += DBView.fields[i] + " " + DBView.types[i] + ", ";
			}
			createTableString += "PRIMARY KEY (" + DBView.fields[0] + "))";
			Connect.executeUpdate(createTableString);
		} catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
		}
	}


	public static String loaderStmt(File loadFile, String tableName) {

		//load data concurrent local statement
		String bulkLoad = "LOAD DATA CONCURRENT LOCAL INFILE '" + loadFile + "' INTO TABLE " + tableName + " FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"'";
		if (Parser.titlesIncluded) {
			bulkLoad += " IGNORE 1 LINES ";
		}
		bulkLoad += "(";

		//load fields detected in file parser
		for (int i = 1; i < Parser.tableSize - 1; i++) {
			bulkLoad += DBView.fields[i] + ", ";
		}
		bulkLoad += DBView.fields[Parser.tableSize - 1] + ") ";

		//set dummy and default cols to be loaded
		bulkLoad += "SET " + DBView.fields[0] + " = NULL, ";
		for (int i = Parser.tableSize + 1; i < DBView.tableSize - 1; i++) {
			bulkLoad += DBView.fields[i] + " = NULL, ";
		}
		bulkLoad += DBView.fields[DBView.tableSize - 1] + " = NULL";

		return bulkLoad;
	}

	//unload the data if there was an error
	public static String unloaderStmt(String unloadFile) {
		String error = "SELECT * INTO OUTFILE '" + unloadFile + ".out' FIELDS TERMINATED BY '" + Parser.delimiter + "' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '" + Parser.terminator + "'";
		return error;
	}


	//load the data with a new thread
	public static void startBulkLoad() throws SQLException {

		tableInit();
		Thread loaderThread = new Thread() {
			public void run() {

				String bulkLoad = loaderStmt(Connect.dataFile.getAbsoluteFile(), Connect.tableName);
				try {
					Connect.executeQuery(bulkLoad);
					System.out.println("Uploading file: " + Connect.dataFile.getAbsolutePath());
				} catch (SQLException e) {
					try {
						String error = unloaderStmt(Connect.dataFile.getAbsolutePath());
						Connect.executeQuery(error);
						System.out.println("Error, sent query \"" + error + "\"");
					} catch (SQLException e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		loaderThread.setName("loaderThread");
		loaderThread.start();
	}


}