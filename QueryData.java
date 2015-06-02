import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;


public class QueryData extends Connect {

	//static ResultSet queryRS = null;

	static class queryStats {

		public static void randomness(String query) throws SQLException {

			//get num tuples, divide by k
			long num_tuples = Connect.countRows();
			long k_size = num_tuples / Struct.k_subsets;
			System.out.println("First k: " + k_size);
			System.out.println("Second k: " + (2 * k_size));

			//repeat query on subdivisions for numeric results
			//TODO: fix from hard coding
			statsQueryTable(executeQuery(query));
			//do shit
			publishQuery(executeQuery(query));



		}

	}

	
	public static void statsQueryTable(ResultSet rs) throws SQLException {
		try {
			
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			HashMap<String, ArrayList<Object>> rsNums = new HashMap<String, ArrayList<Object>>();
			ArrayList<Object> contents = new ArrayList<Object>();
			// Get the column names, only show RS which matches userFields
			for (int col = 1; col <= numberOfColumns; col++) {
				//ArrayList newEmpty = new ArrayList<Integer>();
				int type = metaData.getColumnType(col);
				if ((type == Types.BIGINT) || (type == Types.DECIMAL) || (type == Types.DOUBLE) || (type == Types.FLOAT) || (type == Types.NUMERIC) || (type == Types.INTEGER) || (type == Types.BOOLEAN)) {
					contents = new ArrayList<Object>();
					rsNums.put(metaData.getColumnLabel(col), contents); 
					System.out.print(metaData.getColumnLabel(col) + " ");
				}
			}

			// Get all rows.
			while (rs.next()) {
				
				for (int i = 1; i <= numberOfColumns; i++) { 	
					if (rsNums.containsKey(metaData.getColumnLabel(i))) {
						contents = rsNums.get(metaData.getColumnLabel(i));
						contents.add(rs.getObject(i));
					}
				}
			}

			return;
		} 
		catch (Exception e) { e.printStackTrace(); }
	}

	public static void publishQuery(ResultSet rs) throws SQLException {
		GUI.queryOutput.setModel(TableForm.queryTable(rs)); 
		GUI.queryOutput.setColumnModel(TableForm.colwidth(GUI.queryOutput));
		GUI.loadingProgress.setIndeterminate(false);
		GUI.loadingProgress.setValue(100);
	}

	public static void mainQuery(final String userQuery) throws SQLException {
		Thread queryThread = new Thread() {
			public void run() {
				try {
					queryStats.randomness(userQuery.trim());
					//publishQuery(executeQuery(userQuery.trim()));

				} 
				catch (SQLException e) { e.printStackTrace(); }	
			}
		};
		queryThread.setName("queryThread");
		queryThread.start();

		return;
	}


}
