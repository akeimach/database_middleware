import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import au.com.bytecode.opencsv.CSVReader;

public class ImportCsv extends DBConnection {

	private static void readCsv() {
		try {
			FileReader file = new FileReader("upload.csv");
			CSVReader reader = new CSVReader(file, ','); 
			Connection conn = getConnection();
			String insertQuery = "INSERT INTO test VALUES (?, ?, ?)";
			PreparedStatement pstmt = conn.prepareStatement(insertQuery);
			int i = 1;
			while(reader.readNext() != null){
				for (String data : reader.readNext()) {
					pstmt.setString(i, data);
					i++;
				}
				i = 1;
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			System.out.println("Data Successfully Uploaded"); 
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	private static void readCsvUsingLoad() {
		try {
			Connection conn = getConnection();
			String loadQuery = "LOAD DATA LOCAL INFILE '" + "upload.csv" + "' INTO TABLE test FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' (a1, a2, a3) ";
			System.out.println(loadQuery);
			Statement stmt = conn.createStatement();
			stmt.execute(loadQuery);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	public static void main(String[] args) {
		try { 
			readCsvUsingLoad(); 
			readCsv();
		} 
		catch (Exception ex) {}
	}
}