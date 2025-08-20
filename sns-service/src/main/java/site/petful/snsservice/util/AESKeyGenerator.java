package site.petful.snsservice.util;

import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AESKeyGenerator {

    public static void main(String[] args) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // AES-128: 128, AES-192: 192, AES-256: 256
        SecretKey secretKey = keyGen.generateKey();

        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("Generated AES Key: " + encodedKey);
    }
}
