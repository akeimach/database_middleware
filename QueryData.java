import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class QueryData extends Connect {

	public static String userQuery;

	public static boolean getResultSet() throws SQLException {
		conn = Connect.getConnection();
		rs = executeQuery(conn, userQuery.trim());
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TableModel queryResultSet(ResultSet rs) {
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
			while (rs.next()) {
				Vector newRow = new Vector();
				for (int i = 2; i < numberOfColumns; i++) { newRow.addElement(rs.getObject(i)); }
				rows.addElement(newRow);
			}
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
		}
		return columnModel;
	}

}
