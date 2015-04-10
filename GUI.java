import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

@SuppressWarnings("serial")
public class GUI extends JPanel {
	public static JTextArea filePath = new JTextArea();
	public static JScrollPane scrollOutput = new JScrollPane();
	static File path;
	public static JPanel tabLoad;
	public static JPanel tabQuery;
	public static JPanel schemaTable = new JPanel();
	public static JTextField queryIn;
	public static FormLayout form = new FormLayout();
	public static JPanel grid;
	public static JCheckBox checkPrimary;
	public static JTextField textFieldTypes;
	public static JTextField textFieldVals;
	public static JComboBox dataTypes;
	public static String[] typeOptions = {"INT", "BIGINT", "FLOAT", "DOUBLE", "BIT", 
		"CHAR", "VARCHAR", "TEXT", "DATE", "DATETIME", "TIME", "TIMESTAMP", "YEAR"};

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

	public static JPanel schemaContents(final JPanel tabLoad, int depth, String[] schemaVals, String[] sampleVals, String[] dropTypes) {
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(35, 72, 521, 220);
		tabLoad.add(scrollPane);
		grid = new JPanel();
		scrollPane.setViewportView(grid);
		final FormLayout table = new FormLayout(
			new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow")
			},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC
			}
		);

		grid.setLayout(table);
		JLabel lblPrimary = new JLabel("Primary");
		grid.add(lblPrimary, "2, 2, center, default");
		JLabel lblAttributeName = new JLabel("Attribute name");
		grid.add(lblAttributeName, "4, 2, center, default");
		JLabel lblSampleValue = new JLabel("Sample value");
		grid.add(lblSampleValue, "6, 2, center, default");
		JLabel lblDataType = new JLabel("Data type");
		grid.add(lblDataType, "8, 2, center, default");
		int col = 0;
		for (int i = 4; i <= ((depth + 1) * 2); i += 2) {
			table.appendRow(FormFactory.RELATED_GAP_ROWSPEC);
			table.appendRow(FormFactory.DEFAULT_ROWSPEC);
			checkPrimary = new JCheckBox("");
			grid.add(checkPrimary, "2, " + i + ", center, default");
			textFieldVals = new JTextField();
			textFieldVals.setText(schemaVals[col]);
			grid.add(textFieldVals, "4, " + i + ", center, default");
			textFieldVals.setColumns(10);
			textFieldTypes = new JTextField();
			textFieldTypes.setText(sampleVals[col]);
			grid.add(textFieldTypes, "6, " + i + ", center, default");
			textFieldTypes.setColumns(10);
			int topType = 0;
			for (String type : typeOptions) {
				if (type == dropTypes[col]) { topType = col; }
			}
			dataTypes = new JComboBox();
			dataTypes.setModel(new DefaultComboBoxModel(typeOptions));
			dataTypes.setToolTipText("");
			dataTypes.setSelectedIndex(topType);
			grid.add(dataTypes, "8, " + i + ", center, default");
			col++;
		}
		return tabLoad;
	}

	public static JPanel tabLoadContents(final JPanel tabLoad) {
		filePath.setEditable(false);
		filePath.setForeground(Color.LIGHT_GRAY);
		filePath.setText(" Select data file to upload");
		filePath.setBounds(32, 20, 374, 16);
		tabLoad.add(filePath);
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
		btnBrowse.setBounds(428, 15, 128, 29);
		tabLoad.add(btnBrowse);
		return tabLoad;
	}

	public static JPanel tabQueryContents(JPanel tabQuery) {
		queryIn = new JTextField();
		queryIn.setForeground(Color.BLACK);
		queryIn.setBounds(19, 23, 384, 28);
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
		btnExecute.setBounds(427, 24, 117, 29);
		tabQuery.add(btnExecute);
		return tabQuery;
	}  

	public static JPanel receiveQuery(final JPanel tabQuery, ResultSet rs) throws SQLException {
		JTextPane resultOutput = new JTextPane();
		resultOutput.setBounds(28, 76, 516, 236);
		while(rs.next()){
			System.out.println(rs.getString("a0") + "\t" + rs.getString("a1"));
		}
		tabQuery.add(resultOutput);
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
