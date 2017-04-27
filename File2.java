// Name: Sarvesh Sadhoo
// UTA ID: 1000980763
// Final2

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;
 
public class File2 implements Runnable{

    private static final Socket MyClient = null;
	private int process_ID; // Process Number to Identify Process
    private int port; // Port ID on which the process will listen
    private long timeout; // Timeout before Election Happens
    private File1 controllerFile = null;
    private int start_port = 1000;
    
    
    
    public File2(int process_ID, long timeout, File1 controllerFile) {
        this.process_ID = process_ID;
        this.port = start_port+process_ID;
        this.timeout = timeout;
        this.controllerFile = controllerFile;
    }
    
    private ServerSocket srv_soc = null; // Socket that will listen to the incoming connection
    private boolean leader_flag = false;
    private Timer processOnTimer = null;
    private Thread timer = null;
    private String host = "localhost"; // Local Host on which the program runs
    
    // Method To Run The Thread
    public void run() {
        try {
            this.srv_soc = new ServerSocket(port);
            // Mentions the Port Number of all the Process Running
            controllerFile.setTextArea(process_ID, "Process"+process_ID+" Running On Port: "+port);
            while(true) {
                parser(srv_soc.accept()); // Accept Incoming Connections
            }
        } catch (IOException|InterruptedException ex) {
            //Logger.getLogger(ProcessThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Testing Code----------------------------------------------------
    public void test(String str){
    	DataOutputStream output;
        try {
           output = new DataOutputStream(MyClient.getOutputStream());
        }
        catch (IOException e) {
           System.out.println(e);
        }
    }
    
    // Election Method Starts the Election between the process
    public void election(StringTokenizer stringTokenizer, String token_value){
    	  try {
              if(Integer.parseInt(stringTokenizer.nextToken()) == process_ID) {
                  int[] participants = new int[6];
                  participants[0] = process_ID;
                  int counter = 1;
                  while(stringTokenizer.hasMoreTokens()) {
                      participants[counter] = Integer.parseInt(stringTokenizer.nextToken());
                      System.out.println();
                      counter++;
                  }
                  checkResult(participants);
              }
              else {
                  successorPass(token_value+" "+process_ID, process_ID + 1);
              }
          } catch(Exception ex) {
              ex.printStackTrace();
          }
    }
    
    // This Method Manages the Coordinator
    public void cordinator(String token_value, StringTokenizer stringTokenizer){
        controllerFile.setTextArea(process_ID, "Input Message: "+ token_value);
        try {
            if(Integer.parseInt(stringTokenizer.nextToken()) == process_ID) {
                leader_flag = true;
                beginCoordinator();
            }
            else {
            	// Changes the Coordinator when Process 5 Runs
                if(leader_flag) {
                    controllerFile.setTextArea(process_ID, process_ID+" Not A Coordinator Now");
                    leader_flag = false;
                    endCoordinator();
                }
                successorPass(token_value, process_ID+1);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } 
    }
    
    // StringTokenizer breaks the String In Tokens
    public void on(StringTokenizer stringTokenizer){
    	   try {
               if(!((Integer.parseInt(stringTokenizer.nextToken()) > process_ID))) {
                   sendAliveProcess();
                   startTimer();
               }
           } catch(Exception ex) {
               ex.printStackTrace();
           } }
    
    //----------------------------------------------------------------------insert------------------------
    
    // Parser Method Parsing the Election Data Message and Coordinator Message
    private void parser(Socket socket) throws InterruptedException, IOException {
        BufferedReader buff_read = null;
        try {
            stopTimer();
            Thread.sleep(900);
            buff_read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String token_value = buff_read.readLine();
            buff_read.close();
            socket.close();
            
            StringTokenizer stringTokenizer = new StringTokenizer(token_value);
            String test= stringTokenizer.nextToken();
          
            if(test.equals("ElectionMessage:")){
            	controllerFile.setTextArea(process_ID, "Input Message: "+token_value);
            	election(stringTokenizer, token_value);
                    
                    }
                 
                 if(test.equals("CoordinatorMessage:")){
                    controllerFile.setTextArea(process_ID, "Input Message: "+ token_value);
                    try {
                        if(Integer.parseInt(stringTokenizer.nextToken()) == process_ID) {
                            leader_flag = true;
                            beginCoordinator();
                        }
                        else {
                            if(leader_flag) {
                                controllerFile.setTextArea(process_ID, "Not coordinator anymore");
                                leader_flag = false;
                                endCoordinator();
                            }
                            successorPass(token_value, process_ID+1);
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    } }
                
                    if(test.equals("Process_On")){
                    	on(stringTokenizer);
                    }
            }
        finally{}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
        } 
    
    //-----------------------------------------------------------------------end--------------------------
    
    //Testing Code-----------------------------------------------
    public void listenSocket(){
    	//Create socket connection
    	   try{
    	     Socket socket = new Socket("kq6py", 4321);
    	     PrintWriter out = new PrintWriter(socket.getOutputStream(), 
    	                 true);
    	     BufferedReader in = new BufferedReader(new InputStreamReader(
    	                socket.getInputStream()));
    	   } catch (UnknownHostException e) {
    	     System.out.println("Unknown host: kq6py");
    	     System.exit(1);
    	   } catch  (IOException e) {
    	     System.out.println("No I/O");
    	     System.exit(1);
    	   }
    	}
    
    // failProcess Method crashes the process
    public void failProcess() throws IOException {
        stopTimer();
 
            srv_soc.close();
            if(leader_flag) {
                endCoordinator();
            }

        controllerFile.setTextArea(process_ID, "Process " + process_ID +" Has Crashed");
        controllerFile.setTextArea(process_ID, "Starting Election Algorithm");    
    }
    
    // The Following Method Starts the Election
    public void executeElection() {
        successorPass("ElectionMessage:  "+process_ID, process_ID+1);
    }
    
    // To Check if the Process is Alive
    public void sendAliveProcess() {
        successorPass("Process_On "+process_ID, process_ID+1);
    }
    private void checkResult(int[] candidates) {
        int max = candidates[0];
        for(int i = 1; i<candidates.length; i++) {
            if(candidates[i]>max) {
                max = candidates[i];
            }
        }
        sendCoordinatorToken(max);
    }
    private void sendCoordinatorToken(int coordinator) {
        successorPass("CoordinatorMessage: "+coordinator+" "+process_ID, process_ID+1);
    }
   
    private void beginCoordinator() {
        controllerFile.setTextArea(process_ID, "New Coordinator Selected: " + "Process Number " + process_ID);
        processOnTimer = new Timer();
        processOnTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                successorPass("Process_On 0", process_ID+1);
            }
        }, 0, 5000);
    }
    private void endCoordinator() {
        processOnTimer.cancel();
    }
   
    private void restartTimer() {
        if(timer != null) {
            stopTimer();
        }
        startTimer();
    }
    private void startTimer() {
        if(timer == null) {
            timer = new Thread() {
                @Override
                public void run() {
                    long startTime = new Date().getTime();
                    while(true) {
                        long currentTime = new Date().getTime();
                        if((currentTime - startTime) >= timeout) {
                            executeElection();
                            restartTimer();
                        }
                    }
                }
            };
            timer.start();
        }
    }
 
    private void stopTimer() {
        if(timer != null) {
            timer.stop();
            timer = null;
        }
    }
  //Testing Code-----------------------------------------------
    public void listenSocket2(){
    	//Create socket connection
    	   try{
    	     Socket socket = new Socket("kq6py", 4321);
    	     PrintWriter out = new PrintWriter(socket.getOutputStream(), 
    	                 true);
    	     BufferedReader in = new BufferedReader(new InputStreamReader(
    	                socket.getInputStream()));
    	   } catch (UnknownHostException e) {
    	     System.out.println("Unknown host: kq6py");
    	     System.exit(1);
    	   } catch  (IOException e) {
    	     System.out.println("No I/O");
    	     System.exit(1);
    	   }
    	} 
    
    // This Method handles the Passing Of The Leader
    private void successorPass(String token, int Parent_Process) {
        if(Parent_Process>6) {
            Parent_Process = Parent_Process - 6;
        }
        try {
            Socket client_socket = new Socket(host, start_port+Parent_Process);
            PrintWriter out = new PrintWriter(client_socket.getOutputStream());
            out.println(token);
            out.flush();
            out.close();
            client_socket.close();
            if(!((new StringTokenizer(token).nextToken()).equals("Process_On"))) {
                controllerFile.setTextArea(process_ID, "Output Message: "+token);
               
            }
        } catch(Exception ex) {
            successorPass(token, Parent_Process+1);
        }
    }
  
}