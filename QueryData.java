import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


public class QueryData extends Connect {


	static class queryStats {

		public static long num_tuples;
		public static long k_size;
		public static HashMap<String, double[]> rsNums;
		public static double[] statNums;
		public static double[] dur1;
		public static double[] dur2;

		public static void initKSstats(ResultSet rs) throws SQLException {
			try {

				ResultSetMetaData metaData = rs.getMetaData();
				int numberOfColumns = metaData.getColumnCount(); 
				rsNums = new HashMap<String, double[]>();

				// Get the column names, only show RS which matches userFields
				for (int col = 1; col <= numberOfColumns; col++) {

					int type = metaData.getColumnType(col);
					if ((type == Types.BIGINT) || (type == Types.DECIMAL) || (type == Types.DOUBLE) || 
							(type == Types.FLOAT) || (type == Types.NUMERIC) || (type == Types.INTEGER) || (type == Types.BOOLEAN)) {
						//statNums = new ArrayList<Object>();
						statNums = new double[(int)k_size];
						rsNums.put(metaData.getColumnLabel(col), statNums); 
					}
				}

				// Get all rows.
				while (rs.next()) {
					for (int i = 1; i <= numberOfColumns; i++) { 	
					
						if (rsNums.containsKey(metaData.getColumnLabel(i))) {
							//rsNums.put(metaData., value)
							
							//if (rs.getObject(i) != null) { rsNums.get(metaData.getColumnLabel(i)).add(rs.getObject(i)); }
							//rsNums.put(metaData.getColumnLabel(i), statNums);
						}
					}

					return;
				} 
			}
			catch (Exception e) { e.printStackTrace(); }

		}



		public static void splitByK(ResultSet rs) throws SQLException {

			//get num tuples, divide by k
			num_tuples = Connect.countRows();
			k_size = num_tuples / Struct.k_subsets;
			
			

			Iterator<Entry<String, double[]>> it = rsNums.entrySet().iterator();
			while (it.hasNext()) {

				//Map.Entry<String, double[]> pair = (Map.Entry<String, double[]>)it.next();

				
			}
			

		}

	}

	public static void publishQuery(ResultSet rs) throws SQLException {
		GUI.queryOutput.setModel(TableForm.queryTable(rs)); 
		GUI.queryOutput.setColumnModel(TableForm.colwidth(GUI.queryOutput));
		GUI.loadingProgress.setIndeterminate(false);
		GUI.loadingProgress.setValue(100);
	}

	public static void mainQuery(final String userQuery) throws SQLException {
		Thread ksThread = new Thread() {
			public void run() {
				try {

					ResultSet rs = executeQuery(userQuery.trim());
					
					publishQuery(rs);
					
					queryStats.initKSstats(rs);
					queryStats.splitByK(rs);
					

				} 
				catch (SQLException e) { e.printStackTrace(); }	
			}
		};
		ksThread.setName("ksThread");
		ksThread.start();

		return;
	}



}
