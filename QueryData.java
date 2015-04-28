import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class QueryData extends Connect {

	static ResultSet queryRS = null;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TableModel resultTable(ResultSet rs) throws SQLException {

		
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			Vector columnNames = new Vector();
			// Get the column names
			//remove default primary and hidden version
			for (int column = 1; column < numberOfColumns - 1 ; column++) {
				columnNames.addElement(metaData.getColumnLabel(column + 1));
			}
			// Get all rows.
			Vector rows = new Vector();
			int rowsReturned = 0;
			while (rs.next()) {
				Vector newRow = new Vector();
				for (int i = 2; i < numberOfColumns; i++) { 
					newRow.addElement(rs.getObject(i)); 
				}
				rows.addElement(newRow);
				rowsReturned++;
			}
			GUI.dbOutput.append(rowsReturned + " rows returned\n");
			return new DefaultTableModel(rows, columnNames);
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public static TableColumnModel colwidth(JTable table) {
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < table.getColumnCount(); i++) {
			columnModel.getColumn(i).setPreferredWidth(100);
			columnModel.getColumn(i).setMinWidth(70);
		}
		return columnModel;
	}
	
	
	public static void mainQuery(final String userQuery) throws SQLException {
		//final ResultSet queryRS;
		Thread queryThread = new Thread() {
			public void run() {
				try {
					//Connection conn = Connect.getConnection();
					queryRS = executeQuery(userQuery.trim());
					GUI.queryOutput.setModel(QueryData.resultTable(queryRS)); 
					GUI.queryOutput.setColumnModel(QueryData.colwidth(GUI.queryOutput));
				} 
				catch (SQLException e) { e.printStackTrace(); }	
			}
		};
		queryThread.start();
		
		//queryRS.close();
		
		return;
	}

}
