import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Connect {

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
		Statement stmt = conn.createStatement();
		System.out.println("Executed: \"" + command + "\"");
		return stmt.executeQuery(command);
	}

	public static ResultSet tableStats(String tableName) throws SQLException {

		Connection statsConn = null;
		String command = "SELECT TABLE_ROWS FROM TABLES WHERE TABLE_NAME = \'" + tableName + "\'";
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			statsConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/information_schema", user, password);
			statsConn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			if (!statsConn.isClosed()) { System.out.println("Connected to MySQL stats DB"); }
		} 
		catch (Exception e) { System.err.println("ERROR: " + e.getMessage()); }
		
		Statement stmt = statsConn.createStatement();
		System.out.println("Executed: \"" + command + "\"");
		
		return stmt.executeQuery(command);
	}
	
	
}
	