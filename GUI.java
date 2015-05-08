import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;


@SuppressWarnings("serial")
public class GUI extends JPanel {

	//Holds everything
	public static JSplitPane splitPane;
	public static GridBagLayout gbl_panel;

	//LEFT SIDE: Load file/execute query, RIGHT SIDE: Change schema
	public static JTabbedPane dataPane;
	public static JTabbedPane schemaPane;

	//Load file tab
	public static JPanel tabLoad;
	public static boolean begin_pressed = false;
	public static boolean titleRow = true;

	//Query data tab
	public static JPanel tabQuery;
	public static JSplitPane querySplitPane;
	private static JTextArea queryInput;
	public static JTable queryOutput;
	public static JTextArea dbOutput;
	public static boolean user_typing = false;

	//Schema tab
	public static JPanel tabSchema;
	public static JTable viewSchema;


	public GUI() {

		//Holds everything
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataPane, schemaPane);
		splitPane.setPreferredSize(new Dimension(1000, 700));
		splitPane.setContinuousLayout(true);

		//LEFT SIDE: Load file/execute query, RIGHT SIDE: Change schema
		dataPane = new JTabbedPane();
		dataPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		schemaPane = new JTabbedPane();
		schemaPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		//Load file tab
		tabLoad = new JPanel();
		loadContents(tabLoad);
		dataPane.addTab("Load file", null, tabLoad, null);

		//Query data tab
		tabQuery = new JPanel();
		queryContents(tabQuery);
		dataPane.addTab("Query data", null, tabQuery, null);

		//Schema tab
		tabSchema = new JPanel();
		schemaContents(tabSchema);
		schemaPane.addTab("Schema", null, tabSchema, null);

		//Data pane and schema pane to split pane
		splitPane.setLeftComponent(dataPane);
		splitPane.setRightComponent(schemaPane);
		splitPane.setDividerLocation(600);
		add(splitPane); //add GUI to pane

	}


	public static JPanel loadContents(final JPanel tabLoad) {

		//initialize grid layout
		gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{18, 79, 124, 0};
		gbl_panel.rowHeights = new int[]{0, 36, 34, 30, 78, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		tabLoad.setLayout(gbl_panel);

		//FILE PATH text area
		final JTextArea filePathTextArea = new JTextArea();
		filePathTextArea.setEditable(false);
		filePathTextArea.setForeground(Color.LIGHT_GRAY);
		filePathTextArea.setText(" Select data file to upload");

		//file path grid format on tab
		GridBagConstraints gbc_filePath = new GridBagConstraints();
		gbc_filePath.fill = GridBagConstraints.HORIZONTAL;
		gbc_filePath.insets = new Insets(0, 0, 5, 5);
		gbc_filePath.gridx = 1;
		gbc_filePath.gridy = 1;
		filePathTextArea.setColumns(10);
		tabLoad.add(filePathTextArea, gbc_filePath);


		//BROWSE BUTTON, set file path, get File file
		JButton browseButton = new JButton("Browse");

		//browse button mouse listener
		browseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent getFile) {
				JFileChooser fileChooser = new JFileChooser();
				int val = fileChooser.showOpenDialog(fileChooser);
				if (val == JFileChooser.APPROVE_OPTION) {
					Struct.dataFile = fileChooser.getSelectedFile();
					filePathTextArea.setText(" " + Struct.dataFile.getAbsolutePath());
				}
			}
		});

		//browse button format on tab
		GridBagConstraints gbc_browse = new GridBagConstraints();
		gbc_browse.insets = new Insets(0, 0, 5, 0);
		gbc_browse.gridx = 2;
		gbc_browse.gridy = 1;
		tabLoad.add(browseButton, gbc_browse);


		//TABLE NAME text area
		final JTextArea tableNameTextArea = new JTextArea();
		tableNameTextArea.setForeground(Color.LIGHT_GRAY);
		final String instructions = " Select table name (optional)";
		tableNameTextArea.setText(instructions);

		//table name listener
		tableNameTextArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent getTable) {
				if (!begin_pressed) {
					tableNameTextArea.setText("");
					tableNameTextArea.setForeground(Color.BLACK);
				}
			}
		});

		//table name grid format on tab
		GridBagConstraints gbc_tableName = new GridBagConstraints();
		gbc_tableName.fill = GridBagConstraints.HORIZONTAL;
		gbc_tableName.insets = new Insets(0, 0, 5, 5);
		gbc_tableName.gridx = 1;
		gbc_tableName.gridy = 2;
		tableNameTextArea.setColumns(10);
		tabLoad.add(tableNameTextArea, gbc_tableName);


		//title row check box
		final JCheckBox titleRowCheckBox = new JCheckBox("Use first row for attribute names");
		titleRowCheckBox.setSelected(true);
		titleRowCheckBox.setForeground(Color.DARK_GRAY);

		//title row check box listener
		titleRowCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent useTopRow) {
				if (titleRowCheckBox.isSelected()) { titleRow = true; }
				if (!titleRowCheckBox.isSelected()) { titleRow = false; }
			}
		});

		//check box format on tab
		GridBagConstraints gbc_titleRow = new GridBagConstraints();
		gbc_titleRow.anchor = GridBagConstraints.WEST;
		gbc_titleRow.insets = new Insets(0, 0, 5, 5);
		gbc_titleRow.gridx = 1;
		gbc_titleRow.gridy = 3;
		tabLoad.add(titleRowCheckBox, gbc_titleRow);


		//LOADING PROGRESS progress bar
		final JProgressBar loadingProgress = new JProgressBar();
		loadingProgress.setIndeterminate(true);
		loadingProgress.setVisible(false);

		//loading progress format on tab
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 0, 5);
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 5;
		tabLoad.add(loadingProgress, gbc_progressBar);


		//BEGIN button
		JButton beginButton = new JButton("Begin");

		//begin button listener
		beginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				begin_pressed = true;
				if (!tableNameTextArea.getText().equals(instructions)) { 
					Struct.tableName = tableNameTextArea.getText(); 
				}
				tableNameTextArea.setText(" " + Struct.tableName);
				tableNameTextArea.setEditable(false);
				loadingProgress.setVisible(true);
				start();
			}

		});

		//begin button format on tab
		GridBagConstraints gbc_begin = new GridBagConstraints();
		gbc_begin.anchor = GridBagConstraints.SOUTH;
		gbc_begin.insets = new Insets(0, 0, 5, 5);
		gbc_begin.gridx = 1;
		gbc_begin.gridy = 4;
		tabLoad.add(beginButton, gbc_begin);

		return tabLoad;
	}


	public static JPanel queryContents(final JPanel tabQuery) {

		//initialize grid layout
		gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{71, 0};
		gbl_panel.rowHeights = new int[]{0, 380, 37, 130, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		tabQuery.setLayout(gbl_panel);


		//QUERY INPUT text area
		queryInput = new JTextArea();
		final String instructions = "Enter SQL query";
		queryInput.setText(instructions);
		queryInput.setEditable(false);
		queryInput.setEnabled(false);
		queryInput.setForeground(Color.LIGHT_GRAY);

		//query input mouse listener
		queryInput.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent clear) {
				if (user_typing == false) { //only clears first time clicked
					queryInput.setEditable(true);
					queryInput.setEnabled(true);
					queryInput.setText("");
					queryInput.setForeground(Color.BLACK);
					user_typing = true;
				}
			}
		});

		//query input format on scroll pane
		JScrollPane queryInputScrollPane = new JScrollPane();
		queryInputScrollPane.setEnabled(true);
		queryInputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		queryInputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		queryInputScrollPane.setViewportView(queryInput);


		//QUERY OUTPUT jtable
		queryOutput = new JTable();

		//query output format on scroll pane
		JScrollPane queryOutputScrollPane = new JScrollPane();
		queryOutputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		queryOutputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		queryOutputScrollPane.setViewportView(queryOutput);


		//QUERY FUNCTIONS FORMAT ON VERTICAL SPLIT PANE
		querySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryInputScrollPane, queryOutputScrollPane);
		querySplitPane.setContinuousLayout(true);
		querySplitPane.setTopComponent(queryInputScrollPane);
		querySplitPane.setBottomComponent(queryOutputScrollPane);
		querySplitPane.setResizeWeight(0.3);

		//query functions format on tab
		GridBagConstraints gbc_queryFunctions = new GridBagConstraints();
		gbc_queryFunctions.insets = new Insets(0, 0, 5, 0);
		gbc_queryFunctions.fill = GridBagConstraints.BOTH;
		gbc_queryFunctions.gridx = 0;
		gbc_queryFunctions.gridy = 1;
		tabQuery.add(querySplitPane, gbc_queryFunctions);


		//DB OUTPUT text area
		dbOutput = new JTextArea();
		dbOutput.setEditable(false);

		//db output format on scroll pane
		JScrollPane dbOutputScrollPane = new JScrollPane();
		dbOutputScrollPane.setViewportView(dbOutput);

		//db output format on tab
		GridBagConstraints gbc_dbOutput = new GridBagConstraints();
		gbc_dbOutput.fill = GridBagConstraints.BOTH;
		gbc_dbOutput.gridx = 0;
		gbc_dbOutput.gridy = 3;
		tabQuery.add(dbOutputScrollPane, gbc_dbOutput);


		//EXECUTE QUERY button
		JButton executeButton = new JButton("Execute");

		//execute button listener
		executeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent sendUserInput) {
				if (queryInput.getText() != instructions) { 
					user_typing = false; //user finished writing their query
					queryOutput.setFillsViewportHeight(true);
					queryOutput.setCellSelectionEnabled(true);
					queryOutput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					queryOutput.setShowGrid(true);
					queryOutput.setGridColor(Color.LIGHT_GRAY);
					String userQuery = queryInput.getText();
					try { QueryData.mainQuery(userQuery); } 
					catch (SQLException e) { e.printStackTrace(); }
				}
			}
		});

		//execute button format on pane
		GridBagConstraints gbc_execute = new GridBagConstraints();
		gbc_execute.anchor = GridBagConstraints.EAST;
		gbc_execute.insets = new Insets(0, 0, 5, 0);
		gbc_execute.gridx = 0;
		gbc_execute.gridy = 0;
		tabQuery.add(executeButton, gbc_execute);


		return tabQuery;
	} 


	public static JPanel schemaContents(final JPanel schemaPane) {

		//initialize grid layout
		gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 93, 0};
		gbl_panel.rowHeights = new int[]{0, 559, 47, 0, 0, 7, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		tabSchema.setLayout(gbl_panel);


		//SHOW SCHEMA jtable
		viewSchema = new JTable();
		viewSchema.setModel(new DefaultTableModel(new Object[][] {},new String[] {"Table", "Field", "Type"}));
		viewSchema.setFillsViewportHeight(true);
		viewSchema.setCellSelectionEnabled(true);
		viewSchema.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		viewSchema.setGridColor(Color.LIGHT_GRAY);

		//show schema format on scroll pane
		JScrollPane showSchemaScrollPane  = new JScrollPane();
		showSchemaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		showSchemaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		showSchemaScrollPane.setViewportView(viewSchema);

		//show shema format on tab
		GridBagConstraints gbc_schemaScrollPane = new GridBagConstraints();
		gbc_schemaScrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_schemaScrollPane.fill = GridBagConstraints.BOTH;
		gbc_schemaScrollPane.gridx = 1;
		gbc_schemaScrollPane.gridy = 1;
		tabSchema.add(showSchemaScrollPane, gbc_schemaScrollPane);


		//ERROR MESSAGE jtextpane
		final JTextPane errorMessageTextPane = new JTextPane();
		errorMessageTextPane.setVisible(false);
		errorMessageTextPane.setBackground(UIManager.getColor("TabbedPane.background"));
		errorMessageTextPane.setForeground(Color.DARK_GRAY);
		errorMessageTextPane.setText("There is an error in your schema. Please make sure the titles and data types match.");

		//error message format on tab
		GridBagConstraints gbc_errorMessage = new GridBagConstraints();
		gbc_errorMessage.insets = new Insets(0, 0, 5, 0);
		gbc_errorMessage.fill = GridBagConstraints.BOTH;
		gbc_errorMessage.gridx = 1;
		gbc_errorMessage.gridy = 2;
		tabSchema.add(errorMessageTextPane, gbc_errorMessage);

		//ERROR BUTTON jbutton
		final JButton errorFixedButton = new JButton("Error fixed");
		errorFixedButton.setVisible(false);
		errorFixedButton.setEnabled(false);

		//error button format on tab
		GridBagConstraints gbc_errorFixed = new GridBagConstraints();
		gbc_errorFixed.insets = new Insets(0, 0, 5, 0);
		gbc_errorFixed.gridx = 1;
		gbc_errorFixed.gridy = 3;
		tabSchema.add(errorFixedButton, gbc_errorFixed);


		//CHANGE SCHEMA button
		JButton submitSchemaButton = new JButton("Submit changes");


		//change schema submit changes to db
		submitSchemaButton.addMouseListener(new MouseAdapter() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//wont work if user has cell selected
				Vector changedTitles = new Vector();
				Vector changedTypes = new Vector();
				for (int r = 1; r < viewSchema.getRowCount(); r++) { changedTitles.addElement(viewSchema.getValueAt(r, 1)); }
				for (int r = 1; r < viewSchema.getRowCount(); r++) { changedTypes.addElement(viewSchema.getValueAt(r, 2)); }
				if (changedTitles.size() != changedTypes.size()) {
					errorFixedButton.setVisible(true);
					errorFixedButton.setEnabled(true);
					errorMessageTextPane.setVisible(true);
				}
				try {
					ChangeSchema.mainSchema(changedTitles, changedTypes);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		//change schema button format on tab
		GridBagConstraints gbc_submitSchema = new GridBagConstraints();
		gbc_submitSchema.insets = new Insets(0, 0, 5, 0);
		gbc_submitSchema.gridx = 1;
		gbc_submitSchema.gridy = 4;
		tabSchema.add(submitSchemaButton, gbc_submitSchema);


		return schemaPane;

	}


	static void createAndShowGUI() {
		JFrame frame = new JFrame("Dynamic Database Simulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new GUI(), BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);

	}


	public static void start() {

		try { Parser.mainParser(); } 
		catch (FileNotFoundException e1) { e1.printStackTrace(); }
		catch (InterruptedException e1) { e1.printStackTrace(); } 
		catch (InvocationTargetException e1) { e1.printStackTrace(); }

		try { LoadFile.mainLoader(); } 
		catch (SQLException e1) { e1.printStackTrace(); }

		viewSchema.setModel(TableForm.schemaTable());

	}


	public static void main(String args[]) throws InterruptedException, InvocationTargetException {
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				createAndShowGUI();
				splitPane.setDividerLocation(600);
			}
		});
	}


}
