import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Connect {

	public static Connection conn = null;
	public static Statement stmt;
	public static ResultSet rs;
	public static String server = "jdbc:mysql://localhost:3306/dynamicDB";
	public static String user = "root";
	public static String password = "root";
	
	
	public static Connection getConnection() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(server, user, password);
			if (!conn.isClosed()) { System.out.println("Connected to MySQL"); }
		} 
		catch (Exception e) { System.err.println("ERROR: " + e.getMessage()); }
		return conn;
	}
	
	public static boolean executeUpdate(Connection conn, String command) throws SQLException {
		stmt = conn.createStatement();
		stmt.executeUpdate(command);
		System.out.println("Executed: \"" + command + "\"");
		return true;
	}
	
	public static ResultSet executeQuery(Connection conn, String command) throws SQLException {
		stmt = conn.createStatement();
		rs = stmt.executeQuery(command);
		System.out.println("Executed: \"" + command + "\"");
		return rs;
	}

	
}