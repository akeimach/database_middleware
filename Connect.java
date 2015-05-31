import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;


public class Connect {

	public static String server = "jdbc:mysql://localhost:3306/";
	public static String user = "root";
	public static String password = "root";

	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(server + Struct.dbName, user, password);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} 
		catch (SQLException e) { throw new IllegalStateException("ERROR: " + e.getMessage(), e); } 
		catch (InstantiationException e) { e.printStackTrace(); } 
		catch (IllegalAccessException e) { e.printStackTrace(); } 
		catch (ClassNotFoundException e) { e.printStackTrace(); }
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
	
	public static long countRows() throws SQLException {
		System.out.println("Getting table stats...");
		String orig = Struct.dbName;
		Struct.dbName = "information_schema";
		String command = "SELECT TABLE_ROWS FROM TABLES WHERE TABLE_NAME = \'" + Struct.tableName + "\'";
		
		ResultSet stats = executeQuery(command);
		Struct.dbName = orig;
		while (stats.next()) { Struct.table_size = stats.getLong(1); }
		System.out.println("Current table size: " + Struct.table_size);
		return Struct.table_size;
	}
	/*
	public static ResultSet tableStats() throws SQLException {
		String orig = Struct.dbName;
		Struct.dbName = "information_schema";
		Connection conn = null;
		conn = getConnection();
		String command = "SELECT TABLE_ROWS FROM TABLES WHERE TABLE_NAME = \'" + Struct.tableName + "\'";
		Statement stmt = conn.createStatement();
		System.out.println("Executed: \"" + command + "\"");
		Struct.dbName = orig;
		return stmt.executeQuery(command);
	}
	*/

	
}
