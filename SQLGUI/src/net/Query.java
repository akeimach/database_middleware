package net;

import ui.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JTextArea;

//send a query to the db
public class Query {

	//show the query in the GUI text area
	public static void publishQuery(ResultSet rs, JTextArea dbOutput) throws SQLException {
		GUI.queryOutput.setModel(TableForm.queryTable(rs, dbOutput)); 
		GUI.queryOutput.setColumnModel(TableForm.colwidth(GUI.queryOutput));
	}

	//use a new thread for retrieving the query
	public static void mainQuery(final String userQuery, final JTextArea dbOutput) throws SQLException {
		
		Thread bulkQueryThread = new Thread() {
			public void run() {
				try {
					ResultSet rs = Connect.executeQuery(userQuery.trim());
					publishQuery(rs, dbOutput);
				} 
				catch (SQLException e) { e.printStackTrace(); }	
			}
		};
		bulkQueryThread.setName("bulkQueryThread");
		bulkQueryThread.start();

	}



}
