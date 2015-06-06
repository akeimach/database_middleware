import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TableForm extends Struct {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TableModel schemaTable() {
		
		Vector<String> schemaTableCols = new Vector();
		schemaTableCols.addElement("Table");
		schemaTableCols.addElement("Name");
		schemaTableCols.addElement("Type");
		
		Vector rows = new Vector();
		Vector tableRow = new Vector();
		tableRow.addElement(tableName);
		tableRow.addElement(null);
		tableRow.addElement(null);
		rows.addElement(tableRow);
		
		for (int i = 0; i < userFields.length; i++) {
			Vector newRow = new Vector();
			newRow.addElement(null);
			newRow.addElement(userFields[i]); 
			newRow.addElement(userTypes[i]);
			rows.addElement(newRow);
		}
		int size = Math.max(Struct.curr_table_size, Struct.init_table_size);
		for (int i = userFields.length; i < size; i++) {
			Vector newRow = new Vector();
			rows.addElement(newRow);
		}

		return new DefaultTableModel(rows, schemaTableCols);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TableModel queryTable(ResultSet rs) throws SQLException {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			Vector columnNames = new Vector();
			// Get the column names, only show RS which matches userFields
			
			for (int col = 1; col <= numberOfColumns; col++) {
				for (int usercol = 0; usercol < userFields.length; usercol++) {
					if (metaData.getColumnLabel(col).equalsIgnoreCase(userFields[usercol])) {
						columnNames.addElement(userFields[usercol]); //use header w/ original correct case
					}
				}
			}
			
			// Get all rows.
			Vector rows = new Vector();
			int rowsReturned = 0;
			while (rs.next()) {
				Vector newRow = new Vector();
				for (int i = 1; i <= numberOfColumns; i++) { 
					for (int usercol = 0; usercol < userFields.length; usercol++) {
						if (metaData.getColumnLabel(i).equalsIgnoreCase(userFields[usercol])) {
							newRow.addElement(rs.getObject(i));
						}
					}
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
	
}