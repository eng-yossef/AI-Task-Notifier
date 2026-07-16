package com.example.hellofx;
import javafx.application.Platform;
import javafx.concurrent.Task; // Add this import statement

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FriendsPage extends Application {
    private static ObservableList<Friend> friendsList = null;
    private static ObservableList<Friend> mutualFriendsList = null;
    private final VBox friendsListContainer = new VBox(10);
    private final VBox searchResultsContainer = new VBox(10);
    private final VBox mutualFriendsListContainer = new VBox(10);
    private final PauseTransition pause = new PauseTransition(Duration.millis(300)); // Debounce delay
    TextField searchField=null;

    @Override
    public void start(Stage primaryStage) {
        FriendRequestClient.sentRequests.setAll(FriendRequestClient.getSentRequests());

        // Create navigation buttons and add them to the layout
        HBox btnBox = new Switch().createNavigationButtons(primaryStage);
        // Rounded search bar
         searchField = new TextField();
        searchField.setPromptText("Search for users...");
        searchField.setStyle("-fx-background-radius: 20; -fx-padding: 8 12; -fx-border-radius: 20; -fx-border-color: #cccccc;");
        searchField.setMinWidth(400);

        // Add listener to dynamically call performSearch
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.setOnFinished(event -> {
                if (!newValue.trim().isEmpty()) {
                    performSearch(newValue.trim());
                } else {
                    searchResultsContainer.getChildren().clear(); // Clear results for empty query
                }
            });
            pause.playFromStart(); // Restart debounce timer
        });

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 16;");

        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);

        // Style containers
        friendsListContainer.setStyle("-fx-padding: 20; -fx-background-color: #f9f9f9;");
        mutualFriendsListContainer.setStyle("-fx-padding: 20; -fx-background-color: #f2f2f2;");
        searchResultsContainer.setStyle("-fx-padding: 20; -fx-background-color: #e6f7ff;");

        // Add labels for sections
        Label searchResultsLabel = new Label("Search Results");
        searchResultsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        searchResultsContainer.getChildren().add(searchResultsLabel);

        // Retrieve data from the client
        friendsList = FriendClient.getFriends();
        mutualFriendsList = FriendClient.getMutualFriends();

        // Update lists in the UI
        updateFriendsList();
        updateMutualFriendsList();

        // Set search functionality
        searchButton.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
        });

        VBox vb = new VBox(20,
                searchBox,
                searchResultsContainer
        );
        HBox hb = new HBox(50, friendsListContainer, vb, mutualFriendsListContainer);

        // Wrap in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(hb);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-padding: 10; -fx-background: transparent;");

        VBox root = new VBox(20, btnBox, hb);
        root.setStyle("-fx-alignment: top-center; -fx-padding: 20;");

        // Set up the scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Friends Page");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to perform a search and update the search results container
    private void performSearch(String query) {
        searchResultsContainer.getChildren().clear();

        // Add label to indicate search results
        Label header = new Label("Search Results for '" + query + "'");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        searchResultsContainer.getChildren().add(header);

        try {
            String token = Token.getInstance().getToken();


            // Create the URL with the search term
            URL url = new URL("http://localhost:8080/servletnew/search?searchTerm=" + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", "application/json");


            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // Read the response from the API
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                // Parse the JSON response (an array of users)
                JSONArray users = new JSONArray(response.toString());

                if (users.length() > 0) {
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        Friend friend = new Friend(user.getInt("id"), user.getString("username"), "");
                        HBox userBox = createSearchResultDisplay(friend);
                        searchResultsContainer.getChildren().add(userBox);
                    }
                } else {
                    Label noResultsLabel = new Label("No results found.");
                    searchResultsContainer.getChildren().add(noResultsLabel);
                }
            } else {
                Label errorLabel = new Label("Error fetching search results. Please try again.");
                searchResultsContainer.getChildren().add(errorLabel);
            }
        } catch (Exception e) {
            Label errorLabel = new Label("Error: " + e.getMessage());
            searchResultsContainer.getChildren().add(errorLabel);
            e.printStackTrace();
        }
    }

    // Method to update the friends list UI
    private void updateFriendsList() {
        friendsListContainer.getChildren().clear();
        Label header = new Label("Your Friends ( " + friendsList.size() + " )");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        friendsListContainer.getChildren().add(header);

        for (Friend friend : friendsList) {
            HBox friendBox = createFriendDisplay(friend);
            friendsListContainer.getChildren().add(friendBox);
        }
    }

    // Method to update the mutual friends list UI
    private void updateMutualFriendsList() {
        mutualFriendsListContainer.getChildren().clear();
        Label header = new Label("People You May Know");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        mutualFriendsListContainer.getChildren().add(header);

        for (Friend friend : mutualFriendsList) {
            HBox friendBox = createMutualFriendDisplay(friend);
            mutualFriendsListContainer.getChildren().add(friendBox);
        }
    }

    private HBox createSearchResultDisplay(Friend friend) {
        // Fetch and cache sent requests list
        List<FriendRequest> sentRequests = FriendRequestClient.getSentRequests();

        // Label for the friend's username
        Label friendLabel = new Label(friend.getUsername());
        friendLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Check if the friend is in the friends list or sent requests
        final boolean isaFriend = friendsList.stream().anyMatch(f -> f.getId() == friend.getId());
        final boolean isaSentRequest = sentRequests.stream().anyMatch(f -> f.getId() == friend.getId());
        final boolean[] isFriend = {isaFriend};
        final boolean[] isSentRequest = {isaSentRequest};

        // Create a button and determine its initial state
        Button actionButton = new Button();
        updateButtonState(actionButton, isFriend[0], isSentRequest[0]);

        // Add event listener to toggle friend/request state
        actionButton.setOnAction(e -> {
            if (isFriend[0]) {
                // Remove friend and update state
                FriendClient.removeFriend(friend.getId());
                isFriend[0] = false;
            } else if (isSentRequest[0]) {
                // Cancel sent request and update state
                FriendRequestClient.cancelFriendRequest(friend.getId());
                isSentRequest[0] = false;
            } else {
                // Send new friend request and update state
                FriendRequestClient.sendFriendRequest(friend.getId());
                isSentRequest[0] = true;
            }
            updateMutualFriendsList();

            // Refresh friends and sent requests lists
            updateFriendsList();
            List<FriendRequest> updatedSentRequests = FriendRequestClient.getSentRequests();

            // Update button state based on refreshed data
            isFriend[0] = friendsList.stream().anyMatch(f -> f.getId() == friend.getId());
            isSentRequest[0] = updatedSentRequests.stream().anyMatch(f -> f.getId() == friend.getId());
            updateButtonState(actionButton, isFriend[0], isSentRequest[0]);
        });

        // Create and style the HBox for this friend
        HBox friendBox = new HBox(10, friendLabel, actionButton);
        friendBox.setStyle("-fx-alignment: center-left; -fx-padding: 10; -fx-border-color: #cccccc; -fx-background-color: #ffffff; -fx-border-radius: 10; -fx-background-radius: 10;");
        return friendBox;
    }

    // Helper method to update button's state
    private void updateButtonState(Button button, boolean isFriend, boolean isSentRequest) {
        if (isFriend) {
            button.setText("Unfriend");
            button.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white;"); // Orange for 'Unfriend'
        } else if (isSentRequest) {
            button.setText("Cancel Request");
            button.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white;"); // Yellow for 'Cancel Request'
        } else {
            button.setText("Send Request");
            button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // Green for 'Send Request'
        }
    }

    private HBox createFriendDisplay(Friend friend) {
        Label friendLabel = new Label(friend.getUsername());
        friendLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button deleteButton = createStyledButton("Delete", "#F44336");
        deleteButton.setOnAction(e -> {
            FriendClient.removeFriend(friend.getId());
            friendsList = FriendClient.getFriends();
            updateFriendsList();
        });

        HBox friendBox = new HBox(10, friendLabel, deleteButton);
        friendBox.setStyle("-fx-alignment: center-left; -fx-padding: 10; -fx-border-color: #cccccc; -fx-background-color: #ffffff; -fx-border-radius: 10; -fx-background-radius: 10;");
        return friendBox;
    }
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8 16;");
        return button;
    }


    private HBox createMutualFriendDisplay(Friend friend) {
        // Label for the friend's username
        Label friendLabel = new Label(friend.getUsername());
        friendLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Create a button for sending a friend request (if not already friends)
        Button actionButton = new Button();

        // Check if already a friend
        boolean isAlreadyFriend = friendsList.stream().anyMatch(f -> f.getId() == friend.getId());
        boolean isSentRequest = FriendRequestClient.getSentRequests().stream().anyMatch(f -> f.getId() == friend.getId());

        // Update button state based on whether they are already a friend or if a request is pending
        updateButtonState(actionButton, isAlreadyFriend, isSentRequest);

        // Add event listener to handle friend requests
        actionButton.setOnAction(e -> {


            if (isAlreadyFriend) {
                FriendClient.removeFriend(friend.getId());
            } else if (isSentRequest) {
                FriendRequestClient.cancelFriendRequest(friend.getId());
            } else {
                FriendRequestClient.sendFriendRequest(friend.getId());
            }
            // Add an action listener or event handler to perform the search when needed

                String searchText = searchField.getText(); // Get the current value of the search field
                performSearch(searchText); // Call your search method with the value






            // Refresh mutual friends list after action
            mutualFriendsList = FriendClient.getMutualFriends(); // Update mutual friends list
            updateMutualFriendsList(); // Update the UI container to reflect the changes

        });

        // Create and style the HBox for this mutual friend
        HBox friendBox = new HBox(10, friendLabel, actionButton);
        friendBox.setStyle("-fx-alignment: center-left; -fx-padding: 10; -fx-border-color: #cccccc; -fx-background-color: #ffffff; -fx-border-radius: 10; -fx-background-radius: 10;");
        return friendBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
