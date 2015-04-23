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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
	public static JTable queryOutput;
	public static boolean key_pressed = false;

	//RIGHT SIDE: Change schema
	public static JPanel schemaPane;
	public static JTable initSchema;
	

	public GUI() {

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataPane, schemaPane);
		splitPane.setPreferredSize(new Dimension(1000, 700));
		splitPane.setContinuousLayout(true);
		
		dataPane = new JTabbedPane();
		dataPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		schemaPane = new JPanel();
		
		//Only initialte load on startup--all others depend on load
		tabLoad = new JPanel();
		dataPane.addTab("Load file", null, tabLoad, null);
		loadTabContents(tabLoad);
		
		tabQuery = new JPanel();
		tabQuery.setLayout(null);	
		dataPane.addTab("Query data", null, tabQuery, null);
		
		splitPane.setLeftComponent(dataPane);
		splitPane.setRightComponent(schemaPane);
		
		//add to gui pane
		add(splitPane);

	}

	public static JPanel loadTabContents(final JPanel tabLoad) {
		
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
					LoadFile.file = fileChooser.getSelectedFile();
					path.setText(" " + LoadFile.file.getAbsolutePath());
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
				if (getTableName.getText().equals(instructions)) { LoadFile.tableName = "defaultTable"; }
				else { LoadFile.tableName = getTableName.getText(); }
				getTableName.setText(" " + LoadFile.tableName);
				getTableName.setEditable(false);
				progressBar.setVisible(true);

				//make other tabs available
				try { LoadFile.initUpload(); } 
				catch (SQLException e) { e.printStackTrace(); } 
				catch (IOException e) { e.printStackTrace(); }
				schemaContents(schemaPane);
				queryContents(tabQuery);

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

	public static JPanel schemaContents(final JPanel schemaPane) {

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(40, 70, 700, 450);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		schemaPane.add(scrollPane);

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

		JButton btnAcceptChanges = new JButton("Submit Changes");
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
		btnAcceptChanges.setBounds(317, 28, 145, 29);
		schemaPane.add(btnAcceptChanges);

		return schemaPane;
	}


	public static JPanel queryContents(JPanel tabQuery) {

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
				if (!key_pressed) {
					key_pressed = true; //only first key pressed erases the initial contents
					sqlQueryIn.setEnabled(true);
					sqlQueryIn.setText("");
					sqlQueryIn.setForeground(Color.BLACK);
				}
			}
		});
		sqlQueryIn.setForeground(Color.LIGHT_GRAY);
		sqlQueryIn.setBounds(44, 20, 554, 20);
		tabQuery.add(sqlQueryIn);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(40, 70, 700, 450);
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
		btnExecute.setBounds(610, 15, 128, 29);
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
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
				splitPane.setDividerLocation(splitPane.getSize().width / 2);
			}
		});
	}
}
