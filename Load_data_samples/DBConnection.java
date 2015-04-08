import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testDynamic", "root", "root");
			if(!conn.isClosed())
				System.out.println("Successfully connected to " +
						"MySQL server using TCP/IP...");
		} 
		catch(Exception e) { System.err.println("Exception: " + e.getMessage()); }
		return conn;
	}
}