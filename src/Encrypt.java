import java.util.Base64;
import javax.crypto.Cipher;
import java.security.*;

public class Encrypt {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public Encrypt() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(1024);
            KeyPair keyPair = gen.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        } catch (Exception e) {
        }

    }

    public String encryptMessage(String message, PublicKey pubKey) {
        try {
            byte[] msgAsBytes = message.getBytes();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] cipherText = cipher.doFinal(msgAsBytes);
            return bytesToString(cipherText);
        } catch (Exception e) {
        }
        return null;
    }

    public String bytesToString(byte[] byteMsg) {
        return Base64.getEncoder().encodeToString(byteMsg);
    }

    
    public String decryptMessage(String cipherText) {
        try {
            byte[] msgAsBytes = stringToBytes(cipherText);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
            byte[] plainText = cipher.doFinal(msgAsBytes);
            return new String(plainText, "UTF8");
        } catch (Exception e) {
        }
        return null;
    }

    public byte[] stringToBytes(String cipherText) {
        return Base64.getDecoder().decode(cipherText);
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

/* 
    public static void main(String[] args) {
        try {
            Encrypt e1 = new Encrypt();
            System.out.println(e1.getPublicKey());
            String plainText = "Hello World";
            String cipherText = e1.encryptMessage(plainText, e1.getPublicKey());
            //System.out.println("Cipher text is " + cipherText);
            String decryptedMessage = e1.decryptMessage(cipherText);
            //System.out.println("Plain text is " + decryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

}
