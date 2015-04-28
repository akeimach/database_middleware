import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Connect {

	//public static Connection conn = null;
	//public static Statement stmt;
	//public static ResultSet rs;
	public static String server = "jdbc:mysql://localhost:3306/dynamicDB";
	public static String user = "root";
	public static String password = "root";
	
	
	public static Connection getConnection() throws SQLException {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(server, user, password);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			if (!conn.isClosed()) { System.out.println("Connected to MySQL"); }
		} 
		catch (Exception e) { System.err.println("ERROR: " + e.getMessage()); }
		return conn;
	}
	
	public static void executeUpdate(String command) throws SQLException {
		Connection conn = null;
		conn = getConnection();
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(command);
		System.out.println("Executed: \"" + command + "\"");
		return;
	}
	
	public static ResultSet executeQuery(String command) throws SQLException {
		Connection conn = null;
		conn = getConnection();
		//conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		Statement stmt = conn.createStatement();
		//rs = stmt.executeQuery(command);
		System.out.println("Executed: \"" + command + "\"");
		return stmt.executeQuery(command);
	}

	
}