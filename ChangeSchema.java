import java.util.Vector;


public class ChangeSchema extends DecisionTree {

	
	@SuppressWarnings("rawtypes")
	public static void verifyChanges(Vector titles, Vector types) {
		for (int i = 0; i < titles.size(); i++) {
			if (!titles.elementAt(i).equals(Struct.userFields[i+1])) {
				System.out.println("CHANGED " + Struct.userFields[i+1] + " to " + titles.elementAt(i));
			}
			if (!types.elementAt(i).equals(Struct.userTypes[i+1])) {
				System.out.println("CHANGED " + Struct.userTypes[i+1] + " to " + types.elementAt(i));
			}
		}
	}
	
	
	
	
	
	
}
