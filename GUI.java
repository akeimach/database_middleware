
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
 
public class GUI extends JPanel {
	
	private static final long serialVersionUID = 1L;
	public static JPanel grid = new JPanel();
	public static JTextArea filePath = new JTextArea();
	public static JTextArea sampleTuple = new JTextArea(); 
	public static int maxCols = 0;
	public static ArrayList<String> defaultVals = new ArrayList<String>(); 
	public static ArrayList<String> defaultNames = new ArrayList<String>(); 
	public static String[] finalVals; // = new String[maxCols];
	public static String[] finalNames; // = new String[maxCols];
	public static boolean tableReady = false;
	public static JScrollPane scrollOutput = new JScrollPane();
	static File path;
	private static JPanel tabLoad;
	private JPanel tabQuery;
	public static JPanel schemaTable = new JPanel();
	
    public GUI() {
    	
        setLayout(new BorderLayout(0, 0));
         
        JTabbedPane tabbedPane = new JTabbedPane();
       // tabbedPane.setPreferredSize(new Dimension(214748364, 214748364));
        
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
        
        add(tabbedPane); //extends
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
        			try { 
        				Schema.getSchema(path.getAbsolutePath()); 
        				//Schema.determineDtype(defaultVals);
        			} 
        			catch (IOException e) { e.printStackTrace(); }			
        		}
        		
        	}
        });
        btnBrowse.setBounds(497, 28, 128, 29);
        tabLoad.add(btnBrowse);
        
        //FormLayout formLayout = new FormLayout();
        //grid.setLayout(formLayout);
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
        
       
       Object[] headers = new String[defaultVals.size()];
       //ArrayList<Object>();
       
        for (int i = 0; i < maxCols; i++) {
        	JTextArea title = new JTextArea();
        	title.setText((String) headers[i]);
        	grid.add(title, "4, 2, fill, default");
        }
        
        JComboBox comboBox = new JComboBox();
        comboBox.setModel(new DefaultComboBoxModel(new String[] {"INT", "BIGINT", "FLOAT", "DOUBLE", "BIT", "CHAR", "VARCHAR", "TEXT", "DATE", "DATETIME", "TIME", "TIMESTAMP", "YEAR"}));
        comboBox.setToolTipText("");
        grid.add(comboBox, "6, 2, fill, default");
        
		return tabLoad;
		
    }
    
	public static JPanel tabQueryContents(JPanel tabQuery) {
		
		
		
		
		
		
		
		
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
          //if (tableReady) { Connect.runDB(); }
    }
}
