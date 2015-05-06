import java.util.Map;
import java.util.Vector;


public class ChangeSchema {

	public static Map<String, String> renameMap;
	public static boolean userAdd = false;
	public static boolean userEditType = false;
	
	@SuppressWarnings("rawtypes")
	public static void verifyChanges(Vector titles, Vector types) {
		for (int i = 0; i < titles.size(); i++) {
			if (!titles.elementAt(i).equals(Struct.userFields[i+1])) {
				System.out.println("CHANGED " + Struct.userFields[i+1] + " to " + titles.elementAt(i));
				renameMap.put(Struct.userFields[i+1], (String) titles.elementAt(i));
			}
			if (!types.elementAt(i).equals(Struct.userTypes[i+1])) {
				System.out.println("CHANGED " + Struct.userTypes[i+1] + " to " + types.elementAt(i));
				userEditType = true;
			}
		}
	}
	
	
	public static void evaluateChanges() {
		
		if (!renameMap.isEmpty()) {
			String renameCols = "ALTER TABLE " + Struct.tableName + " RENAME COLUMN ";
			while (!renameMap.isEmpty()) {
				//department_name to dept_name;
			}
			 
		}
		
	}
	
	
}
