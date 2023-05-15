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
        //ChatViewer viewer = new ChatViewer();
        //viewer.setVisible(true);
        //Client c1 = new Client(viewer.getUserInput());
        Client c1 = new Client(System.in);
        c1.run();
        BufferedReader in = c1.getIn();
        PrintWriter out = c1.getOut();
        PublicKey publicKey = c1.getPublicKey();
        try {
            String msgFromServer;

            while ((msgFromServer = in.readLine()) != null) {
                String decrypted = c1.decrypt(msgFromServer);
                if (msgFromServer.startsWith("/serverKey")) {
                    String stringPublicKey  = in.readLine();
                    byte[] serverkeyAsArray = Base64.getDecoder().decode(stringPublicKey);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    // do clientInstance.setServerPublicKey(serverPubkey)
                    PublicKey serverPubKey = keyFactory.generatePublic(new X509EncodedKeySpec(serverkeyAsArray));
                    c1.setServerPublicKey(serverPubKey);
                    // convert our public key to string then send to server
                    byte[] keyAsArray = publicKey.getEncoded();
                    out.println(Base64.getEncoder().encodeToString(keyAsArray));
                } else {
                    System.out.println(decrypted);
                    //viewer.showMessage(decrypted);

                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
