import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;


public class ChangeSchema extends Connect {

	static class DecisionTree {

		//least common supertype--determine if changes necessary, set new num of cols

		//number of existing tuples
		public long countRows() throws SQLException {
			String orig = Struct.dbName;
			Struct.dbName = "information_schema";
			String command = "SELECT TABLE_ROWS FROM TABLES WHERE TABLE_NAME = \'" + Struct.tableName + "\'";
			ResultSet stats = executeQuery(command);
			Struct.dbName = orig;
			while (stats.next()) { Struct.table_size = stats.getLong(1); }
			System.out.println("CURRENT TABLE SIZE: " + Struct.table_size);
			return Struct.table_size;
		}
	}

	static class UserChange {

		@SuppressWarnings("rawtypes")
		public static void alterMethod(Vector changedTitles, Vector changedTypes) throws SQLException {
			
			Map<Integer, String> changedTitlesMap = new HashMap<Integer, String>();
			Map<Integer, String> changedTypesMap = new HashMap<Integer, String>();

			//initialize maps
			for (int i = 0; i < Struct.userFields.length; i++) {
				if (!changedTitles.elementAt(i).equals(Struct.userFields[i])) { changedTitlesMap.put(i, (String)changedTitles.elementAt(i)); }
				if (!changedTypes.elementAt(i).equals(Struct.userTypes[i])) { changedTypesMap.put(i, (String)changedTypes.elementAt(i)); }
			}
			
			//user added new fields
			if (!changedTitlesMap.isEmpty() && (Struct.future_user_table_size == Struct.curr_user_table_size)) { //only title changed, no field added
				for (Entry<Integer, String> entry : changedTitlesMap.entrySet()) {
					String renameCols = "ALTER TABLE " + Struct.tableName + " CHANGE " + Struct.userFields[entry.getKey()] + " " + entry.getValue() + " " + Struct.userTypes[entry.getKey()];
					Struct.userFields[entry.getKey()] = entry.getValue(); //change so don't redo same one again  
					executeUpdate(renameCols); 
				}
			}
		
			//user changed data types
			if (!changedTypesMap.isEmpty() && (Struct.future_user_table_size == Struct.curr_user_table_size)) {
				boolean noComma = true;
				String changeType = "ALTER TABLE " + Struct.tableName;
				for (Entry<Integer, String> entry : changedTypesMap.entrySet()) {
					if (noComma || (changedTypesMap.size() == 1)) {
						noComma = false;
						changeType += " MODIFY COLUMN " + Struct.userFields[entry.getKey()] + " " + entry.getValue();
						Struct.userTypes[entry.getKey()] = entry.getValue();
					}
					else { 
						changeType += ", MODIFY COLUMN " + Struct.userFields[entry.getKey()] + " " + entry.getValue();
						Struct.userTypes[entry.getKey()] = entry.getValue();
					}
				}
				executeUpdate(changeType);
			}
		
			//user added a new column
			if (Struct.future_user_table_size > Struct.curr_user_table_size) {
				boolean noComma = true;	
				String changeType = "ALTER TABLE " + Struct.tableName;
				for (int i = Struct.curr_user_table_size; i < Struct.future_user_table_size; i++) {
					if (noComma || (Struct.curr_user_table_size - Struct.future_user_table_size == 1)) {
						noComma = false;
						changeType += " ADD COLUMN " + changedTitles.elementAt(i) + " " + changedTypes.elementAt(i);
					}
					else { 
						changeType += ", ADD COLUMN " + changedTitles.elementAt(i) + " " + changedTypes.elementAt(i);
					}
				}
				
				String[] tempField = Struct.userFields;
				String[] tempType = Struct.userTypes;
				Struct.userFields = new String[Struct.future_user_table_size];
				Struct.userTypes = new String[Struct.future_user_table_size];
				for (int i = 0; i < tempField.length; i++) {
					Struct.userFields[i] = tempField[i];
					Struct.userTypes[i] = tempType[i];
				}
				for (int j = Struct.future_user_table_size; j > Struct.curr_user_table_size; j--) {
					Struct.userFields[j-1] = (String)changedTitles.elementAt(j-1);
					Struct.userTypes[j-1] = (String)changedTypes.elementAt(j-1);
					System.out.println(Struct.userFields[j-1] + " " + (String)changedTitles.elementAt(j-1));
				}
				executeUpdate(changeType);
			}
		}
		
		
		public static void dummyMethod() {
			
		}
		
		

		@SuppressWarnings("rawtypes")
		public static void mainUserChange(final Vector changedTitles, final Vector changedTypes) throws SQLException {

			DecisionTree dec = new DecisionTree();
			//determine alter method
			long num_tuples = dec.countRows();
			//Dummy method
			boolean high_read_volume = true; //TODO: for testing
			//ask the user?
			//View method
			boolean few_reads = false; //TODO: for testing

			//EXISTING TABLE < 4500 TUPLES
			if (num_tuples < 4500 || num_tuples > 4500) { //TODO: for testing
				Thread alterMethodThread = new Thread() {
					public void run() {
						try { 
							alterMethod(changedTitles, changedTypes); 
							Struct.updateDBstrings();
						} 
						catch (SQLException e) { e.printStackTrace(); } 
					}
				};
				alterMethodThread.setName("alterMethodThread");
				alterMethodThread.start();
			}

			//NUM TUPLES > 4500, determine expected workload before finalize
			else {
				if (high_read_volume) {
					
				}
				else if (few_reads) {
					
				}
			}
			

		}

	}

	class AutoChange {


	}



}
