package net;

import data.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

//change the schema based on user input OR the decision tree
public class ChangeSchema {

	//for when the user changes the schema via the GUI
	public static class UserChange {

		@SuppressWarnings("rawtypes")
		public static void alterMethod(Vector changedTitles, Vector changedTypes) throws SQLException {
			
			Map<Integer, String> changedTitlesMap = new HashMap<Integer, String>();
			Map<Integer, String> changedTypesMap = new HashMap<Integer, String>();

			//initialize maps
			for (int i = 0; i < UserView.fields.length; i++) {
				if (!changedTitles.elementAt(i).equals(UserView.fields[i])) { changedTitlesMap.put(i, (String)changedTitles.elementAt(i)); }
				if (!changedTypes.elementAt(i).equals(UserView.types[i])) { changedTypesMap.put(i, (String)changedTypes.elementAt(i)); }
			}
			
			//user added new fields
			if (!changedTitlesMap.isEmpty() && (UserView.tableSize == Parser.tableSize)) { //only title changed, no field added
				for (Entry<Integer, String> entry : changedTitlesMap.entrySet()) {
					String renameCols = "ALTER TABLE " + Connect.tableName + " CHANGE " + UserView.fields[entry.getKey()] + " " + entry.getValue() + " " + UserView.types[entry.getKey()];
					UserView.fields[entry.getKey()] = entry.getValue(); //change so don't redo same one again  
					Connect.executeUpdate(renameCols); 
				}
			}
		
			//user changed data types
			if (!changedTypesMap.isEmpty() && (UserView.tableSize == Parser.tableSize)) {
				boolean noComma = true;
				String changeType = "ALTER TABLE " + Connect.tableName;
				for (Entry<Integer, String> entry : changedTypesMap.entrySet()) {
					if (noComma || (changedTypesMap.size() == 1)) {
						noComma = false;
						changeType += " MODIFY COLUMN " + UserView.fields[entry.getKey()] + " " + entry.getValue();
						UserView.types[entry.getKey()] = entry.getValue();
					}
					else { 
						changeType += ", MODIFY COLUMN " + UserView.fields[entry.getKey()] + " " + entry.getValue();
						UserView.types[entry.getKey()] = entry.getValue();
					}
				}
				Connect.executeUpdate(changeType);
			}
		
			//user added a new column
			if (UserView.tableSize > Parser.tableSize) {
				boolean noComma = true;	
				String changeType = "ALTER TABLE " + Connect.tableName;
				for (int i = Parser.tableSize; i < UserView.tableSize; i++) {
					if (noComma || (Parser.tableSize - UserView.tableSize == 1)) {
						noComma = false;
						changeType += " ADD COLUMN " + changedTitles.elementAt(i) + " " + changedTypes.elementAt(i);
					}
					else { 
						changeType += ", ADD COLUMN " + changedTitles.elementAt(i) + " " + changedTypes.elementAt(i);
					}
				}
				
				String[] tempField = UserView.fields;
				String[] tempType = UserView.types;
				UserView.fields = new String[UserView.tableSize];
				UserView.types = new String[UserView.tableSize];
				for (int i = 0; i < tempField.length; i++) {
					UserView.fields[i] = tempField[i];
					UserView.types[i] = tempType[i];
				}
				for (int j = UserView.tableSize; j > Parser.tableSize; j--) {
					UserView.fields[j-1] = (String)changedTitles.elementAt(j-1);
					UserView.types[j-1] = (String)changedTypes.elementAt(j-1);
				}
				Connect.executeUpdate(changeType);
			}
		}
		
		
		public static void dummyMethod() { }
		

		@SuppressWarnings("rawtypes")
		public static void mainUserChange(final Vector changedTitles, final Vector changedTypes) throws SQLException {

			//determine alter method
			long num_tuples = Connect.countRows();
			//Dummy method
			boolean high_read_volume = true; //for testing
			//ask the user?
			//View method
			boolean few_reads = false; //for testing

			//EXISTING TABLE < 4500 TUPLES
			if (num_tuples < 4500 || num_tuples > 4500) { //for testing
				Thread alterMethodThread = new Thread() {
					public void run() {
						try { 
							alterMethod(changedTitles, changedTypes); 
							DBView.updateDBstrings();
						} 
						catch (SQLException e) { e.printStackTrace(); } 
					}
				};
				alterMethodThread.setName("alterMethodThread");
				alterMethodThread.start();
			}

			//NUM TUPLES > 4500, determine expected workload before finalize
			else {
				if (high_read_volume) { }
				else if (few_reads) { }
			}
		}
	}

	//change the schema if the program detects an error
	static class AutoChange { }

}
