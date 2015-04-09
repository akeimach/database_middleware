import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import au.com.bytecode.opencsv.CSVReader;

@SuppressWarnings("serial")
public class HandleData extends Schema {

	public static int tableInt = 0; //increment each new table
	public static String tableName = "table" + tableInt;
	public static boolean tableExists = true; //for development
	public static Connection conn = null;
	public static PreparedStatement pstmt = null;
	public static ResultSet rs = null;
	public static Statement stmt = null;
	
	//SQL commands without recordset (CREATE/INSERT/UPDATE/DELETE/DROP)
	public static boolean executeUpdate(Connection conn, String command) throws SQLException {
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(command);
			System.out.println("Complete: " + command);
			return true;
		} 
		finally { if (stmt != null) stmt.close(); }
	}

	public static void tableInit() throws SQLException {
		conn = Connect.getConnection();
		if (tableExists = true) {
			try {
				String dropString = "DROP TABLE " + tableName; //Drop table
				executeUpdate(conn, dropString);
			}
			catch (SQLException e) {
				System.out.println("ERROR: Could not drop the table");
				e.printStackTrace();
			}
		}
		try {
			String createString = "CREATE TABLE " + tableName + " ("; //Create new
			for (int i = 0; i < Schema.tableSize; i++ ) {
				createString += schemaVals[i] + " " + schemaTypes[i] + " NOT NULL, ";
			}
			createString += "PRIMARY KEY (" + schemaVals[0] + "))";
			executeUpdate(conn, createString);
			tableExists = true;
		}
		catch (SQLException e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
			return;
		}

		loadData();
		return;
	}

	public static void loadData() throws SQLException {
		try {
			conn = Connect.getConnection();
			FileReader file = new FileReader(path);
			CSVReader reader = new CSVReader(file, ','); 
			
			String insertQuery = "INSERT INTO " + tableName + " VALUES (";
			for (int i = 0; i < Schema.tableSize - 1; i++) { insertQuery += "?, "; }
			insertQuery += "?)";
			
			System.out.println(insertQuery);
			pstmt = conn.prepareStatement(insertQuery);
			String[] line = reader.readNext();
			for (String data : line) { System.out.print(data + "|"); }
			while(line != null) {
				int i = 1;
				for (String data : line) {
					pstmt.setString(i, data);
					i++;
				}
				line = reader.readNext();
				System.out.println(line);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			System.out.println("Data Successfully Uploaded"); 
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	public static void sendQuery(String query) throws SQLException {
		try {
			conn = Connect.getConnection();
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			System.out.println(query);
			System.out.println(rs.getRow());
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
		}
		finally {
			if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException ignore) {}
			if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
		}
	}
}