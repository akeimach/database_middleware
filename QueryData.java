import java.sql.ResultSet;
import java.sql.SQLException;


public class QueryData extends Connect {

	static ResultSet queryRS = null;
	
	static class queryStats {
		
		public static void randomness(String query) throws SQLException {
			
			//get num tuples, divide by k
			long num_tuples = Connect.countRows();
			long k_size = num_tuples / Struct.k_subsets;
			System.out.println("First k: " + k_size);
			System.out.println("Second k: " + (2 * k_size));
			
			//repeat query on subdivisions for numeric results
			System.out.println(query + " AND id_0 < " + k_size);
			
			
			//do shit
			
		}
		
	}


	public static void mainQuery(final String userQuery) throws SQLException {
		Thread queryThread = new Thread() {
			public void run() {
				try {
					queryStats.randomness(userQuery.trim());
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
