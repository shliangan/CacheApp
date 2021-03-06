package CacheSync;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author shanliang
 */
public class ChatServer implements Runnable {

    private ChatServerThread clients[] = new ChatServerThread[50];
    private ServerSocket server = null;
    private Thread thread = null;
    private int clientCount = 0;
    public Payload load = null;
    
    private int numberOfHandlesOccured = 0;
    private Payload client0Payload = null;
    
    GUI g;

    private static final int k = 13;
    
    public ChatServer(int port, GUI gui) {
        try {
            this.g=gui;
 
                SwingWorker.setGUIText("Binding to port " + port + ", please wait  ...");
            

            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port);
                SwingWorker.setGUIText("Server started: " + server);
            

            System.out.println("Server started: " + server);
            start();
        } catch (IOException ioe) {

            SwingWorker.setGUIText("Can not bind to port " + port + ": " + ioe.getMessage());
            System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
        }
    }

    public void run() {
        while (thread != null) {
            try {
           SwingWorker.setGUIText("Waiting for a client ...");

                System.out.println("Waiting for a client ...");
                addThread(server.accept());
            } catch (IOException ioe) {
                
         SwingWorker.setGUIText("Server accept error: " + ioe);
                System.out.println("Server accept error: " + ioe);
                try {
                    stop();
                } catch (IOException ex) {
                    Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() throws IOException {
        if (thread != null) {
            server.close();
            thread = null;
        }
    }

    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }
    
    public void send(Payload load){
        try {
            for (int i = 0; i < clientCount; i++) {
                clients[i].send(load);
            }
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void handle(int ID, Payload pl) throws IOException {
        this.load = pl;
        

                g.setTextMessage("Received payload with\n" + 
                                   "\t ID: " + pl.id);
                
                System.out.println("Received payload with\n" + 
                                   "\t ID: " + pl.id);
                
                if (pl.id == 2) {
                    if (numberOfHandlesOccured == 0 && clientCount == 2) {
                        System.out.println("Recieved ArrayList of Strings and Bloom Filter");
                        SwingWorker.setGUIText("Recieved ArrayList of Strings and Bloom Filter");
                        System.out.println("Number of strings recieved: " + pl.strings.size());
                        SwingWorker.setGUIText("Number of strings recieved: " + pl.strings.size());
                        Initialize.addStrings(pl.strings);
                        client0Payload = pl;
                    }
                    else {
                        System.out.println("Recieved ArrayList of Strings and Bloom Filter");
                        SwingWorker.setGUIText("Recieved ArrayList of Strings and Bloom Filter");
                        System.out.println("Number of strings recieved: " + pl.strings.size());
                        SwingWorker.setGUIText("Number of strings recieved: " + pl.strings.size());
                        // Determine which strings the client needs
                        ArrayList<String> stringsToSend = Initialize.getStrings(pl.filter, pl.keySize, pl.numberOfElements);
                        load = new Payload(3, null, stringsToSend);
                        send(load);
                        Initialize.addStrings(pl.strings);
                    }
                }
                
                numberOfHandlesOccured++;
                
                if (numberOfHandlesOccured == 2) {
                    SwingWorker.setGUIText("Sending updated strings to Client 0.");
                    // Get strings to send
                    ArrayList<String> stringsToSend = Initialize.getStrings(client0Payload.filter, client0Payload.keySize, client0Payload.numberOfElements);
                    load = new Payload(3, null, stringsToSend);
                    clients[0].send(load);
                }
    }

    public synchronized void remove(int ID) throws IOException {
        int pos = findClient(ID);
        if (pos >= 0) {
            ChatServerThread toTerminate = clients[pos];
            System.out.println("Removing client thread " + ID + " at " + pos);
             SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                g.setTextMessage("Removing client thread " + ID + " at " + pos);
            }
        });
            if (pos < clientCount - 1) {
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i - 1] = clients[i];
                }
            }
            clientCount--;
            toTerminate.close();
            toTerminate.stop();
            
        }
    }

    private void addThread(Socket socket) throws IOException, ClassNotFoundException {
        if (clientCount < clients.length) {
            SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                g.setTextMessage("Client accepted: " + socket);
                
            }
        });
            System.out.println("Client accepted: " + socket);
            clients[clientCount] = new ChatServerThread(this, socket, this.g);
            clients[clientCount].open();
            clients[clientCount].start();
            clientCount++;
        } else {
            System.out.println("Client refused: maximum " + clients.length + " reached.");
                 SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                g.setTextMessage("Client refused: maximum " + clients.length + " reached.");
            }
        });
        
        }
    }

}

