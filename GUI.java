import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class GUI extends JPanel {

	public static JPanel tabLoad;
	public static JPanel tabSchema;
	public static JPanel tabQuery;
	public static JTable queryOutput;
	public static JTable initSchema;
	public static JTable currentSchema;
	public static boolean pressedBegin = false;
	public static boolean first = true;
	public static JComboBox comboType;

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
		//schemaTabContents(tabSchema);
		//query data
		tabQuery = new JPanel();
		tabQuery.setLayout(null);
		tabbedPane.addTab("Query data", null, tabQuery, null);
		tabQuery.setPreferredSize(new Dimension(600, 400));		
		//queryTabContents(tabQuery);
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
		path.setBounds(32, 20, 374, 20);
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
		btnBrowse.setBounds(428, 15, 128, 29);
		tabLoad.add(btnBrowse);
		//get table name
		final String instructions = " Select table name (optional)";
		final JTextArea getTableName = new JTextArea();
		getTableName.setText(instructions);
		getTableName.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent clear) {
				if (!pressedBegin) {
					getTableName.setText("");
					getTableName.setForeground(Color.BLACK);
				}
			}
		});
		getTableName.setForeground(Color.LIGHT_GRAY);
		getTableName.setBounds(32, 52, 374, 20);
		tabLoad.add(getTableName);
		//create progress bar
		final JProgressBar progressBar = new JProgressBar();
		//progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);
		progressBar.setBounds(177, 143, 224, 20);
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
				try { 
					LoadFile.initUpload(); 
					//make the other two tabs active only after data starts loading
					schemaTabContents(tabSchema);
					queryTabContents(tabQuery);
				}
				catch (SQLException e) { e.printStackTrace(); } 
				catch (IOException e) { e.printStackTrace(); }
			}
		});
		btnBegin.setBounds(231, 102, 117, 29);
		tabLoad.add(btnBegin);
		return tabLoad;
	}

	public static JPanel schemaTabContents(final JPanel tabSchema) {

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(36, 49, 506, 279);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		tabSchema.add(scrollPane);
		
		initSchema = new JTable();
		initSchema.setFillsViewportHeight(true);
		initSchema.setCellSelectionEnabled(true);
		try { initSchema.setModel(ChangeSchema.getInitSchemaRS()); } 
		catch (IOException e1) { e1.printStackTrace(); }
		initSchema.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		initSchema.setShowGrid(true);
		initSchema.setGridColor(Color.LIGHT_GRAY);
		initSchema.setColumnModel(ChangeSchema.colwidth(initSchema));

		scrollPane.setViewportView(initSchema);
		
		JButton btnAcceptChanges = new JButton("Accept Changes");
		btnAcceptChanges.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int countChanges = 0;
				for (int i = 0; i < ChangeSchema.topinitNames.size(); i++) {
					if (initSchema.getValueAt(0, i) != "Enter new title?") {
						ChangeSchema.changedName.setElementAt(initSchema.getValueAt(0, i), i);
						countChanges++;
					}
					if (initSchema.getValueAt(1, i) != ChangeSchema.topinitType.get(i)) {
						ChangeSchema.changedType.setElementAt(initSchema.getValueAt(1, i), i);
						countChanges++;
					}
				}
				if (countChanges > 0) { ChangeSchema.userChange(); }
			}
		});
		btnAcceptChanges.setBounds(217, 8, 145, 29);
		tabSchema.add(btnAcceptChanges);
		
		//after data has load initially
		//try { ChangeSchema.getCurrSchema(); } 
		
		return tabSchema;
	}

	

	public static JPanel queryTabContents(JPanel tabQuery) {

		final String instructions = " Input SQL query";
		final JTextArea sqlQueryIn = new JTextArea();

		sqlQueryIn.setText(instructions);
		sqlQueryIn.setEnabled(false);
		sqlQueryIn.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent clear) {
				sqlQueryIn.setEnabled(true);
				sqlQueryIn.setText("");
				sqlQueryIn.setForeground(Color.BLACK);
			}
		});
		sqlQueryIn.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent clear) {
				if (first) {
					first = false;
					sqlQueryIn.setEnabled(true);
					sqlQueryIn.setText("");
					sqlQueryIn.setForeground(Color.BLACK);
				}
			}
		});
		sqlQueryIn.setForeground(Color.LIGHT_GRAY);
		sqlQueryIn.setBounds(32, 20, 374, 20);
		tabQuery.add(sqlQueryIn);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(36, 61, 506, 254);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		tabQuery.add(scrollPane);

		JButton btnExecute = new JButton("Execute");
		btnExecute.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent sendUserInput) {

				if (sqlQueryIn.getText().equals(instructions)) { return; } //do nothing if no query
				else { QueryData.userQuery = sqlQueryIn.getText(); }
				try {
					QueryData.getResultSet();
					queryOutput = new JTable();
					queryOutput.setFillsViewportHeight(true);
					queryOutput.setCellSelectionEnabled(true);
					queryOutput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					queryOutput.setShowGrid(true);
					queryOutput.setGridColor(Color.LIGHT_GRAY);
					queryOutput.setModel(QueryData.queryResultSet(Connect.rs));
					queryOutput.setColumnModel(QueryData.colwidth(queryOutput));
					scrollPane.setViewportView(queryOutput);
					
				} 
				catch (SQLException e) { e.printStackTrace(); }
			}
		});
		btnExecute.setBounds(430, 15, 128, 29);
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
