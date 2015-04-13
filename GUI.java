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
					LoadData.file = fileChooser.getSelectedFile();
					path.setText(" " + LoadData.file.getAbsolutePath());
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
				if (getTableName.getText().equals(instructions)) { LoadData.tableName = "defaultTable"; }
				else { LoadData.tableName = getTableName.getText(); }
				getTableName.setText(" " + LoadData.tableName);
				getTableName.setEditable(false);
				progressBar.setVisible(true);
				try { LoadData.initUpload(LoadData.file); }
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
