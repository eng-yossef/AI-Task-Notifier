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

public class FriendRequestClient {
//    public static    List<Friend> sentRequests = new ArrayList<>();
    public static ObservableList<FriendRequest> sentRequests = FXCollections.observableArrayList();


    private static final String BASE_URL = "http://localhost:8080/servletnew/friendrequest"; // Update with your servlet URL

    // Send a friend request (POST)
    public static void sendFriendRequest(int friendId) {
        try {
            String token = Token.getInstance().getToken();

            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);

            String jsonBody = String.format("""
                {
                    "friendId": %d
                }
            """, friendId);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                printResponse(connection);

            } else {
                printResponse(connection);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // View incoming friend requests (GET)
    public static List<FriendRequest> viewFriendRequests() {
        List<FriendRequest> friendRequests = new ArrayList<>();
        try {
            String token = Token.getInstance().getToken();

            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }

                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    friendRequests.add(new FriendRequest(
                            jsonObject.getInt("userId"),
                            jsonObject.getString("username"),
                            jsonObject.getString("status")
                    ));
                }
            } else {
                printResponse(connection);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return friendRequests;
    }

    // Accept or reject a friend request (PUT)
    public static void respondToFriendRequest(int userId, String status) {
        try {
            String token = Token.getInstance().getToken();

            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);

            String jsonBody = String.format("""
                {
                    "userId": %d,
                    "status": "%s"
                }
            """, userId, status);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                printResponse(connection);
            } else {
                printResponse(connection);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cancel a pending friend request (DELETE)
    public static void cancelFriendRequest(int friendId) {
        try {
            String token = Token.getInstance().getToken();

            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);

            String jsonBody = String.format("""
                {
                    "friendId": %d
                }
            """, friendId);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                printResponse(connection);
            } else {
                printResponse(connection);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Utility method to print response
    private static void printResponse(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static List<FriendRequest> fetchSentRequests() {
        List<FriendRequest>sent=new ArrayList<>();

        try {
            String token = Token.getInstance().getToken();

            // Construct the URL for fetching sent requests
            URL url = new URL(BASE_URL + "?sent=true");
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
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int id = jsonObject.getInt("userId");
                    String username = jsonObject.getString("username");
                    String status = jsonObject.getString("status");

                    sent.add(new FriendRequest(id, username, status));
                }
            } else {
                printErrorResponse(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sent;
    }

    public static ObservableList<FriendRequest> getSentRequests() {
        // Fetch the latest sent requests from the backend
        List<FriendRequest> fetchedRequests = fetchSentRequests();
        // Update the ObservableList with the fetched data
        sentRequests.setAll(fetchedRequests);


        // Return the ObservableList
        return sentRequests;
    }

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

}
