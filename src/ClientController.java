import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.text.*;
import java.util.*;

public class ClientController {
    

    /*
     * Have a controller which creates a client and a viewer
     * takes input from viewer and passes it to client to send a message to server 
     * when server sends a message we receive it in client and pass message to viewer to be displayed Client.java:57 - System.out.println(decrypted);
     */

    public static void main(String[] args) {
        Client c1 = new Client();
        c1.run();
        BufferedReader in = c1.getIn();
        PrintWriter out = c1.getOut();
        PublicKey publicKey = c1.getPublicKey();
        ChatViewer viewer = new ChatViewer(c1);
        viewer.setVisible(true);
        try {
            String msgFromServer;

            while ((msgFromServer = in.readLine()) != null) {
                String decrypted = c1.decrypt(msgFromServer);
                if (msgFromServer.startsWith("/serverKey")) {
                    String[] msgSplit = msgFromServer.split(" ", 2);
                    String stringPublicKey  = msgSplit[1];
                    byte[] serverkeyAsArray = Base64.getDecoder().decode(stringPublicKey);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    // do clientInstance.setServerPublicKey(serverPubkey)
                    PublicKey serverPubKey = keyFactory.generatePublic(new X509EncodedKeySpec(serverkeyAsArray));
                    c1.setServerPublicKey(serverPubKey);
                    // convert our public key to string then send to server
                    byte[] keyAsArray = publicKey.getEncoded();
                    String clientKeyString = Base64.getEncoder().encodeToString(keyAsArray);
                    out.println(clientKeyString);
                } else {
                    System.out.println("Received from server: " + decrypted);
                    viewer.showMessage(decrypted);
                }
            }
        } catch (Exception e) {
            System.out.println("Closing connection");
            c1.close();
            System.exit(0);
        }
    }
}
