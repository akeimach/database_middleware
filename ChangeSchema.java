import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class ChangeSchema extends Connect {

	public static Vector<String> columnNames;
	public static Vector<Integer> columnSize;
	public static Vector<String> columnType;
	public static DefaultTableModel tbm;
	public static TableColumnModel cols;
	public static TableModelListener schemaChange;
	@SuppressWarnings("rawtypes")
	public static Vector changedTitles;
	@SuppressWarnings("rawtypes")
	public static Vector topinitNames;
	@SuppressWarnings("rawtypes")
	public static Vector topinitType;
	@SuppressWarnings("rawtypes")
	public static Vector changedType;
	@SuppressWarnings("rawtypes")
	public static Vector changedName;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DefaultTableModel getInitSchemaRS() throws IOException {

		Vector readrows = new Vector();

		// Get the column names
		topinitNames = new Vector();
		for (String name : Parser.defaultFields) { topinitNames.addElement(name); }


		//set top two rows: (editable) titles and size
		Vector topinitSize = new Vector();
		for (@SuppressWarnings("unused") String title : Parser.defaultFields) { topinitSize.addElement("Enter new title?"); }
		readrows.addElement(topinitSize);

		topinitType = new Vector();
		for (String type : Parser.defaultTypes) { topinitType.addElement(type); }
		readrows.addElement(topinitType);
	
		
		for (int i = 0; i < Parser.initrows.size(); i++) {
			Object row = Parser.initrows.get(i);
			readrows.addElement(row);
		}
		
		changedType = new Vector();
		//changedType.equals(topinitType.clone());
		changedType = topinitType;
		changedName = new Vector();
		//changedName.equals(topinitNames.clone());
		changedName = topinitNames;
		
		return new DefaultTableModel(readrows, topinitNames);
	}


	public static boolean getCurrSchema() throws SQLException {
		conn = Connect.getConnection();
		String topLines = "SELECT * FROM " + Connect.tableName + " WHERE id < " + Parser.topFileSample;
		defaultrs = executeQuery(conn, topLines.trim());
		return true;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TableModel schemaResultSet(ResultSet defaultrs) {
		try {
			ResultSetMetaData metaData = defaultrs.getMetaData();
			int numberOfColumns = metaData.getColumnCount(); 
			columnNames = new Vector();
			columnSize = new Vector();
			columnType = new Vector();
			// Get the column names
			//remove default primary and hidden version
			for (int column = 1; column < numberOfColumns - 1 ; column++) {
				columnNames.addElement(metaData.getColumnLabel(column + 1));
				columnSize.addElement(metaData.getColumnDisplaySize(column + 1));
				columnType.addElement(metaData.getColumnClassName(column + 1));
				//System.out.println(metaData.getColumnDisplaySize(column + 1));
				//+ metaData.getColumnClassName(column + 1));
			}

			// Get all rows.
			Vector rows = new Vector();
			//top two for user edit
			Vector dname = new Vector();
			Vector dtype = new Vector();
			for (int i = 2; i < numberOfColumns; i++) { 
				dname.addElement("Enter new title"); 
				JComboBox comboBox = new JComboBox();
				comboBox.setModel(new DefaultComboBoxModel(new String[] {"INT", "BIGINT", "FLOAT",
						"DOUBLE", "BIT", "CHAR", "VARCHAR", "TEXT", "DATE", "DATETIME", "TIME", "TIMESTAMP", "YEAR"}));
				comboBox.setToolTipText("");
				dtype.addElement("Enter data type"); 
			}
			rows.addElement(dname);
			rows.addElement(dtype);

			while (defaultrs.next()) {
				Vector newRow = new Vector();
				for (int i = 2; i < numberOfColumns; i++) { 
					newRow.addElement(defaultrs.getObject(i)); 

				}
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


	public static void userChange() {
		//set new title (if any)
		
		for (int i = 0; i < changedType.size(); i++) {
			System.out.print(changedType.elementAt(i) + " ");
		}

		for (int i = 0; i < changedName.size(); i++) {
			System.out.print(changedName.elementAt(i) + " ");
		}
		//set new type (if any)
	}

}
