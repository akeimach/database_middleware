import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;


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
			//System.out.println(query + " AND id_0 < " + k_size);
			//TODO: fix from hard coding
			ResultSet k1 = executeQuery(query);
			//ResultSet k2 = executeQuery(query + " AND id_0 > " + k_size);

			statsQueryTable(k1);
			//do shit
			publishQuery(k1);



		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void statsQueryTable(ResultSet rs) throws SQLException {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			Vector columnNames = new Vector();

			//ArrayList<Integer> vals = new ArrayList<Integer>();
			HashMap<String, ArrayList<Integer>> rsNums = new HashMap<String, ArrayList<Integer>>();

			//ArrayList current = dictMap.get(dictCode);

			// Get the column names, only show RS which matches userFields
			for (int col = 1; col <= numberOfColumns; col++) {
				//ArrayList newEmpty = new ArrayList<Integer>();
				rsNums.put(metaData.getColumnLabel(col), null);
			}

			// Get all rows.
			while (rs.next()) {
				//Vector columnNums = new Vector();
				ArrayList contents = new ArrayList<Integer>();
				for (int i = 1; i <= numberOfColumns; i++) { 		
					if (Parser.isNumeric((String) rs.getObject(i))) {
						System.out.println(rs.getObject(i));
					}
				}
			}



			//System.out.println(rowsReturned + " rows returned\n");
			//GUI.dbOutput.append(rowsReturned + " rows returned\n");
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
