package com.example.hellofx;

public class FriendRequest {
    private int userId;
    private String username;
    private String status;

    public FriendRequest(int userId, String username, String status) {
        this.userId = userId;
        this.username = username;
        this.status = status;
    }

    public int getId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
