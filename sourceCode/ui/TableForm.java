package ui;

import data.Parser;
import data.UserView;
import net.Connect;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TableForm {

	//make the schema table for the GUI
	@SuppressWarnings({
		"rawtypes", "unchecked"
	})
	public static TableModel schemaTable() {

		Vector < String > schemaTableCols = new Vector();
		schemaTableCols.addElement("Table");
		schemaTableCols.addElement("Name");
		schemaTableCols.addElement("Type");

		Vector rows = new Vector();
		Vector tableRow = new Vector();
		tableRow.addElement(Connect.tableName);
		tableRow.addElement(null);
		tableRow.addElement(null);
		rows.addElement(tableRow);

		for (int i = 0; i < UserView.fields.length; i++) {
			Vector newRow = new Vector();
			newRow.addElement(null);
			newRow.addElement(UserView.fields[i]);
			newRow.addElement(UserView.types[i]);
			rows.addElement(newRow);
		}
		int size = Math.max(UserView.tableSize, Parser.tableSize);
		for (int i = UserView.fields.length; i < size; i++) {
			Vector newRow = new Vector();
			rows.addElement(newRow);
		}

		return new DefaultTableModel(rows, schemaTableCols);
	}

	//make the query table for the GUI
	@SuppressWarnings({
		"rawtypes", "unchecked"
	})
	public static TableModel queryTable(ResultSet rs, JTextArea dbOutput) throws SQLException {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount();
			Vector columnNames = new Vector();
			// Get the column names, only show RS which matches userFields

			for (int col = 1; col <= numberOfColumns; col++) {
				for (int usercol = 0; usercol < UserView.fields.length; usercol++) {
					if (metaData.getColumnLabel(col).equalsIgnoreCase(UserView.fields[usercol])) {
						columnNames.addElement(UserView.fields[usercol]); //use header w/ original correct case
					}
				}
			}

			// Get all rows.
			Vector rows = new Vector();
			int rowsReturned = 0;
			while (rs.next()) {
				Vector newRow = new Vector();
				for (int i = 1; i <= numberOfColumns; i++) {
					for (int usercol = 0; usercol < UserView.fields.length; usercol++) {
						if (metaData.getColumnLabel(i).equalsIgnoreCase(UserView.fields[usercol])) {
							newRow.addElement(rs.getObject(i));
						}
					}
				}
				rows.addElement(newRow);
				rowsReturned++;
			}

			dbOutput.append(rowsReturned + " rows returned\n");
			return new DefaultTableModel(rows, columnNames);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	//column model widths
	public static TableColumnModel colwidth(JTable table) {
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < table.getColumnCount(); i++) {
			columnModel.getColumn(i).setPreferredWidth(100);
			columnModel.getColumn(i).setMinWidth(70);
		}
		return columnModel;
	}

}