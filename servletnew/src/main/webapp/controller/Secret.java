package main.webapp.controller;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;


public class Secret {
    private static SecretKey key;

    // Private constructor to prevent instantiation
    private Secret() {
    }

    // Static block to initialize the secret key once
    static {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            keyGenerator.init(256); // 256-bit key for HmacSHA256
            key = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // Public method to get the secret key (singleton)
    public static SecretKey getKey() {
        return key;
    }
}
