import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class ChangeSchema extends Connect {

	public static boolean getCurrSchema() throws SQLException {
		conn = Connect.getConnection();
		String topLines = "SELECT * FROM " + Connect.tableName + " WHERE id < " + Parser.topFileSample;
		defaultrs = executeQuery(conn, topLines.trim());
		return true;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TableModel editHiddenResultSet(ResultSet defaultrs) {
		try {
			ResultSetMetaData metaData = defaultrs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			Vector columnNames = new Vector();
			// Get the column names
			//remove default primary and hidden version
			for (int column = 1; column < numberOfColumns - 1 ; column++) {
				columnNames.addElement(metaData.getColumnLabel(column + 1));
			}
			// Get all rows.
			Vector rows = new Vector();
			while (defaultrs.next()) {
				Vector newRow = new Vector();
				for (int i = 2; i < numberOfColumns; i++) { newRow.addElement(defaultrs.getObject(i)); }
				rows.addElement(newRow);
			}
			return new DefaultTableModel(rows, columnNames);
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
