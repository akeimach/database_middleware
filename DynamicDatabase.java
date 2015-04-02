import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class DynamicDatabase {

    PanelOne pload = new PanelOne();
    PanelTwo pverify = new PanelTwo();
    PanelThree pquery = new PanelThree();

    CardLayout layout = new CardLayout();
    JPanel cardPanel = new JPanel(layout);
    
    public DynamicDatabase() {
    	
        JButton showLoad = new JButton("Load");
        JButton showVerify = new JButton("Verify");
        JButton showQuery = new JButton("Query");
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(showLoad);
        buttonsPanel.add(showVerify);
        buttonsPanel.add(showQuery);
        
        showLoad.addActionListener(new ButtonListener());
        showVerify.addActionListener(new ButtonListener());
        showQuery.addActionListener(new ButtonListener());

        cardPanel.add(pload, "Load");
        pload.setLayout(null);
        
        final JTextArea txtrSelectDataFile = new JTextArea();
        txtrSelectDataFile.setEditable(false);
        txtrSelectDataFile.setForeground(Color.LIGHT_GRAY);
        txtrSelectDataFile.setText("Select data file to upload");
        txtrSelectDataFile.setBounds(34, 35, 382, 16);
        pload.add(txtrSelectDataFile);
        
        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(34, 68, 532, 271);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
		final JTextArea fileContents = new JTextArea();
		scrollPane.setViewportView(fileContents);
        pload.add(scrollPane);
        
        JButton btnBrowse = new JButton("Browse");
        btnBrowse.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent upload) {
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
							fileContents.append(line+"\n");
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
        btnBrowse.setBounds(449, 30, 117, 29);
        pload.add(btnBrowse);
        
        JLabel lblContinueIfThe = new JLabel("Continue if the data looks correct");
        lblContinueIfThe.setBounds(34, 351, 382, 16);
        pload.add(lblContinueIfThe);
        
        JButton btnNext = new JButton("Next >");
        btnNext.addActionListener(new ButtonListener());
        btnNext.setBounds(449, 346, 117, 29);
        pload.add(btnNext);
        
        cardPanel.add(pverify, "Verify");
        cardPanel.add(pquery, "Query");

        JFrame frame = new JFrame("Dynamic Database");
        frame.getContentPane().add(cardPanel);
        frame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if ("Load".equals(command)) {
                layout.show(cardPanel, "Load");
            } else if (("Verify".equals(command)) || ("Next >".equals(command))) {
                layout.show(cardPanel, "Verify");
            } else {
                layout.show(cardPanel, "Query");
            }
            
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DynamicDatabase testCardLayout = new DynamicDatabase();
            }
        });
    }
}

class PanelOne extends JPanel {
    public PanelOne() {
        add(new JLabel("Load Data"));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }
}

class PanelTwo extends JPanel {

    public PanelTwo() {
        //setBackground(Color.BLUE);
        add(new JLabel("Verify Data"));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }
}

class PanelThree extends JPanel {

    public PanelThree() {
        //setBackground(Color.YELLOW);
        add(new JLabel("Query Data"));
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }
}