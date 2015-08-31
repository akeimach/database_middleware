package net;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Connect {

	public static File dataFile;
	public static String tableName = "table1";
	public static String dbName = "dynamicDB";
	public static String server = "jdbc:mysql://localhost:3306/";
	public static String user = "root";
	public static String password = "root";

	//connect to DB
	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(server + dbName, user, password);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} catch (SQLException e) {
			throw new IllegalStateException("ERROR: " + e.getMessage(), e);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return conn;
	}

	//send an update to the db
	public static void executeUpdate(String command) throws SQLException {
		Connection conn = null;
		conn = getConnection();
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(command);
		System.out.println("Executed: \"" + command + "\"");
		return;
	}

	//send a query to the db
	public static ResultSet executeQuery(String command) throws SQLException {
		Connection conn = null;
		conn = getConnection();
		Statement stmt = conn.createStatement();
		System.out.println("Executed: \"" + command + "\"");
		return stmt.executeQuery(command);
	}

	//count the rows to determine decision tree outcome
	public static long countRows() throws SQLException {
		System.out.println("Getting table stats...");
		String orig = dbName;
		dbName = "information_schema";
		String command = "SELECT TABLE_ROWS FROM TABLES WHERE TABLE_NAME = \'" + tableName + "\'";

		ResultSet stats = executeQuery(command);
		dbName = orig;
		long table_size = 0;
		while (stats.next()) {
			table_size = stats.getLong(1);
		}
		System.out.println("Current table size: " + table_size);
		return table_size;
	}


}