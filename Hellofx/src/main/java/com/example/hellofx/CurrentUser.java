package com.example.hellofx;

public class CurrentUser {
    private static CurrentUser instance;
    private Integer id;
    private String username;

    // Private constructor to prevent instantiation from other classes
    private CurrentUser() {
        this.id = 0; // Default value for id
        this.username = "Default Username"; // Default value for username
    }

    // Public method to get the single instance of CurrentUser
    public static CurrentUser getInstance() {
        if (instance == null) {
            instance = new CurrentUser();
        }
        return instance;
    }

    // Getter and Setter for id and username
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Method to reset user (if needed)
    public void resetUser() {
        this.id = 0;
        this.username = "Default Username";
    }
}
