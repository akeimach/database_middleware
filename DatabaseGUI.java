import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
 
public class DatabaseGUI extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	public static JTextArea filePath = new JTextArea();
	public static JTextArea fileContents = new JTextArea();
	public static ArrayList<Object> defaultVals = new ArrayList<Object>(); 
	
	public static int maxCols = 0;
	//public static String[] defaultVals = null;
	
	static File path;
	private JPanel tabLoad;
	//private JPanel tabQuery;
	
    public DatabaseGUI() {
    	
        super(new GridLayout(1, 1));
         
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabLoad = new JPanel();
        tabLoad.setLayout(null);
        tabbedPane.addTab("Load file", null, tabLoad, null);
        tabLoad.setPreferredSize(new Dimension(600, 400));
        tabLoadContents(tabLoad);
        
        
        
        JPanel tabQuery = new JPanel();
        tabQuery.setLayout(null);
        tabbedPane.addTab("Query data", null, tabQuery, null);
        tabQuery.setPreferredSize(new Dimension(600, 400));
        tabQueryContents(tabQuery);
        
        //Add tabbed pane to panel
        add(tabbedPane); //extends
        //enable scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
           
    }
     
    public static JPanel tabLoadContents(JPanel tabLoad) {
    		
    	
    	filePath.setEditable(false);
    	filePath.setForeground(Color.LIGHT_GRAY);
    	filePath.setText(" Select data file to upload");
    	filePath.setBounds(24, 11, 382, 16);
        tabLoad.add(filePath);
        
        JScrollPane scrollInput = new JScrollPane();
        scrollInput.setBounds(24, 48, 535, 56);
        scrollInput.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tabLoad.add(scrollInput);
        scrollInput.setViewportView(fileContents);
        
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
        				DetermineSchema.getSchema(path.getAbsolutePath()); 
        				DetermineSchema.guessTitles(defaultVals);
        			} 
        			catch (IOException e) { e.printStackTrace(); }			
        		}
        		
        	}
        });
        btnBrowse.setBounds(431, 6, 128, 29);
        tabLoad.add(btnBrowse);
        
        JButton btnNext = new JButton("Accept schema");
        btnNext.setBounds(431, 314, 128, 29);
        tabLoad.add(btnNext);
        
        JButton btnReparse = new JButton("Re-parse");
        btnReparse.setBounds(34, 314, 128, 29);
        tabLoad.add(btnReparse);
              
        
        
        
        
		return tabLoad;
		
    }
    
	public static JPanel tabQueryContents(JPanel tabQuery) {
    	return tabQuery;
    }  
     
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = DatabaseGUI.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
     
    private static void createAndShowGUI() {
    	
        //Create and set up the window.
        JFrame frame = new JFrame("Dynamic Database Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
        //Add content to the window
        frame.getContentPane().add(new DatabaseGUI(), BorderLayout.CENTER);
         
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
     
    public static void main(String[] args) {
    	
        //create and show this application's GUI
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
            	UIManager.put("swing.boldMetal", Boolean.FALSE);
            	createAndShowGUI();
            }
        });
    }
}

