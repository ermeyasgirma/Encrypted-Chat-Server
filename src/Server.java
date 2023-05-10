import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.net.*;
import java.text.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

// add time stamps to messages

public class Server implements Runnable {
    private ServerSocket chatServer;
    private List<ClientHandler> clients;
    private Map<String, ClientHandler> nameToConnection;
    private Map<ClientHandler, PublicKey> clientToPubKey; 
    private ExecutorService threadPool;
    private boolean endChat;
    private SimpleDateFormat formatter;  
    private Date date;  
    private Encrypt encryptor;
    private PublicKey publicKey;



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

    public String encrypt(String s, PublicKey pkey) {
        return encryptor.encryptMessage(s, pkey);
    }

    public String encyptPriv(String s, PublicKey recipientPubKey) {
        return encryptor.encryptMessage(s, recipientPubKey);
    }

    public String decrypt(String s) {
        return encryptor.decryptMessage(s);
    }



    public void broadcastMessage(String msg) {
        for (ClientHandler client : clients) {
            PublicKey pkey = clientToPubKey.get(client);
            client.sendMessage(encrypt(msg, pkey));
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
        private PublicKey clientPubKey;

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
                            out.println(encrypt("No nickname provided", clientPubKey));
                        }
                        System.out.println(username + " has changed their username to " + splitMsg[1]);
                        broadcastMessage(username + " has changed their username to " + splitMsg[1]);
                        nameToConnection.remove(username);
                        nameToConnection.put(username, this);
                        username = splitMsg[1];
                    } else if (message.equalsIgnoreCase("/exit")) {
                        System.out.println(username + " has left the chat :((");
                        broadcastMessage(username + " has left the chat :((");
                        closeConnection();
                        nameToConnection.remove(username);

                    } else if (message.startsWith("/pm")) {
                        // handle private message case
                        String[] splitMsg = message.split(" ", 3);
                        String recipientName = splitMsg[1];
                        ClientHandler recipient = nameToConnection.get(recipientName);
                        PublicKey recipientPubKey = clientToPubKey.get(recipient);
                        if (!nameToConnection.containsKey(recipientName)) {
                            out.println(encrypt("This user does not exist. Please enter a valid recipient.", clientPubKey));
                        } else {
                            // we need to decrypt the message from the client - which is done above
                            // then re-encrypt it with our own public key and send it to the recipient
                            String privMsg = splitMsg[2];
                            System.out.println(username + " -> " + recipientName + " " + privMsg);
                            recipient.getWriter().println(encyptPriv(username + " (direct message): " + privMsg, recipientPubKey));
                        }
                    } else {
                        System.out.println(username + ": " + message);
                        broadcastMessage(username + " (" + formatter.format(date) + ")" + ": " + message);
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
                //convert server public key to string
                byte[] encodedServerPubKey = publicKey.getEncoded();
                String serverPubKeyAsString = Base64.getEncoder().encodeToString(encodedServerPubKey);
                out.println("/serverKey ");
                out.println(serverPubKeyAsString);
                // convert clients public key string to public key type
                String clientPubKeyString = in.readLine();
                byte[] clientPubKeyBytes = Base64.getDecoder().decode(clientPubKeyString);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                clientPubKey = keyFactory.generatePublic(new X509EncodedKeySpec(clientPubKeyBytes));
                clientToPubKey.put(this, clientPubKey);
                out.println(encrypt("Please enter a username, with no spaces:", clientPubKey));
                verifyUsername();
                nameToConnection.put(username, this);
                System.out.println(username + " has connected");
                broadcastMessage(username + " has joined the chat");

            } catch (Exception e) {
                closeConnection();
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        public void verifyUsername() {
            try {
                String tempName;
                // need to decrypt line being read in
                while (invalidUsername(tempName = decrypt(in.readLine()))) {
                    out.println(encrypt("That username is either invalid or already taken. Please enter a new username.", clientPubKey));
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

        public PublicKey getPublicKey() {
            return clientPubKey;
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