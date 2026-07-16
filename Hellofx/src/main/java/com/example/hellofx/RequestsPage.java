package com.example.hellofx;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.List;

public class RequestsPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create navigation buttons and add them to the layout
        HBox btnBox = new Switch().createNavigationButtons(primaryStage);

        // Create the main content container for friend requests
        VBox friendRequestContentContainer = new VBox(10);
        friendRequestContentContainer.setStyle("-fx-padding: 20;");

        // Fetch and display friend requests dynamically
        displayFriendRequests(friendRequestContentContainer, primaryStage);

        // Combine buttons and content in the root layout
        VBox root = new VBox(20, btnBox, friendRequestContentContainer);
        root.setStyle("-fx-alignment: top-center; -fx-padding: 20;"); // Align everything to the top

        // Set up the scene
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Friend Requests Page");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Helper method to display friend requests
    private void displayFriendRequests(VBox friendRequestContentContainer, Stage primaryStage) {
        // Clear previous content
        friendRequestContentContainer.getChildren().clear();

        // Fetch the friend requests
        List<FriendRequest> friendRequests = FriendRequestClient.viewFriendRequests();

        // Display each friend request with Accept and Delete buttons
        for (FriendRequest request : friendRequests) {
            // Create a label for each request
            Label requestLabel = new Label(request.getUsername() + " has sent you a friend request.");
            requestLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            // Create Accept and Delete buttons with styles
            Button acceptButton = createStyledButton("Accept", "#4CAF50"); // Green button
            acceptButton.setOnAction(event -> {
                respondToRequest(request.getId(), "accepted");
                displayFriendRequests(friendRequestContentContainer, primaryStage); // Refresh the UI
            });

            Button deleteButton = createStyledButton("Delete", "#F44336"); // Red button
            deleteButton.setOnAction(event -> {
                FriendRequestClient.cancelFriendRequest(request.getId());
                displayFriendRequests(friendRequestContentContainer, primaryStage); // Refresh the UI
            });

            // Place buttons and label in a horizontal box (to align them in the same line)
            HBox buttonsBox = new HBox(10, acceptButton, deleteButton);
            buttonsBox.setAlignment(Pos.CENTER_RIGHT); // Align buttons to the right

            // Create a VBox to hold the label and buttons, and add a border around it
            VBox requestBox = new VBox(10, requestLabel, buttonsBox);
            requestBox.setStyle(
                    "-fx-border-color: #ddd; " +  // Light gray border
                            "-fx-border-radius: 10px; " +  // Rounded corners
                            "-fx-padding: 10px; " +  // Padding inside the border
                            "-fx-background-color: #f9f9f9; " +  // Light background for each card
                            "-fx-margin: 10px; "  // Margin around each card
            );

            // Add the requestBox to the content container
            friendRequestContentContainer.getChildren().add(requestBox);
        }
    }

    // Method to create a styled button with rounded corners and a background color
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-cursor: hand;");
        return button;
    }

    // Helper method to respond to a friend request
    private void respondToRequest(int userId, String status) {
        FriendRequestClient.respondToFriendRequest(userId, status);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
