// Name: Sarvesh Sadhoo
// UTA ID: 1000980763
// Final2

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.TextField;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class File1 extends javax.swing.JFrame {

    JButton exit =null;
    JTextArea textArea=null;
    
    // Defining and Instantiating all the buttons used for the UI
    JButton election_button=null; // Election Button 
    JButton down_button=null; // Crash Button
    JButton resatrt_button=null; // Restart Process Button
    JTextField textfield=null; // Message Display Area
    
    // Variables Declaration for all the process variables used in this file. 
    private Thread[] process = new Thread[6];
    private File2 procc1 = new File2(1, 15000, this);
    private File2 procc2 = new File2(2, 10000, this);
    private File2 procc3 = new File2(3, 15000, this);
    private File2 procc4 = new File2(4, 13000, this);
    private File2 procc5 = new File2(5, 5000, this);
    public File1() {
        
    	exit = new JButton();
    	textArea = new JTextArea();
    	
    	// Creating and Labeling the buttons
    	election_button  = new JButton(); 
    	election_button.setText("Election");
    	down_button = new JButton();
    	down_button.setText("Down");
    	resatrt_button = new JButton();
    	resatrt_button.setText("Restart");
    	textfield = new JTextField(20);
    	
    	exit.setText("Exit Application");
    	
    	// Window Pane Definition
    	JScrollPane scroll_panel = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	add(scroll_panel,"Center");
        JPanel up = new JPanel( new FlowLayout());
        up.add(exit);
        add(up,"South");
        
        exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.exit(0);
            }
        });
        
        //Listener For election_button
        election_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                 procc1.executeElection();
            }
        });
        
        
        // Listener for down_button
        down_button.addActionListener(new java.awt.event.ActionListener() {
            @SuppressWarnings("deprecation")
			public void actionPerformed(java.awt.event.ActionEvent evt) {

	    		
	    		String name =(String)textfield.getText().toUpperCase();
	    		if(name.equals("P1"))
	    		{
	    			try {
						procc1.failProcess();
					} catch (IOException e) {
					
						e.printStackTrace();
					}
	    			process[0].stop();
	                process[0] = null;
	    		}
	    		
	    		else if(name.equals("P2"))
	    		{
	    			try {
						procc2.failProcess();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			process[1].stop();
	    			process[1] = null;
	    		}
	    		
	    		
	    		else if(name.equals("P3"))
	    		{
	    			try {
						procc3.failProcess();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                process[2].stop();
	                process[2] = null;
	    		}
	    		
	    		
	    		else if(name.equals("P4"))
	    		{
	    			try {
						procc4.failProcess();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                process[3].stop();
	                process[3] = null;
	    		}
	    		
	    		else if(name.equals("P5"))
	    		{
	    			try {
						procc5.failProcess();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			process[4].stop();
	                process[4] = null;
	    		}
	    		else{
	    			textArea.append("crashed");
	    		}
	    	
            }
        });
        
        
        // Listener for Restart Button
        resatrt_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {

	    		
            	String name =(String)textfield.getText().toUpperCase();
	    		if(name.equals("P1"))
	    		{
	    			process[0] = new Thread(procc1);
    		        process[0].start();
    		        setTextArea(1, "Process 1 Started Again");
    		        procc1.executeElection();
	    		}
	    		
	    		else if(name.equals("P2"))
	    		{
	    		        process[1] = new Thread(procc2);
	    		        process[1].start();
	    		        setTextArea(2, "Process 2 Started Again");
	    		        procc2.executeElection();
	    		}
	    		
	    		
	    		else if(name.equals("P3"))
	    		{
	    		        process[2] = new Thread(procc3);
	    		        process[2].start();
	    		        setTextArea(3, "Process 3 Started Again");
	    		        procc3.executeElection();
	    		}
	    		
	    		
	    		else if(name.equals("P4"))
	    		{
	    			process[3] = new Thread(procc4);
		            process[3].start();
		            setTextArea(3, "Process 4 Start Aain");
		            procc4.executeElection();
	    		}
	    		
	    		else if(name.equals("P5"))
	    		{
		            process[4] = new Thread(procc5);
		            process[4].start();
		            setTextArea(5, "Process 5 Started Again");
		            procc5.executeElection();
	    		} 
	    		
	    	
            }
        });
        
        // Adding Buttons to the Window Pane
        JPanel elec1 = new JPanel( new FlowLayout());
        elec1.add(election_button);
        elec1.add(down_button);
        elec1.add(resatrt_button);
        elec1.add(textfield);
        //elec1.setSize(800, 800);
        add(elec1,"East");
        
        setSize(900,500);
        setVisible(true);
        pack();
        
        for(int i=0; i<5;i++) {
            process[i] = null;
        }
        
        process[0] = new Thread(procc1);
        process[0].start();
        
        process[1] = new Thread(procc2);
        process[1].start();
        
        
        process[2] = new Thread(procc3);
        process[2].start();
        
        
        process[3] = new Thread(procc4);
        process[3].start();
        
        
        process[4] = new Thread(procc5);
        process[4].start();
        
    }
    
    // Method for The Display Area
    public void setTextArea(int PID, String msg) {
    	textArea.append(msg+"\n");
    }
    
    // Main Method
    public static void main(String[] args) {
    	new File1();
    }

  
 }