package data;


//Tracks the differences between the perspective presented to the user and to the DB
public class UserView {

	//User interaction view for change and query
	public static String[] fields;
	public static String[] types;
	public static int tableSize = 0;

	//set column titles based on top line in data file (or not)
	public static void setUserColumns() {

		fields = new String[Parser.tableSize];

		if (Parser.titlesIncluded) { fields = Parser.titleRow; }
		else { 
			for (int i = 0; i < Parser.tableSize; i++) { fields[i] = "col_" + (i+1); }
		}

		types = new String[Parser.tableSize];
		types = Parser.types;
	}

}