import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;


public class ChangeSchema extends Connect {

	public static Map<Integer, String> renameColMap;
	public static Map<Integer, String> changeTypeMap;

	@SuppressWarnings("rawtypes")
	public static void verifyChanges(Vector titles, Vector types) {

		renameColMap = new HashMap<Integer, String>();
		changeTypeMap = new HashMap<Integer, String>();

		for (int i = 0; i < titles.size(); i++) {
			if (!titles.elementAt(i).equals(Struct.userFields[i])) { renameColMap.put(i, (String)titles.elementAt(i)); }
			if (!types.elementAt(i).equals(Struct.userTypes[i])) { changeTypeMap.put(i, (String)types.elementAt(i)); }
		}
	}


	public static void changeColNames() throws SQLException {
		if (!renameColMap.isEmpty()) {
			for (Entry<Integer, String> entry : renameColMap.entrySet()) {
				String renameCols = "ALTER TABLE " + Struct.tableName + " CHANGE " + Struct.userFields[entry.getKey()] + " " + entry.getValue() + " " + Struct.userTypes[entry.getKey()];
				Struct.userFields[entry.getKey()] = entry.getValue(); //change so don't redo same one again  
				executeUpdate(renameCols); 
			}
		}
	}

	public static void changeTypes() throws SQLException {
		boolean noComma = true;
		if (!changeTypeMap.isEmpty()) {
			String changeType = "ALTER TABLE " + Struct.tableName;
			for (Entry<Integer, String> entry : changeTypeMap.entrySet()) {

				if (noComma || (changeTypeMap.size() == 1)) {
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
	}


	@SuppressWarnings("rawtypes")
	public static void mainSchema(final Vector titles, final Vector types) throws SQLException {

		verifyChanges(titles, types);

		Thread changeColThread = new Thread() {
			public void run() {
				try { 
					changeColNames(); 
					changeTypes(); 
				} 
				catch (SQLException e) { e.printStackTrace(); } 
			}
		};
		changeColThread.setName("changeColThread");
		changeColThread.start();

	}


}
