package ui;

import net.Connect;
import net.LoadData;
import net.Query;
import data.Parser;
import data.UserView;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Vector;


@SuppressWarnings("serial")
public class GUI extends JPanel {

	public static boolean user_typing = false;
	JTable viewSchema;
	public static JTable queryOutput;

	//format the GUI pane	
	public GUI() {


		//LEFT SIDE
		JTabbedPane dataPane = new JTabbedPane();
		dataPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		//Load file tab
		JPanel tabLoad = new JPanel();
		loadContents(tabLoad);
		dataPane.addTab("Load file", null, tabLoad, null);
		//Query data tab
		JPanel tabQuery = new JPanel();
		queryContents(tabQuery);
		dataPane.addTab("Query data", null, tabQuery, null);


		//RIGHT SIDE
		JTabbedPane schemaPane = new JTabbedPane();
		schemaPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		//Schema tab
		JPanel tabSchema = new JPanel();
		schemaContents(tabSchema);
		schemaPane.addTab("Schema", null, tabSchema, null);


		//Background pane holds tabs (Load data, schema, and query tabs)
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataPane, schemaPane);
		splitPane.setPreferredSize(new Dimension(1000, 700));
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(600);
		add(splitPane); //add GUI to pane

	}

	//format the grid layout
	GridBagLayout gridLayout(int[] columnWidths, int[] rowHeights, double[] columnWeights, double[] rowWeights) {
		GridBagLayout gridLayout = new GridBagLayout();
		gridLayout.columnWidths = columnWidths;
		gridLayout.rowHeights = rowHeights;
		gridLayout.columnWeights = columnWeights;
		gridLayout.rowWeights = rowWeights;
		return gridLayout;
	}

	//format the grid constraints
	GridBagConstraints gridConstraints(Insets insets, int fill, int gridx, int gridy) {
		GridBagConstraints gridLayout = new GridBagConstraints();
		if (insets != null) {
			gridLayout.insets = insets;
		}
		gridLayout.fill = fill;
		gridLayout.gridx = gridx;
		gridLayout.gridy = gridy;
		return gridLayout;
	}

	//format the text boxes
	JTextArea textBox(String instructions, int col) {
		JTextArea textBox = new JTextArea();
		textBox.setEditable(false);
		textBox.setForeground(Color.LIGHT_GRAY);
		textBox.setText(instructions);
		if (col > 0) {
			textBox.setColumns(col);
		}
		return textBox;
	}

	//format the scroll panes
	JScrollPane scrollLayout() {
		JScrollPane scrollLayout = new JScrollPane();
		scrollLayout.setEnabled(true);
		scrollLayout.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollLayout.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		return scrollLayout;
	}


	//format the data tab, set actions
	public JPanel loadContents(final JPanel tabLoad) {

		//Initialize grid layout
		GridBagLayout gbl_panel = gridLayout(new int[] {
			18, 79, 124, 0
		}, new int[] {
			0, 36, 34, 30, 78, 0, 0
		}, new double[] {
			0.0, 1.0, 0.0, Double.MIN_VALUE
		}, new double[] {
			0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
		});
		tabLoad.setLayout(gbl_panel);

		//FILE PATH TEXT BOX
		final JTextArea filePathTextArea = textBox("Select data file to upload", 10);
		GridBagConstraints gbc_filePath = gridConstraints(new Insets(0, 0, 5, 5), GridBagConstraints.HORIZONTAL, 1, 1);
		tabLoad.add(filePathTextArea, gbc_filePath);

		//BROWSE BUTTON
		JButton browseButton = new JButton("Browse");
		GridBagConstraints gbc_browse = gridConstraints(new Insets(0, 0, 5, 0), 0, 2, 1);
		tabLoad.add(browseButton, gbc_browse);
		browseButton.addMouseListener(new MouseAdapter() {@Override
			//bring up the dialog box to select data file
			public void mouseClicked(MouseEvent getFile) {
				JFileChooser fileChooser = new JFileChooser();
				int val = fileChooser.showOpenDialog(fileChooser);
				if (val == JFileChooser.APPROVE_OPTION) {
					Connect.dataFile = fileChooser.getSelectedFile();
					//set the text box to show file path
					filePathTextArea.setText(Connect.dataFile.getAbsolutePath());
				}
			}
		});

		//TABLE NAME TEXT BOX
		final JTextArea tableNameTextArea = textBox("Select table name (optional)", 10);
		GridBagConstraints gbc_tableName = gridConstraints(new Insets(0, 0, 5, 5), GridBagConstraints.HORIZONTAL, 1, 2);
		tabLoad.add(tableNameTextArea, gbc_tableName);
		tableNameTextArea.addMouseListener(new MouseAdapter() {@Override
			public void mousePressed(MouseEvent getTable) {
				tableNameTextArea.setEditable(true);
				tableNameTextArea.setText("");
				tableNameTextArea.setForeground(Color.BLACK);
			}
		});

		//CHECK BOX COLUMN TITLES
		final JCheckBox titleRowCheckBox = new JCheckBox("Use first row for attribute names");
		titleRowCheckBox.setSelected(true);
		titleRowCheckBox.setForeground(Color.DARK_GRAY);
		GridBagConstraints gbc_titleRow = gridConstraints(new Insets(0, 0, 5, 5), 0, 1, 3);
		gbc_titleRow.anchor = GridBagConstraints.WEST;
		tabLoad.add(titleRowCheckBox, gbc_titleRow);
		titleRowCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent useTopRow) {
				if (!titleRowCheckBox.isSelected()) {
					Parser.titlesIncluded = false;
				}
			}
		});

		//LOADING PROGRESS BAR
		final JProgressBar loadingProgress = new JProgressBar();
		loadingProgress.setIndeterminate(true);
		loadingProgress.setVisible(false);
		GridBagConstraints gbc_progressBar = gridConstraints(new Insets(0, 0, 0, 5), 0, 1, 5);
		tabLoad.add(loadingProgress, gbc_progressBar);

		//BEGIN BUTTON
		JButton beginButton = new JButton("Begin");
		GridBagConstraints gbc_begin = gridConstraints(new Insets(0, 0, 5, 5), 0, 1, 4);
		gbc_begin.anchor = GridBagConstraints.SOUTH;
		tabLoad.add(beginButton, gbc_begin);
		beginButton.addActionListener(new ActionListener() {@Override
			public void actionPerformed(ActionEvent e) {
				if (tableNameTextArea.isEditable()) {
					Connect.tableName = tableNameTextArea.getText();
				}
				tableNameTextArea.setText(Connect.tableName);
				tableNameTextArea.setEditable(false);
				loadingProgress.setVisible(true);
				try {
					Parser.parseData(Connect.dataFile);
					LoadData.startBulkLoad();
					viewSchema.setModel(TableForm.schemaTable());
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				} catch (InvocationTargetException e3) {
					e3.printStackTrace();
				} catch (SQLException e4) {
					e4.printStackTrace();
				}
			}
		});

		return tabLoad;
	}

	public JPanel queryContents(final JPanel tabQuery) {

		//INIT GRID LAYOUT
		GridBagLayout gbl_panel = gridLayout(new int[] {
			71, 0
		}, new int[] {
			0, 380, 37, 130, 0
		}, new double[] {
			1.0, Double.MIN_VALUE
		}, new double[] {
			0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE
		});
		tabQuery.setLayout(gbl_panel);

		//QUERY INPUT TEXT BOX
		final String instructions = "Enter SQL query";
		final JTextArea queryInput = textBox(instructions, 0);
		queryInput.setEnabled(false);
		JScrollPane inputPane = scrollLayout();
		inputPane.setViewportView(queryInput);
		queryInput.addMouseListener(new MouseAdapter() {@Override
			public void mouseReleased(MouseEvent clear) {
				if (!user_typing) { //only clears first time clicked
					queryInput.setEditable(true);
					queryInput.setEnabled(true);
					queryInput.setText("");
					queryInput.setForeground(Color.BLACK);
					user_typing = true;
				}
			}
		});

		//QUERY OUTPUT TABLE
		queryOutput = new JTable();
		JScrollPane outputPane = scrollLayout();
		outputPane.setViewportView(queryOutput);

		//SPLIT PANE
		JSplitPane querySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPane, outputPane);
		querySplitPane.setContinuousLayout(true);
		querySplitPane.setTopComponent(inputPane);
		querySplitPane.setBottomComponent(outputPane);
		querySplitPane.setResizeWeight(0.3);
		GridBagConstraints gbc_queryFunctions = gridConstraints(new Insets(0, 0, 5, 0), GridBagConstraints.BOTH, 0, 1);
		tabQuery.add(querySplitPane, gbc_queryFunctions);

		//DB OUTPUT TEXT BOX
		final JTextArea dbOutput = new JTextArea();
		dbOutput.setEditable(false);
		JScrollPane dbOutputScrollPane = new JScrollPane();
		dbOutputScrollPane.setViewportView(dbOutput);
		GridBagConstraints gbc_dbOutput = gridConstraints(null, GridBagConstraints.BOTH, 0, 3);
		tabQuery.add(dbOutputScrollPane, gbc_dbOutput);

		//EXECUTE QUERY BUTTON
		JButton executeButton = new JButton("Execute");
		GridBagConstraints gbc_execute = gridConstraints(new Insets(0, 0, 5, 0), 0, 0, 0);
		gbc_execute.anchor = GridBagConstraints.EAST;
		tabQuery.add(executeButton, gbc_execute);
		executeButton.addMouseListener(new MouseAdapter() {@Override
			public void mouseClicked(MouseEvent sendUserInput) {
				if (queryInput.getText() != instructions) {
					user_typing = false; //user finished writing their query
					queryOutput.setFillsViewportHeight(true);
					queryOutput.setCellSelectionEnabled(true);
					queryOutput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					queryOutput.setShowGrid(true);
					queryOutput.setGridColor(Color.LIGHT_GRAY);
					try {
						Query.mainQuery(queryInput.getText(), dbOutput);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});

		return tabQuery;
	}


	public JPanel schemaContents(final JPanel tabSchema) {

		//initialize grid layout
		GridBagLayout gbl_panel = gridLayout(new int[] {
			0, 93, 0
		}, new int[] {
			0, 559, 31, 0, 0, 7, 0
		}, new double[] {
			0.0, 1.0, Double.MIN_VALUE
		}, new double[] {
			0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
		});
		tabSchema.setLayout(gbl_panel);

		//SHOW SCHEMA jtable
		viewSchema = new JTable();
		viewSchema.setModel(new DefaultTableModel(new Object[][] {}, new String[] {
			"Table", "Field", "Type"
		}));
		viewSchema.setFillsViewportHeight(true);
		viewSchema.setCellSelectionEnabled(true);
		viewSchema.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		viewSchema.setGridColor(Color.LIGHT_GRAY);
		JScrollPane showSchemaScrollPane = scrollLayout();
		showSchemaScrollPane.setViewportView(viewSchema);
		GridBagConstraints gbc_schemaScrollPane = gridConstraints(new Insets(0, 0, 5, 0), GridBagConstraints.BOTH, 1, 1);
		tabSchema.add(showSchemaScrollPane, gbc_schemaScrollPane);

		//ADD COLUMN BUTTON
		JButton btnAddColumn = new JButton("Add column");
		GridBagConstraints gbc_btnAddColumn = gridConstraints(new Insets(0, 0, 5, 0), GridBagConstraints.WEST, 1, 2);
		tabSchema.add(btnAddColumn, gbc_btnAddColumn);
		btnAddColumn.addMouseListener(new MouseAdapter() {@Override
			public void mouseClicked(MouseEvent arg0) {
				UserView.tableSize++;
				viewSchema.setModel(TableForm.schemaTable());
			}
		});

		//CHANGE SCHEMA BUTTON
		JButton submitSchemaButton = new JButton("Submit changes");
		GridBagConstraints gbc_submitSchema = gridConstraints(new Insets(0, 0, 5, 0), 0, 1, 4);
		tabSchema.add(submitSchemaButton, gbc_submitSchema);
		submitSchemaButton.addMouseListener(new MouseAdapter() {@SuppressWarnings({
				"rawtypes", "unchecked"
			})@Override
			public void mouseClicked(MouseEvent arg0) {
				//wont work if user has cell selected
				Vector changedTitles = new Vector();
				Vector changedTypes = new Vector();
				for (int r = 1; r < viewSchema.getRowCount(); r++) {
					changedTitles.addElement(viewSchema.getValueAt(r, 1));
				}
				for (int r = 1; r < viewSchema.getRowCount(); r++) {
					changedTypes.addElement(viewSchema.getValueAt(r, 2));
				}
				try {
					net.ChangeSchema.UserChange.mainUserChange(changedTitles, changedTypes);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		return tabSchema;
	}


	//main create and show GUI
	public static void main(String args[]) throws InterruptedException, InvocationTargetException {
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("Dynamic Database Simulator");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().add(new GUI(), BorderLayout.CENTER);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}


}