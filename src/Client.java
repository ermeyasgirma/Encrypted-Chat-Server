import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Client implements Runnable {
    
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private boolean closeConnection;
    private PublicKey serverPubKey;
    private Encrypt encryptor;
    private PublicKey publicKey;

    public Client() {
        closeConnection = false;
        encryptor = new Encrypt();
        publicKey = encryptor.getPublicKey();
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

            while ((msgFromServer = in.readLine()) != null) {
                String decrypted = decrypt(msgFromServer);
                if (msgFromServer.startsWith("/serverKey")) {
                    String stringPublicKey  = in.readLine();
                    byte[] serverkeyAsArray = Base64.getDecoder().decode(stringPublicKey);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    serverPubKey = keyFactory.generatePublic(new X509EncodedKeySpec(serverkeyAsArray));
                    // convert our public key to string then send to server
                    byte[] keyAsArray = publicKey.getEncoded();
                    out.println(Base64.getEncoder().encodeToString(keyAsArray));
                } else {
                    System.out.println(decrypted);
                }
            }
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

        private BufferedReader inputReader;


        public void run () {
            try {
                inputReader = new BufferedReader(new InputStreamReader(System.in));

                while (!closeConnection) {
                    String msg = inputReader.readLine();
                    if (msg.startsWith("/serverKey")){
                        // receive public key
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
