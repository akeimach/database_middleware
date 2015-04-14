import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class GUI extends JPanel {

	public static JPanel tabLoad;
	public static JPanel tabSchema;
	public static JPanel tabQuery;
	public static JTable queryOutput;
	public static boolean pressedBegin = false;

	public GUI() {
		setLayout(new BorderLayout(0, 0));
		JTabbedPane tabbedPane = new JTabbedPane();
		//load file
		tabLoad = new JPanel();
		tabLoad.setLayout(null);
		tabbedPane.addTab("Load file", null, tabLoad, null);
		tabLoad.setPreferredSize(new Dimension(600, 400));
		loadTabContents(tabLoad); //only one initiated on start-up
		//change schema
		tabSchema = new JPanel();
		tabSchema.setLayout(null);
		tabbedPane.addTab("Change schema", null, tabSchema, null);
		tabSchema.setPreferredSize(new Dimension(600, 400));
		//query data
		tabQuery = new JPanel();
		tabQuery.setLayout(null);
		tabbedPane.addTab("Query data", null, tabQuery, null);
		tabQuery.setPreferredSize(new Dimension(600, 400));		
		queryTabContents(tabQuery);
		//add to gui pane
		add(tabbedPane);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	public static JPanel loadTabContents(final JPanel tabLoad) {
		//file path box
		final JTextArea path = new JTextArea();
		path.setEditable(false);
		path.setForeground(Color.LIGHT_GRAY);
		path.setText(" Select data file to upload");
		path.setBounds(32, 36, 374, 16);
		tabLoad.add(path);
		//browse button, saves data file to File "file", sets file path
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent uplaod) {
				JFileChooser fileChooser = new JFileChooser();
				int val = fileChooser.showOpenDialog(fileChooser);
				if (val == JFileChooser.APPROVE_OPTION) {
					LoadFile.file = fileChooser.getSelectedFile();
					path.setText(" " + LoadFile.file.getAbsolutePath());
				}
			}
		});
		btnBrowse.setBounds(428, 31, 128, 29);
		tabLoad.add(btnBrowse);
		//get table name
		final String instructions = " Select table name (optional)";
		final JTextArea getTableName = new JTextArea();
		getTableName.setText(instructions);
		getTableName.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent clear) {
				if (pressedBegin == false) {
					getTableName.setText("");
					getTableName.setForeground(Color.BLACK);
				}
			}
		});
		getTableName.setForeground(Color.LIGHT_GRAY);
		getTableName.setBounds(32, 64, 374, 16);
		tabLoad.add(getTableName);
		//create progress bar
		final JProgressBar progressBar = new JProgressBar();
		//progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);
		progressBar.setBounds(225, 133, 146, 20);
		progressBar.setVisible(false);
		tabLoad.add(progressBar);
		//start loading data, get input table name
		JButton btnBegin = new JButton("Begin");
		btnBegin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent begin) {
				pressedBegin = true;
				if (getTableName.getText().equals(instructions)) { LoadFile.tableName = "defaultTable"; }
				else { LoadFile.tableName = getTableName.getText(); }
				getTableName.setText(" " + LoadFile.tableName);
				getTableName.setEditable(false);
				progressBar.setVisible(true);
				try { LoadFile.initUpload(); }
				catch (SQLException e) { e.printStackTrace(); } 
				catch (IOException e) { e.printStackTrace(); }
			}
		});
		btnBegin.setBounds(240, 92, 117, 29);
		tabLoad.add(btnBegin);
		return tabLoad;
	}

	public static JPanel schemaTabContents(final JPanel tabSchema) {
		return tabSchema;
	}

	public static JPanel queryTabContents(JPanel tabQuery) {

		//get table name
		final String queryInstruct = " Input SQL query";
		final JTextArea sqlQueryIn = new JTextArea();
		sqlQueryIn.setEditable(false);
		sqlQueryIn.setText(queryInstruct);
		sqlQueryIn.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent clear) {
				if (pressedBegin == true) {
					sqlQueryIn.setEditable(true);
					sqlQueryIn.setText("");
					sqlQueryIn.setForeground(Color.BLACK);
				}
			}
		});
		sqlQueryIn.setForeground(Color.LIGHT_GRAY);
		sqlQueryIn.setBounds(31, 36, 374, 16);
		tabQuery.add(sqlQueryIn);
		
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(31, 92, 506, 223);
		tabQuery.add(scrollPane);

		JButton btnExecute = new JButton("Execute");
		btnExecute.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent sendUserInput) {
				if (pressedBegin == true) {
					if (sqlQueryIn.getText().equals(queryInstruct)) { return; } //do nothing if no query
					else {  QueryData.userQuery = sqlQueryIn.getText(); }
					try {
						QueryData.getResultSet();
						queryOutput = new JTable();
						queryOutput.setModel(QueryData.tableHiddenResultSet(Connect.rs));
						scrollPane.setViewportView(queryOutput);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		btnExecute.setBounds(428, 31, 128, 29);
		tabQuery.add(btnExecute);

		return tabQuery;
	}   

	private static void createAndShowGUI() {
		JFrame frame = new JFrame("Dynamic Database Simulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new GUI(), BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		createAndShowGUI();
	}
}
