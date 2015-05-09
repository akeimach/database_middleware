import java.sql.ResultSet;
import java.sql.SQLException;


public class QueryData extends Connect {

	static ResultSet queryRS = null;
	
	public static void mainQuery(final String userQuery) throws SQLException {
		Thread queryThread = new Thread() {
			public void run() {
				try {
					queryRS = executeQuery(userQuery.trim());
					GUI.queryOutput.setModel(TableForm.queryTable(queryRS)); 
					GUI.queryOutput.setColumnModel(TableForm.colwidth(GUI.queryOutput));
				} 
				catch (SQLException e) { e.printStackTrace(); }	
			}
		};
		queryThread.setName("queryThread");
		queryThread.start();
		
		
		GUI.loadingProgress.setIndeterminate(false);
		GUI.loadingProgress.setValue(100);
		
		return;
	}

}
