import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect extends LoadFile {

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
	
}