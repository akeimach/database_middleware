import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

@SuppressWarnings("serial")
public class GUI extends JPanel {

	//change schema always visible
	public static JSplitPane splitPane;

	//LEFT SIDE: Load file and execute query
	public static JTabbedPane dataPane;
	//Load file tab
	public static JPanel tabLoad;
	public static boolean begin_pressed = false;
	//Query data
	public static JPanel tabQuery;
	public static JSplitPane querySplitPane;
	private static JTextArea txtrQueryinput;
	public static JScrollPane inputScrollPane;
	public static JTable queryOutput;
	public static JScrollPane outputScrollPane;
	public static JTextArea dbOutput;
	public static JScrollPane dbOutputScrollPane;
	public static boolean user_typing = false;
	private static JButton btnExecute;

	//RIGHT SIDE: Change schema
	public static JPanel schemaPane;
	public static JTable initSchema;



	public GUI() {

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataPane, schemaPane);
		splitPane.setPreferredSize(new Dimension(1000, 700));
		splitPane.setContinuousLayout(true);

		dataPane = new JTabbedPane();
		dataPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		tabLoad = new JPanel();
		loadContents(tabLoad);
		dataPane.addTab("Load file", null, tabLoad, null);

		tabQuery = new JPanel();
		queryContents(tabQuery);
		dataPane.addTab("Query data", null, tabQuery, null);

		schemaPane = new JPanel();
		schemaContents(schemaPane);

		splitPane.setLeftComponent(dataPane);
		splitPane.setRightComponent(schemaPane);

		//add to gui pane
		add(splitPane);

	}


	public static JPanel loadContents(final JPanel tabLoad) {

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{18, 215, 124, 0};
		gbl_panel.rowHeights = new int[]{25, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		tabLoad.setLayout(gbl_panel);

		//file path box
		final JTextArea path = new JTextArea();
		path.setEditable(false);
		path.setForeground(Color.LIGHT_GRAY);
		path.setText(" Select data file to upload");
		GridBagConstraints gbc_path = new GridBagConstraints();
		gbc_path.fill = GridBagConstraints.HORIZONTAL;
		gbc_path.insets = new Insets(0, 0, 5, 5);
		gbc_path.gridx = 1;
		gbc_path.gridy = 0;
		path.setColumns(10);
		tabLoad.add(path, gbc_path);

		//browse button, saves data file to File "file", sets file path
		JButton btnBrowse = new JButton("Browse");
		GridBagConstraints gbc_browse = new GridBagConstraints();
		gbc_browse.insets = new Insets(0, 0, 5, 0);
		gbc_browse.gridx = 2;
		gbc_browse.gridy = 0;
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent uplaod) {
				JFileChooser fileChooser = new JFileChooser();
				int val = fileChooser.showOpenDialog(fileChooser);
				if (val == JFileChooser.APPROVE_OPTION) {
					Parser.file = fileChooser.getSelectedFile();
					path.setText(" " + Parser.file.getAbsolutePath());
				}
			}
		});
		btnBrowse.setBounds(526, 39, 128, 29);
		tabLoad.add(btnBrowse, gbc_browse);

		//get table name
		final String instructions = " Select table name (optional)";
		final JTextArea getTableName = new JTextArea();
		GridBagConstraints gbc_setTable = new GridBagConstraints();
		gbc_setTable.fill = GridBagConstraints.HORIZONTAL;
		gbc_setTable.insets = new Insets(0, 0, 5, 5);
		gbc_setTable.gridx = 1;
		gbc_setTable.gridy = 1;
		getTableName.setColumns(10);
		getTableName.setText(instructions);
		getTableName.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent clear) {
				if (!begin_pressed) {
					getTableName.setText("");
					getTableName.setForeground(Color.BLACK);
				}
			}
		});
		getTableName.setForeground(Color.LIGHT_GRAY);
		getTableName.setBounds(127, 76, 393, 20);
		tabLoad.add(getTableName, gbc_setTable);

		//create progress bar
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 0, 5);
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 4;
		progressBar.setVisible(false);
		tabLoad.add(progressBar, gbc_progressBar);

		//begin program, make schema and query tabs active
		JButton btnBegin = new JButton("Begin");
		GridBagConstraints gbc_begin = new GridBagConstraints();
		gbc_begin.insets = new Insets(0, 0, 5, 5);
		gbc_begin.gridx = 1;
		gbc_begin.gridy = 3;
		btnBegin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent loader) {
				begin_pressed = true;
				if (getTableName.getText().equals(instructions)) { Struct.tableName = "defaultTable"; }
				else { Struct.tableName = getTableName.getText(); }
				getTableName.setText(" " + Struct.tableName);
				getTableName.setEditable(false);
				progressBar.setVisible(true);

				//make other tabs available
				try { 
					LoadFile.initUpload(); 
					initSchema.setModel(ChangeSchema.getInitSchemaRS());
				} 
				catch (SQLException e) { e.printStackTrace(); } 
				catch (IOException e) { e.printStackTrace(); }

				//start background loader thread
				Thread queryThread = new Thread() {
					public void run() {
						LoadFile.startBulkLoad();
					}
				};
				queryThread.start();
			}
		});
		btnBegin.setBounds(331, 119, 117, 29);
		tabLoad.add(btnBegin, gbc_begin);

		return tabLoad;
	}


	public static JPanel queryContents(final JPanel tabQuery) {

		GridBagLayout gbl_tabQuery = new GridBagLayout();
		gbl_tabQuery.columnWidths = new int[]{324, 0};
		gbl_tabQuery.rowHeights = new int[]{0, 411, 0, 284, 0};
		gbl_tabQuery.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_tabQuery.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		tabQuery.setLayout(gbl_tabQuery);

		final String instructions = "Enter SQL query";
		txtrQueryinput = new JTextArea();
		txtrQueryinput.setText(instructions);
		txtrQueryinput.setEditable(false);
		txtrQueryinput.setEnabled(false);
		txtrQueryinput.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent clear) {
				if (user_typing == false) { //only clears first time clicked
					txtrQueryinput.setEditable(true);
					txtrQueryinput.setEnabled(true);
					txtrQueryinput.setText("");
					txtrQueryinput.setForeground(Color.BLACK);
					user_typing = true;
				}
			}
		});
		txtrQueryinput.setForeground(Color.LIGHT_GRAY);
		inputScrollPane = new JScrollPane();
		inputScrollPane.setEnabled(true);
		inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		inputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		inputScrollPane.setViewportView(txtrQueryinput);

		queryOutput = new JTable();
		outputScrollPane = new JScrollPane();
		outputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		outputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		outputScrollPane.setViewportView(queryOutput);

		querySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputScrollPane, outputScrollPane);
		querySplitPane.setContinuousLayout(true);
		querySplitPane.setTopComponent(inputScrollPane);
		querySplitPane.setBottomComponent(outputScrollPane);
		querySplitPane.setResizeWeight(0.3);

		GridBagConstraints gbc_queries = new GridBagConstraints();
		gbc_queries.insets = new Insets(0, 0, 5, 0);
		gbc_queries.fill = GridBagConstraints.BOTH;
		gbc_queries.gridx = 0;
		gbc_queries.gridy = 1;
		tabQuery.add(querySplitPane, gbc_queries);

		btnExecute = new JButton("Execute");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 0;
		btnExecute.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent sendUserInput) {
				if (txtrQueryinput.getText().equals(instructions)) { return; } //do nothing if no query
				else { 
					QueryData.userQuery = txtrQueryinput.getText(); 
					user_typing = false; //user finished writing their query
				}
				try {
					QueryData.getResultSet();
					queryOutput.setFillsViewportHeight(true);
					queryOutput.setCellSelectionEnabled(true);
					queryOutput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					queryOutput.setShowGrid(true);
					queryOutput.setGridColor(Color.LIGHT_GRAY);
					queryOutput.setModel(QueryData.queryResultSet(Connect.rs));
					queryOutput.setColumnModel(QueryData.colwidth(queryOutput));
					outputScrollPane.setViewportView(queryOutput);
				} 
				catch (SQLException e) { e.printStackTrace(); }
			}
		});
		tabQuery.add(btnExecute, gbc_btnNewButton);


		dbOutput = new JTextArea();
		dbOutput.setEditable(false);
		dbOutputScrollPane = new JScrollPane();
		dbOutputScrollPane.setViewportView(dbOutput);
		GridBagConstraints gbc_dbOut = new GridBagConstraints();
		gbc_dbOut.fill = GridBagConstraints.BOTH;
		gbc_dbOut.gridx = 0;
		gbc_dbOut.gridy = 3;
		tabQuery.add(dbOutputScrollPane, gbc_dbOut);


		return tabQuery;
	} 


	public static JPanel schemaContents(final JPanel schemaPane) {

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(40, 70, 700, 450);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		schemaPane.add(scrollPane);

		initSchema = new JTable();
		initSchema.setFillsViewportHeight(true);
		initSchema.setCellSelectionEnabled(true);

		initSchema.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		initSchema.setShowGrid(true);
		initSchema.setGridColor(Color.LIGHT_GRAY);
		initSchema.setColumnModel(ChangeSchema.colwidth(initSchema));

		scrollPane.setViewportView(initSchema);

		JButton btnAcceptChanges = new JButton("Submit Changes");
		btnAcceptChanges.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (begin_pressed) {
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
			}
		});
		btnAcceptChanges.setBounds(317, 28, 145, 29);
		schemaPane.add(btnAcceptChanges);

		return schemaPane;
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
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
				splitPane.setDividerLocation(700);
				//splitPane.setDividerLocation(splitPane.getSize().width / 2);
			}
		});
	}
}
