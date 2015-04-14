import java.sql.SQLException;

public class ChangeSchema extends Connect {
	
	public static int numLines = 15;

	public static boolean getCurrSchema() throws SQLException {
		conn = Connect.getConnection();
		String topLines = "SELECT * FROM " + Connect.tableName + " WHERE id < " + numLines;
		rs = executeQuery(conn, topLines);
		return true;
	}
	
}
