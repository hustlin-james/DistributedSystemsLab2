import threading
import time
import socket
import json
from enum import Enum 
from Tkinter import *
from random import randint

#James Fielder
#1000631384

HOST_NAME = "127.0.0.1"
PORT = 9000
BUF_SIZE = 16384

#Input: None
#Output: None
#Summary: Class for keeping track of NodeProcess Status
class State(Enum):
    running = 1
    stopped = 2

#Input: None
#Output: None
#Summary: Setup the GUI with the 3 buttons for running the tests
class App:
    def __init__(self,master):
        frame = Frame(master)
        frame.pack()

        self.button = Button(
            frame, text="QUIT", fg="red", command=frame.quit
            )
        self.button.pack(side=LEFT)

        self.start_process_btn = Button(frame, text="Start", command=start_node_processes)
        self.start_process_btn.pack(side=LEFT)

        self.stop_current_coordinator_btn = Button(frame, text="Stop Current Coordinator",command=stop_current_coordinator)
        self.stop_current_coordinator_btn.pack(side=LEFT)

        self.run_two_elections_btn = Button(frame, text="Run 2 Elections",command=run_two_elections)
        self.run_two_elections_btn.pack(side=LEFT)
        

#Input: [int] threadID, [int]port, [int]num_processes
#Output: None
#Summary: Subclass of the Thread class with variables and functions to allow socket connections.
#and simmulate a ring node.  It is created with a threadID which is a number 0-4, and a port which
#starts from 9000 and adds the threadID this way it is easy to calculate the successor which is just 
#the threadID + 1.  It also keeps track of the total number of nodes in the ring network that was 
#created. Most of the logic is in the process_msg function which reads a marshalled object from the socket
#and unmarshall it into a object with information.
class NodeProcess(threading.Thread):
    def __init__(self, threadID, port, num_processes):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.port = port
        self.num_processes = num_processes
        self.coordinator = -1
        self.status = State.running
        self.successor = None
    #Inputs: None
    #Outputs: None
    #Summary: Continously listen to the open socket for messages
    def run(self):
        s = socket.socket()
        s.bind((HOST_NAME, self.port))

        s.listen(5)       
        while True:
            c, addr = s.accept()
            d = c.recv(BUF_SIZE)
            self.process_msg(d)
            c.close()   

    #Inputs: None
    #Outputs: None
    #Summary: Handles logic for incoming messages
    def process_msg(self,msg):
        obj = json.loads(msg)
        command = obj['command'] 


        #Summary: This starts the ring simulation with Node 0, it sets its
        #coordinator to the highest number process and sents an update message
        #to the other nodes to update its coordinator
        if command == "start" and self.threadID == 0:
            time.sleep(3)
            print("[STARTING] ring at node %d"%(self.threadID))
            print("[COORDINATOR] is node %d"%(self.num_processes-1))
            self.coordinator = obj['coordinator']
            print("[UPDATING] coordinator for node %d with [COORDINATOR] %d"%(self.threadID,self.coordinator))
            send_to = (self.threadID+1)%self.num_processes
            port = PORT+send_to
            updated_nodes = []
            updated_nodes.append(self.threadID)
            msg = json.dumps({
                'command':'update_coordinator',
                'new_coordinator':(self.num_processes-1),
                'updated_nodes':updated_nodes
            })
            send_msg_from_socket(msg,port)
        #Summary: Sets the current nodes coodinator to the one that was sent in the message
        #If this is the last node to be updated then sends out a 'circulate' message which
        #just circulates the token between nodes. Also restarts a crashed process if it notice it.
        elif command == 'update_coordinator':

            time.sleep(3)
            new_coordinator = obj['new_coordinator']
         
            self.coordinator = new_coordinator
            updated_nodes = obj['updated_nodes']

            #Check if this is the last node we updated
            send_to = (self.threadID+1)%self.num_processes
            port = PORT+send_to

            crashed_coordinator = -1

            if 'crashed_coordinator' in obj:
                crashed_coordinator = obj['crashed_coordinator']

            #Finished update the coordinator of all processes
            if self.threadID in updated_nodes:
                print("[Finished] updating coordinator")

                if crashed_coordinator == -1:
                    send_to = (self.threadID+1)%self.num_processes
                    port = PORT+send_to
                    msg = json.dumps({
                        'command':'circulate',
                        'received_from':self.threadID,
                    })
                    send_msg_from_socket(msg,port)
                else:
                    if threads[crashed_coordinator].status != State.running:
                        print("[Restarting] crashed process %d"%(crashed_coordinator))

                    threads[crashed_coordinator].status = State.running
                    nodes = []
                    nodes.append(self.threadID)
                    nodes_str = ','.join(str(x) for x in nodes)
                    crashed_coordinator = -1
                    print("[ELECTION] At node %d with candidates: %s"%(self.threadID,nodes_str))
                    send_to = (self.threadID+1)%self.num_processes
                    port = PORT+send_to
                    msg = json.dumps({
                        'command':'election',
                        'nodes':nodes,
                        'crashed_coordinator':crashed_coordinator
                    })
                    send_msg_from_socket(msg,port)
            else:
                if self.status == State.running:
                    print("[UPDATING] coordinator for node %d with coordinator %d"%(self.threadID,new_coordinator))
                    updated_nodes.append(self.threadID)
                    msg = json.dumps({
                        'command':'update_coordinator',
                        'new_coordinator':new_coordinator,
                        'updated_nodes':updated_nodes,
                        'crashed_coordinator':crashed_coordinator
                    })
                    send_msg_from_socket(msg,port)
                else: 
                    msg = json.dumps({
                        'command':'update_coordinator',
                        'new_coordinator':new_coordinator,
                        'updated_nodes':updated_nodes,
                        'crashed_coordinator':crashed_coordinator
                    })
                    send_msg_from_socket(msg,port)
        #Summary: the current node will receive the other candidate nodes in the massage
        #and if it has not been added it will add it to the candiates and forward the message
        #if the node has already been added then the election has gone full circle, in which case
        #the highest number processes will be chosen as the new coordinator and an update message will 
        #be sent out.
        elif command == 'election':
            time.sleep(3)
            nodes = obj['nodes']
            
            crashed_coordinator = -1
            if 'crashed_coordinator' in obj:
                    crashed_coordinator = obj['crashed_coordinator']

            if self.status == State.running:
                if self.threadID in nodes:
                    #We have come full circle, pick the highest node and set as the coordinator
                    h_node = max(nodes)
                    self.coordinator = h_node

                    print("[ELECTION] Finished with candidates: %s"%(','.join(str(x) for x in nodes)))
                    print("[UPDATING] coordinator for node %d with coordinator %d"%(self.threadID,h_node))
                    send_to = (self.threadID+1)%self.num_processes
                    port = PORT+send_to
                    msg = json.dumps({
                        'command':'update_coordinator',
                        'new_coordinator':h_node,
                        'updated_nodes':[self.threadID],
                        'crashed_coordinator':crashed_coordinator
                    })
                    send_msg_from_socket(msg,port)
                else:
                    nodes.append(self.threadID)
                    nodes_str = ','.join(str(x) for x in nodes)
                    print("[ELECTION] At node %d with candidates: %s"%(self.threadID,nodes_str))
                    send_to = (self.threadID+1)%self.num_processes
                    port = PORT+send_to
                    msg = json.dumps({
                        'command':'election',
                        'nodes':nodes,
                        'crashed_coordinator':crashed_coordinator
                    })
                    send_msg_from_socket(msg,port)
            else:
                #not running just pass along the message to the successor
                send_to = (self.threadID+1)%self.num_processes
                port = PORT+send_to
                msg = json.dumps({
                    'command':'election',
                    'nodes':nodes,
                    'crashed_coordinator':crashed_coordinator
                })
                send_msg_from_socket(msg,port)

        #Summary: Simulates communication between the ring network with a 
        #logical token.  Checks if the coordinator is running and if it
        #is not the an election will be started.
        elif command == 'circulate':
            time.sleep(3)

            coordinator_running = True
            crashed_coordinator = -1
            if self.threadID == self.coordinator and self.status == State.running:
                pass
            else:
                for t in threads:
                    if t.coordinator == t.threadID and t.status != State.running:
                        coordinator_running = False
                        crashed_coordinator = t.threadID
                        break
            
            send_to = (self.threadID+1)%self.num_processes
            port = PORT+send_to
            if coordinator_running == True:

                received_from = obj['received_from']
                if self.status == State.running and self.successor.status == State.running:
                    print("[CIRCULATE] token from node %s to node %d, coordinator %d is running"%(received_from,self.threadID,self.coordinator))
                    msg = json.dumps({
                        'command':'circulate',
                        'received_from':self.threadID
                    })
                else:
                    msg = json.dumps({
                        'command':'circulate',
                        'received_from':received_from
                    })
                send_msg_from_socket(msg,port)
            else:
                print("At Node %d and coordinator %d has stopped."%(self.threadID,self.coordinator))
                print("[Starting] election")

                nodes = []

                if self.status == State.running:
                    nodes.append(self.threadID)
                    nodes_str = ', '.join(str(x) for x in nodes)
                    
                    print("[ELECTION] At node %d with candidates: %s"%(self.threadID,nodes_str))
                    msg = json.dumps({
                        'command':'election',
                        'nodes':nodes,
                        'crashed_coordinator':crashed_coordinator
                    })
                    send_msg_from_socket(msg,port)
                else: 
                    msg = json.dumps({
                        'command':'election',
                        'nodes':nodes,
                        'crashed_coordinator':crashed_coordinator
                    })
                    send_msg_from_socket(msg,port)

#Inputs: [str] msg, [int] port
#Outputs: None
#Summary: This is a helper method to send messages from socket to socket using
#the specified port.
def send_msg_from_socket(msg,port):
    s = socket.socket()
    s.connect((HOST_NAME,port))
    s.send(msg)
    s.close()

#Inputs: None
#Output: None
#Sends another message to a node so that more than one process will
#send the election message
def run_two_elections():
    print("[SIMULATING] 2 Elections")
    current_coordinator = threads[0].coordinator
    threads[current_coordinator].status = State.stopped
    nodes = []
    msg = json.dumps({
        'command':'election',
        'nodes':nodes,
        'crashed_coordinator':current_coordinator
    })
    threadID = randint(0,n_processes-1)
    print("At Node %d and coordinator %d has stopped."%(threadID,current_coordinator))
    print("[Starting] election")
    send_msg_from_socket(msg,PORT+threadID)

#Inputs: None
#Output: None
#Stops the current coordinator and allows the next process to start an election
#Get the current coordinate then send the stop 
def stop_current_coordinator():

    current_coordinator = threads[0].coordinator
    threads[current_coordinator].status = State.stopped
    print('[STOPPING] coordinator node %d'%(current_coordinator))

#Inputs: None
#Outputs: None
#Summary: Setup the neccesary variables and logic to simulate a ring election with 5 processes
#NodeProcess is a subclass of Threads that has logic for connection to it with sockets thus allowing
#the Nodes to pass messages to each other
def start_node_processes():
    global threads
    global n_processes
    app.start_process_btn.config(state = DISABLED)
    n_processes = 7
    threads = []

    for i in range(n_processes):
        threadId = i
        port = PORT+i
        t = NodeProcess(threadId,port,n_processes)
        t.daemon = True
        t.start()
        threads.append(t)

    for i in range(n_processes):
        if i == (n_processes-1):
            threads[i].successor = threads[0]
        else:
            threads[i].successor = threads[i+1]

    s = socket.socket()
    
    s.connect((HOST_NAME,threads[0].port))
    msg = json.dumps({
        'command':'start',
        'coordinator':(n_processes-1)
    })
    s.send(msg)
    s.close()

def main():  
    global app
    root = Tk()
    root.title('Ring Network Election Simulation')
    app = App(root)
    root.mainloop()
    root.destroy()

if __name__ == "__main__":
    main()