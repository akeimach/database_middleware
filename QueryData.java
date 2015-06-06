import java.sql.ResultSet;
import java.sql.SQLException;


public class QueryData extends Connect {


	public static void publishQuery(ResultSet rs) throws SQLException {
		GUI.queryOutput.setModel(TableForm.queryTable(rs)); 
		GUI.queryOutput.setColumnModel(TableForm.colwidth(GUI.queryOutput));
		GUI.loadingProgress.setIndeterminate(false);
		GUI.loadingProgress.setValue(100);
	}

	/*
	public static void ksQuery() throws SQLException {
		Thread ksQueryThread = new Thread() {
			public void run() {
				try {
					
					String k1_query = "SELECT * FROM table1_ks1";
					String k2_query = "SELECT * FROM table1_ks1";
					ResultSet rs_k1 = executeQuery(k1_query);
					ResultSet rs_k2 = executeQuery(k2_query);
					KSstats.startKS(rs_k1, rs_k2);
				} 
				catch (SQLException e) { e.printStackTrace(); }	
			}
		};
		ksQueryThread.setName("ksQueryThread");
		ksQueryThread.start();
	}
	*/
	public static void mainQuery(final String userQuery) throws SQLException {
		
		Thread bulkQueryThread = new Thread() {
			public void run() {
				try {
					ResultSet rs = executeQuery(userQuery.trim());
					publishQuery(rs);
				} 
				catch (SQLException e) { e.printStackTrace(); }	
			}
		};
		bulkQueryThread.setName("bulkQueryThread");
		bulkQueryThread.start();

	}



}
