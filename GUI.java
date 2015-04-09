import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

@SuppressWarnings("serial")
public class GUI extends JPanel {

	public static JPanel grid = new JPanel();
	public static JTextArea filePath = new JTextArea();
	public static JTextArea sampleTuple = new JTextArea(); 
	public static JScrollPane scrollOutput = new JScrollPane();
	static File path;
	private static JPanel tabLoad;
	private JPanel tabQuery;
	public static JPanel schemaTable = new JPanel();
	public static JTextField queryIn;

	public GUI() {
		
		setLayout(new BorderLayout(0, 0));
		JTabbedPane tabbedPane = new JTabbedPane();

		tabLoad = new JPanel();
		tabLoad.setLayout(null);
		tabbedPane.addTab("Load file", null, tabLoad, null);
		tabLoad.setPreferredSize(new Dimension(600, 400));
		tabLoadContents(tabLoad);

		tabQuery = new JPanel();
		tabQuery.setLayout(null);
		tabbedPane.addTab("Query data", null, tabQuery, null);
		tabQuery.setPreferredSize(new Dimension(600, 400));
		tabQueryContents(tabQuery);

		add(tabbedPane);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	public static JPanel tabLoadContents(JPanel tabLoad) {

		filePath.setEditable(false);
		filePath.setForeground(Color.LIGHT_GRAY);
		filePath.setText(" Select data file to upload");
		filePath.setBounds(40, 33, 431, 16);
		tabLoad.add(filePath);

		JScrollPane scrollInput = new JScrollPane();
		scrollInput.setBounds(24, 69, 230, 316);
		scrollInput.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollInput.setViewportView(sampleTuple);
		tabLoad.add(scrollInput);

		JButton btnNext = new JButton("Accept schema");
		btnNext.setBounds(535, 401, 128, 29);
		tabLoad.add(btnNext);

		JButton btnReparse = new JButton("Re-parse");
		btnReparse.setBounds(343, 401, 128, 29);
		tabLoad.add(btnReparse);

		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent uplaod) {
				JFileChooser fileChooser = new JFileChooser();
				int val = fileChooser.showOpenDialog(fileChooser);
				if (val == JFileChooser.APPROVE_OPTION) {
					path = fileChooser.getSelectedFile();
					filePath.setText(path.getAbsolutePath());	
					try { InferData.getSample(); }
					catch (IOException e) { e.printStackTrace(); } 
					catch (SQLException e) { e.printStackTrace(); }
				}
			}
		});
		btnBrowse.setBounds(497, 28, 128, 29);
		tabLoad.add(btnBrowse);

		grid.setBounds(299, 69, 331, 316);
		tabLoad.add(grid);
		grid.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
				new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));

		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"INT", "BIGINT", "FLOAT", "DOUBLE", "BIT", "CHAR", "VARCHAR", "TEXT", "DATE", "DATETIME", "TIME", "TIMESTAMP", "YEAR"}));
		comboBox.setToolTipText("");
		grid.add(comboBox, "6, 2, fill, default");

		return tabLoad;

	}

	public static JPanel tabQueryContents(JPanel tabQuery) {

		queryIn = new JTextField();
		queryIn.setForeground(Color.BLACK);
		queryIn.setText("Type query");
		queryIn.setBounds(19, 23, 500, 28);
		tabQuery.add(queryIn);
		queryIn.setColumns(10);

		JButton btnExecute = new JButton("Execute");
		btnExecute.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent sendQuery) {
				try { HandleData.sendQuery(queryIn.getText()); }
				catch (SQLException e) { e.printStackTrace(); }
			}
		});
		btnExecute.setBounds(534, 24, 117, 29);
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
