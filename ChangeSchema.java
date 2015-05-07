import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;


public class ChangeSchema extends Connect {

	public static Map<Integer, String> renameMap;
	public static Map<Integer, String> changeType;

	@SuppressWarnings("rawtypes")
	public static void verifyChanges(Vector titles, Vector types) {

		renameMap = new HashMap<Integer, String>();
		changeType = new HashMap<Integer, String>();

		for (int i = 0; i < titles.size(); i++) {
			if (!titles.elementAt(i).equals(Struct.userFields[i])) { renameMap.put(i, (String)titles.elementAt(i)); }
			if (!types.elementAt(i).equals(Struct.userTypes[i])) { changeType.put(i, (String)types.elementAt(i)); }
		}
	}


	public static void changeColNames() throws SQLException {
		if (!renameMap.isEmpty()) {
			for (Entry<Integer, String> entry : renameMap.entrySet()) {
				String renameCols = "ALTER TABLE " + Struct.tableName + " CHANGE " + Struct.userFields[entry.getKey()] + " " + entry.getValue() + " " + Struct.userTypes[entry.getKey()];
				Struct.userFields[entry.getKey()] = entry.getValue(); //change so don't redo same one again  
				executeUpdate(renameCols); 
			}
		}
	}

	public static void changeTypes() throws SQLException {

	}

	@SuppressWarnings("rawtypes")
	public static void mainSchema(final Vector titles, final Vector types) throws SQLException {
		Thread schemaThread = new Thread() {
			public void run() {
				verifyChanges(titles, types);
				try { changeColNames(); } 
				catch (SQLException e) { e.printStackTrace(); } 
			}
		};
		schemaThread.setName("schemaThread");
		schemaThread.start();
	}


}
