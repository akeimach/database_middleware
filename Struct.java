import java.io.File;
import java.util.Vector;

public class Struct {

	public static String tableName = "table1"; //default
	public static Vector<String> columnNames;
	public static Vector<Integer> columnSize;
	public static Vector<String> columnType;

	public static Vector<String> fields;
	public static Vector<Integer> schemaRow;
	public static Vector<String> datatypes;
	
	public static File dataFile;
	public static int sampleLines = 15;
	public static String titleRow = new String();
	public static String[] initFields;
	public static String[] initTypes;
	public static int[] initSize;
	public static int numCols = 0;
	
	
	
	
}
