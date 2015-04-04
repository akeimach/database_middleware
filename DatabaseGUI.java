import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
 
public class DatabaseGUI extends JPanel {
	private JPanel tabLoad_1;
	
    public DatabaseGUI() {
    	
        super(new GridLayout(1, 1));
         
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel tabLoad;
        tabLoad_1 = new JPanel();
        tabLoad_1.setLayout(null);
        tabbedPane.addTab("Load file", null, tabLoad_1, null);
        tabLoad_1.setPreferredSize(new Dimension(600, 400));
        tabLoad = tabLoadContents(tabLoad_1);
        
        
        JPanel tabQuery = new JPanel();
        tabQuery.setLayout(null);
        tabbedPane.addTab("Query data", null, tabQuery, null);
        tabQuery.setPreferredSize(new Dimension(600, 400));
        tabQuery = tabQueryContents(tabQuery);
        
        //Add tabbed pane to panel
        add(tabbedPane); //extends
        //enable scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
           
    }
     
    public static JPanel tabLoadContents(JPanel tabLoad) {
    	
    	
    	final JTextArea txtrSelectDataFile = new JTextArea();
        txtrSelectDataFile.setEditable(false);
        txtrSelectDataFile.setForeground(Color.LIGHT_GRAY);
        txtrSelectDataFile.setText(" Select data file to upload");
        txtrSelectDataFile.setBounds(24, 11, 382, 16);
        tabLoad.add(txtrSelectDataFile);
        
        final JTextArea fileContents = new JTextArea();
        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setRowHeaderView(fileContents);
        scrollPane.setBounds(24, 39, 535, 119);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tabLoad.add(scrollPane);
        
        JButton btnUpload = new JButton("Browse");
        btnUpload.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent uplaod) {
        		JFileChooser fileChooser = new JFileChooser();
        		int val = fileChooser.showOpenDialog(fileChooser);
        		if (val == JFileChooser.APPROVE_OPTION) {
        			File data = fileChooser.getSelectedFile();
        			txtrSelectDataFile.setText(data.getAbsolutePath());
					try {
						BufferedReader readData;
						readData = new BufferedReader(new FileReader(data));
						String line = readData.readLine();
						int init = 0; //first only take first rows to analyze
						while ((line != null) && (init < 30)) {
							System.out.println(line);
							fileContents.append(line + "\n");
							line = readData.readLine();
							
							++init;
						}
						readData.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
        		}
        	}
        });
        btnUpload.setBounds(442, 6, 117, 29);
        tabLoad.add(btnUpload);
        
        
        
        
        JButton btnNext = new JButton("Next >");
        btnNext.setBounds(471, 319, 88, 29);
        tabLoad.add(btnNext);
        
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
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
            	UIManager.put("swing.boldMetal", Boolean.FALSE);
            	createAndShowGUI();
            }
        });
    }
}

