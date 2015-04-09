import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class Connect extends GUI {

	private final static String dbName = "testDynamic";

	public static Connection getConnection() throws SQLException {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dbName, "root", "root");
			if(!conn.isClosed())
				System.out.println("Successfully connected to MySQL server using TCP/IP");
		} 
		catch(Exception e) { System.err.println("Exception: " + e.getMessage()); }
		return conn;
	}
}