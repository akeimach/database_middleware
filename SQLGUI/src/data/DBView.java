package data;


//make the data containers for the schema seen by the db
public class DBView {

	public static String[] fields;
	public static String[] types;
	public static int tableSize = 0;
	public static int numDummyCols = 4;

	//set the columns with the dummy name and version number
	public static void setDBcolumns() {

		tableSize = Parser.tableSize + numDummyCols;

		fields = new String[tableSize]; 
		fields[0] = "id_0";
		for (int col = 1; col < numDummyCols; col++) { fields[Parser.tableSize + col] =  "version_" + (Parser.tableSize + col); }
		int title_id = 0;
		//set the rest of the columns to the standard titles
		for (int i = 1; i <= Parser.tableSize; i++) {
			if (Parser.titlesIncluded) { fields[i] = Parser.titleRow[title_id]; }
			else { fields[i] = "col_" + i; }
			title_id++;
		}

		types = new String[tableSize];
		types[0] = "INT";
		for (int col = 1; col < numDummyCols; col++) { types[Parser.tableSize + col] =  "INT NULL"; }
		int type_id = 0;
		for (int i = 1; i <= Parser.tableSize; i++) {
			types[i] = Parser.types[type_id];
			type_id++;
		}
	}
	
	//call this if the user changes the schema
	public static void updateDBstrings() {

		tableSize = UserView.tableSize + numDummyCols;

		fields = new String[tableSize]; 
		fields[0] = "id_0";
		for (int col = 1; col < numDummyCols; col++) { fields[UserView.tableSize + col] =  "version_" + (UserView.tableSize + col); }
		int title_id = 0;
		//set the rest of the columns to the standard titles
		for (int i = 1; i <= Parser.tableSize; i++) {
			if (Parser.titlesIncluded) { fields[i] = UserView.fields[title_id]; }
			else { fields[i] = "col_" + i; }
			title_id++;
		}

		types = new String[tableSize];
		types[0] = "INT";
		for (int col = 1; col < numDummyCols; col++) { types[UserView.tableSize + col] =  "INT NULL"; }
		int type_id = 0;
		for (int i = 1; i <= Parser.tableSize; i++) {
			types[i] = Parser.types[type_id];
			type_id++;
		}
		Parser.tableSize = UserView.tableSize;
	}
}