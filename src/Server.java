import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.net.*;
import java.time.*;
import java.text.*;

// add time stamps to messages

public class Server implements Runnable {
    private ServerSocket chatServer;
    private List<ClientHandler> clients;
    private Map<String, ClientHandler> nameToConnection;
    private Map<ClientHandler, Long> clientToPubKey; 
    private ExecutorService threadPool;
    private boolean endChat;
    private SimpleDateFormat formatter;  
    private Date date;  
    private Encrypt encryptor;
    private long[] publicKey;



    public Server() {
        clients = new ArrayList<>();
        nameToConnection = new HashMap<>();
        clientToPubKey = new HashMap<>();
        endChat = false;
        threadPool = Executors.newCachedThreadPool();
        formatter = new SimpleDateFormat("HH:mm");
        date = new Date();
        encryptor = new Encrypt();
        publicKey = encryptor.getPublicKey();
    }

    @Override
    public void run() {
        try {
            chatServer = new ServerSocket(9999);
            while (!endChat) {
                Socket client = chatServer.accept();
                ClientHandler cHandler = new ClientHandler(client);
                clients.add(cHandler);
                threadPool.execute(cHandler);
            }
        } catch (IOException e) {
            closeServer();
            System.out.println(e.getStackTrace());
        }
    }


    public void broadcastMessage(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    public void closeServer() {
        endChat = true;
        threadPool.shutdown();
        try {
            if (!chatServer.isClosed()) {
                chatServer.close();
            } 
            for (ClientHandler currClient : clients) {
                currClient.closeConnection();
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class ClientHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        private long clientPubKey;

        public ClientHandler(Socket client) {
            this.client = client;            
        }

        public PrintWriter getWriter() {
            return out;
        }

        public void run() {

            try {
                // use out to send a message to the client
                out = new PrintWriter(client.getOutputStream(), true);
                // use in to receive messages from the client
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                setUp();
                String message;
                while ((message = decrypt(in.readLine())) != null) {
                    System.out.println("Current message is " + message);
                    if (message.startsWith("/name")) {
                        String[] splitMsg = message.split(" ", 2);
                        if (splitMsg.length != 2) {
                            out.println(encrypt("No nickname provided"));
                        }
                        System.out.println(username + " has changed their username to " + splitMsg[1]);
                        broadcastMessage(encrypt(username + " has changed their username to " + splitMsg[1]));
                        nameToConnection.remove(username);
                        nameToConnection.put(username, this);
                        username = splitMsg[1];
                    } else if (message.equalsIgnoreCase("/exit")) {
                        System.out.println(username + " has left the chat :((");
                        broadcastMessage(encrypt(username + " has left the chat :(("));
                        closeConnection();
                        nameToConnection.remove(username);

                    } else if (message.startsWith("/pm")) {
                        // handle private message case
                        String[] splitMsg = message.split(" ", 3);
                        String recipientName = splitMsg[1];
                        ClientHandler recipient = nameToConnection.get(recipientName);
                        long recipientPubKey = clientToPubKey.get(recipient);
                        if (!nameToConnection.containsKey(recipientName)) {
                            out.println(encrypt("This user does not exist. Please enter a valid recipient."));
                        } else {
                            // we need to decrypt the message from the client - which is done above
                            // then re-encrypt it with our own public key and send it to the recipient
                            String privMsg = splitMsg[2];
                            System.out.println(username + " -> " + recipientName + " " + privMsg);
                            recipient.getWriter().println(encyptPriv(username + " (direct message): " + privMsg, recipientPubKey));
                        }
                    } else {
                        System.out.println(username + ": " + message);
                        broadcastMessage(encrypt(username + " (" + formatter.format(date) + ")" + ": " + message));
                    }
                }


            } catch(IOException exception) {
                closeConnection();
                exception.printStackTrace();
            }
        }

        public void setUp() {
            // sets up key collection 
            try {
                out.println("/serverKey " + Long.toString(publicKey));
                clientPubKey = Long.parseLong(in.readLine());
                clientToPubKey.put(this, clientPubKey);
                out.println(encrypt("Please enter a username, with no spaces:"));
                verifyUsername();
                nameToConnection.put(username, this);
                System.out.println(encrypt(username + " has connected"));
                broadcastMessage(encrypt(username + " has joined the chat"));

            } catch (Exception e) {
                closeConnection();
            }
        }

        public String encrypt(String s) {
            return encryptor.encryptMessage(s, clientPubKey);
        }

        public String encyptPriv(String s, long recipientPubKey) {
            return encryptor.encryptMessage(s, recipientPubKey);
        }

        public String decrypt(String s) {
            return "";
        }


        public void sendMessage(String msg) {
            out.println(msg);
        }

        public void verifyUsername() {
            try {
                String tempName;
                while (invalidUsername(tempName = in.readLine())) {
                    out.println("That username is either invalid or already taken. Please enter a new username.");
                }
                username = tempName;
            } catch (Exception e) {
                closeConnection();
                e.printStackTrace();
            }
        }

        public boolean invalidUsername(String username) {
            // checks if the username contains spaces or is already taken
            return (username.contains(" ") || nameToConnection.containsKey(username));
        }


        public void closeConnection() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    } 


    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    
}