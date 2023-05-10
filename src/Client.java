import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.nio.Buffer;

public class Client implements Runnable {
    
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private boolean closeConnection;
    private long serverPubKey;
    private Encrypt encryptor;

    public Client() {
        closeConnection = false;
        encryptor = new Encrypt();
    }


    public void run() {
        try {
            socket = new Socket("127.0.0.1", 9999);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            InputHandler iHandler = new InputHandler();
            Thread t = new Thread(iHandler);
            t.start();
            String msgFromServer;
            while ((msgFromServer = decrypt(in.readLine())) != null) {
                System.out.println(msgFromServer);
            }
        } catch (Exception e) {
            close();
        }

    }

    public String decrypt(String s) {
        return "";
    }

    public void close() {
        closeConnection = true;
        try {
            in.close();
            out.close();
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
        }
    }


    class InputHandler implements Runnable {

        private BufferedReader inputReader;

        public void run () {
            try {
                inputReader = new BufferedReader(new InputStreamReader(System.in));
                while (!closeConnection) {
                    String msg = inputReader.readLine();
                    if (msg.startsWith("/serverKey:")){
                        // no need to decrypt or encrypt initial key exchange
                        String[] msgSplit = msg.split(" ", 2);
                        serverPubKey = Long.valueOf(msgSplit[1]);
                        out.println(Long.toString(encryptor.getPublicKey()));
                    } else if (msg.equals("/exit")) {
                        out.println(encrypt("/exit"));
                        inputReader.close();
                        close();
                    } else {
                        out.println(encrypt(msg));
                    }
                }
            } catch (Exception e) {
                close();
            }
        }

        public String encrypt(String s) {
            return encryptor.encryptMessage(s, serverPubKey);
        }

        
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

}
