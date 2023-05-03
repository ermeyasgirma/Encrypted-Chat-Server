import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.net.*;

// add time stamps to messages

public class Server implements Runnable {
    private ServerSocket chatServer;
    private List<ClientHandler> clients;
    private Map<String, ClientHandler> nameToConnection;
    private ExecutorService threadPool;
    private boolean endChat;

    public Server() {
        clients = new ArrayList<>();
        nameToConnection = new HashMap<>();
        endChat = false;
        threadPool = Executors.newCachedThreadPool();
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
                out.println("Please enter a username, with no names");
                verifyUsername();
                nameToConnection.put(username, this);
                System.out.println(username + " has connected");
                broadcastMessage(username + " has joined the chat");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/name")) {
                        String[] splitMsg = message.split(" ", 2);
                        if (splitMsg.length != 2) {
                            out.println("No nickname provided");
                        }
                        System.out.println(username + " has changed their username to " + splitMsg[1]);
                        broadcastMessage(username + " has changed their username to " + splitMsg[1]);
                        nameToConnection.remove(username);
                        nameToConnection.put(username, this);
                        username = splitMsg[1];
                    } else if (message.startsWith("/exit")) {
                        closeConnection();
                        System.out.println(username + " has left the chat :((");
                        broadcastMessage(username + " has left the chat :((");
                        nameToConnection.remove(username);

                    } else if (message.startsWith("/pm")) {
                        // handle private message case
                        String[] splitMsg = message.split(" ", 3);
                        String recipient = splitMsg[1];
                        if (!nameToConnection.containsKey(recipient)) {
                            out.println("This user does not exist. Please enter a valid recipient.");
                        } else {
                            String privMsg = splitMsg[2];
                            // send private message 
                            System.out.println(username + " -> " + recipient + " " + privMsg);
                            nameToConnection.get(recipient).getWriter().println(username + " (direct message): " + privMsg);
                        }
                    } else {
                        System.out.println(username + ": " + message);
                        broadcastMessage(username + ": " + message);
                    }
                }


            } catch(IOException exception) {
                closeConnection();
                exception.printStackTrace();
            }
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