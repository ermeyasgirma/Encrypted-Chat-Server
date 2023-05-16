import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.text.*;
import java.util.*;

public class Client implements Runnable {
    
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private boolean closeConnection;
    private PublicKey serverPubKey;
    private Encrypt encryptor;
    private PublicKey publicKey;
    private SimpleDateFormat formatter;  
    private Date date;
    

    public Client() {
        closeConnection = false;
        encryptor = new Encrypt();
        publicKey = encryptor.getPublicKey();
        formatter = new SimpleDateFormat("HH:mm");
        date = new Date();
    }

    public void setServerPublicKey(PublicKey serverPublicKey) {
        this.serverPubKey = serverPublicKey;
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public void run() {
        try {
            socket = new Socket("127.0.0.1", 9999);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            //InputHandler iHandler = new InputHandler();
            //Thread t = new Thread(iHandler);
            //t.start();

        } catch (Exception e) {
            close();
        }

    }

    public String encrypt(String s) {
        return encryptor.encryptMessage(s, serverPubKey);
    }

    public String decrypt(String s) {
        return encryptor.decryptMessage(s);
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
        private Scanner inputReader;
        public void run () {
            System.out.println("We at least begin running ");
            try {
                while (!closeConnection) {
                    inputReader = new Scanner(System.in);
                    System.out.println("We enter the while loop to check scanner input");
                    String msg = inputReader.next();
                    System.out.println("The user input read in is " + msg);
                    if (msg.equals("/exit")) {
                        out.println(encrypt("/exit"));
                        inputReader.close();
                        close();
                    } else {
                        out.println(encrypt(msg));
                    }
                }
            } catch (Exception e) {
                System.out.println("While loop throws an exception ");
                close();
            }
            System.out.println("We leave the run method now ");
        }
        
    }

    public static void main(String[] args) {
        //Client client = new Client();
        //client.run();
    }

}
