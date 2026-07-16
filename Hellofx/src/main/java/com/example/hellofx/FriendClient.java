package com.example.hellofx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FriendClient {

    // Base URL of your FriendshipServlet
    private static final String BASE_URL = "http://localhost:8080/servletnew/friendship";
//    private static List<Friend> friends = new ArrayList<>();
    public static ObservableList<Friend> friends = FXCollections.observableArrayList();


    // Get a list of friends (GET)
    public static void fetchFriends() {
        try {
            String token = Token.getInstance().getToken();

            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }

                // Parse the response and update the friends list
                friends.clear();
                JSONArray friendArray = new JSONArray(response.toString());
                for (int i = 0; i < friendArray.length(); i++) {
                    JSONObject friendJson = friendArray.getJSONObject(i);
                    Friend friend = new Friend(
                            friendJson.getInt("id"),
                            friendJson.getString("username"),
                            friendJson.getString("status")
                    );
                    friends.add(friend);
                }
            } else {
                printErrorResponse(connection);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Remove a friendship (DELETE)
    public static void removeFriend(int friendId) {
        try {
            String token = Token.getInstance().getToken();

            URL url = new URL(BASE_URL + "?friendId=" + friendId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                System.out.println("Friend removed successfully.");
                fetchFriends(); // Refresh the friend list
            } else {
                printErrorResponse(connection);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Utility method to print error responses
    private static void printErrorResponse(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            System.err.println("Error: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Return the list of friends
    public static ObservableList<Friend> getFriends() {
//        printFriends();
        fetchFriends();
        return friends;
    }


    // Print all friends in the list
    public static void printFriends() {
        if (friends.isEmpty()) {
            System.out.println("No friends found.");
        } else {
            System.out.println("Friends List:");
            for (Friend friend : friends) {
                System.out.printf("ID: %d, Username: %s, Status: %s%n",
                        friend.getId(), friend.getUsername(), friend.getStatus());
            }
        }
    }


    public static ObservableList<Friend> getMutualFriends() {
        ObservableList<Friend> mutualFriends = FXCollections.observableArrayList();

        try {
            String token = Token.getInstance().getToken();

            // Construct the URL for fetching mutual friends
            URL url = new URL(BASE_URL + "?action=mutual");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set HTTP method and headers
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", "application/json");

            // Check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response using org.json
                JSONArray jsonArray = new JSONArray(response.toString());
                ObservableList<FriendRequest> sentRequests = FriendRequestClient.getSentRequests();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int id = jsonObject.getInt("id");
                    String username = jsonObject.getString("username");

                    // Check if the friend is in the sent requests
                    boolean isPending = sentRequests.stream()
                            .anyMatch(request -> request.getId() == id && request.getStatus().equalsIgnoreCase("pending"));

                    if (isPending) {
                        mutualFriends.add(new Friend(id, username, "pending"));
                    } else {
                        mutualFriends.add(new Friend(id, username, "status"));  // Add with default status
                    }
                }
            } else {
                printErrorResponse(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Remove mutual friends that are already in the friends list
        mutualFriends.removeIf(mutualFriend ->
                friends.stream().anyMatch(friend -> friend.getId() == mutualFriend.getId())
        );

        return mutualFriends;
    }







    // Inner class to represent a Friend

}
