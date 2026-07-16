package com.example.hellofx;

public class Token {
    private static Token instance;
    private String token;

    // Private constructor to restrict instantiation
    private Token() {}

    // Get the single instance of Token
    public static Token getInstance() {
        if (instance == null) {
            instance = new Token();
        }
        return instance;
    }

    // Getter for token
    public String getToken() {
        return token;
    }

    // Setter for token
    public void setToken(String token) {
        this.token = token;
    }

    // Clear the token
    public void clearToken() {
        this.token = null;
    }
}
