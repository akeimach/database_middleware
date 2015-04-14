import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Connect {

	public static String tableName = "defaultTable"; //default
	public static String dbName = "testDynamic";
	public static Connection conn;
	public static Statement stmt;
	public static PreparedStatement pstmt;
	public static ResultSet rs;
	
	public static Connection getConnection() throws SQLException {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dbName, "root", "root");
			if(!conn.isClosed())
				System.out.println("Successfully connected to MySQL server");
		} 
		catch(Exception e) { System.err.println("ERROR: " + e.getMessage()); }
		return conn;
	}
	
	public static boolean executeUpdate(Connection conn, String command) throws SQLException {
		stmt = conn.createStatement();
		stmt.executeUpdate(command);
		System.out.println("Executed update \"" + command + "\"");
		return true;
	}
	
	public static ResultSet executeQuery(Connection conn, String command) throws SQLException {
		stmt = conn.createStatement();
		rs = stmt.executeQuery(command);
		System.out.println("Executed query \"" + command + "\"");
		return rs;
	}

	
}