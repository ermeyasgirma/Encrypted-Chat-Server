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
    private InputStream viewerInput; 
    

    public Client(InputStream i) {
        closeConnection = false;
        encryptor = new Encrypt();
        publicKey = encryptor.getPublicKey();
        formatter = new SimpleDateFormat("HH:mm");
        date = new Date();
        viewerInput = i; 
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

            InputHandler iHandler = new InputHandler(viewerInput);
            Thread t = new Thread(iHandler);
            t.start();

            /* 
            String msgFromServer;

            while ((msgFromServer = in.readLine()) != null) {
                String decrypted = decrypt(msgFromServer);
                System.out.println("Decrypted is " + decrypted);
                if (msgFromServer.startsWith("/serverKey")) {
                    String stringPublicKey  = in.readLine();
                    byte[] serverkeyAsArray = Base64.getDecoder().decode(stringPublicKey);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    // do clientInstance.setServerPublicKey(serverPubkey)
                    PublicKey serverPubKey = keyFactory.generatePublic(new X509EncodedKeySpec(serverkeyAsArray));
                    setServerPublicKey(serverPubKey);
                    // convert our public key to string then send to server
                    byte[] keyAsArray = publicKey.getEncoded();
                    out.println(Base64.getEncoder().encodeToString(keyAsArray));
                } else {
                    System.out.println(decrypted);
                    //viewer.showMessage(decrypted);

                }
            }
            */
        } catch (Exception e) {
            close();
        }

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
        private InputStream istream;

        public InputHandler(InputStream i) {
            istream = i;
        }

        private BufferedReader inputReader;

        public void run () {
            try {
                inputReader = new BufferedReader(new InputStreamReader(istream));
                while (!closeConnection) {
                    String msg = inputReader.readLine();
                    System.out.println(msg);
                    if (msg.equals("/exit")) {
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
        //Client client = new Client();
        //client.run();
    }

}
