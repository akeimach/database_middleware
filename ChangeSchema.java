import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class ChangeSchema extends Struct {

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TableModel schemaTable() {
		
		Vector<String> schemaTableCols = new Vector();
		schemaTableCols.addElement("Table");
		schemaTableCols.addElement("Name");
		schemaTableCols.addElement("Type");
		
		Vector rows = new Vector();
		Vector tableRow = new Vector();
		tableRow.addElement(Struct.tableName);
		tableRow.addElement(null);
		tableRow.addElement(null);
		rows.addElement(tableRow);
		
		if (!GUI.titleRow) { Struct.columnTitles = Struct.initFields; }
		for (int i = 0; i < Struct.columnTitles.length; i++) {
			Vector newRow = new Vector();
			newRow.addElement(null);
			newRow.addElement(Struct.columnTitles[i]);
			newRow.addElement(Struct.initTypes[i]);
			rows.addElement(newRow);
		}
		return new DefaultTableModel(rows, schemaTableCols);
	}
	
	
}
