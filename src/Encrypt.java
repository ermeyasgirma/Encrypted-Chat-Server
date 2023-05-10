import java.math.*;
import java.util.Random;
public class Encrypt {

    private long privateKey;
    private long p; 
    private long q;
    private long k;
    private long n;
    private long[] publicKey;

    public Encrypt() {
        publicKey = new long[2];
        generateKeyPair();

    }

    public String encryptMessage(String plainText, long[] recipientPubKey) {
        String cipherText = new String();
        for (char c : plainText.toCharArray()) {
            char cipherChar = encryptChar(c, recipientPubKey);
            cipherText += Character.toString(cipherChar);
        }
        return cipherText;
    }

    public char encryptChar(char c, long[] recipientPubKey) {
        char cipherChar = (char) (int) (Math.pow(c, recipientPubKey[1]) % recipientPubKey[0]);
        return cipherChar;
    }

    public long[] getPublicKey() {
        return publicKey;
    }

    public void generateKeyPair() {
        // generate public
        p = randomPrime(); q = randomPrime(); k = randomPrime();
        n = p*q;
        publicKey[0] = n; publicKey[1] = k;
        this.privateKey = generatePrivateKey();
    }

    public long randomPrime() {
        BigInteger prime = BigInteger.probablePrime(15, new Random());
        return prime.longValue();
    }
    
    public long generatePrivateKey() {
        // our private key is the modular multiplicative inverse of k under modulo phi(n)
        long phiN = (p-1)*(q-1);
        for (long i = 1; i < phiN; i++) {
            if (((k % phiN) * (i % phiN)) % phiN == 1) {
                return i;
            }
        }
        return 1L;
    }

    public String decryptMessage(String cipherText) {
        String plainText = "";
        for (char c : cipherText.toCharArray()) {
            char plainChar = decryptChar(c);
            plainText += Character.toString(plainChar);
        }
        return plainText;
    }

    public char decryptChar(char c) {
        char plainChar = Math.pow((double) ((int) c), privateKey);
    }
}
