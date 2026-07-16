package com.example.hellofx;

import com.example.hellofx.pages.AnalyticsDashboard;
import com.example.hellofx.pages.CalendarView;
import com.example.hellofx.pages.SettingsPage;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Switch {

    private Integer id;
    private String username;

    // Constructor with optional parameters
    public Switch(Integer id, String username) {
        this.id = (id != null) ? id : 0; // Default value for id if null (0 as default)
        this.username = (username != null) ? username : "Default Username"; // Default value for username
    }

    // Constructor without parameters (default values)
    public Switch() {
        this(CurrentUser.getInstance().getId(), CurrentUser.getInstance().getUsername()); // Calls the constructor with default values for id and username
    }

    // Method to return the HBox containing buttons with actions for navigation
    public HBox createNavigationButtons(Stage primaryStage) {
        // Create buttons for navigation
        Button friendsButton = createRoundedButtonWithIcon("Friends", "icons/friends.png");
        Button homeButton = createRoundedButtonWithIcon("Home", "icons/home.png");
        Button friendRequestsButton = createRoundedButtonWithIcon("Friend Requests", "icons/requests.png");
        Button sharedTasksButton = createRoundedButtonWithIcon("AI Assistant", "icons/tasks.png");
        
        // Create new buttons for advanced features
        Button analyticsButton = createRoundedButtonWithIcon("Analytics", "icons/analytics.png");
        Button calendarButton = createRoundedButtonWithIcon("Calendar", "icons/calendar.png");
        Button settingsButton = createRoundedButtonWithIcon("Settings", "icons/settings.png");

        // Add actions for navigation
        friendsButton.setOnAction(e -> switchToPage(primaryStage, "friends"));
        friendRequestsButton.setOnAction(e -> switchToPage(primaryStage, "requests"));
        sharedTasksButton.setOnAction(e -> switchToPage(primaryStage, "sharedTasks"));
        homeButton.setOnAction(e -> switchToPage(primaryStage, "Home"));
        analyticsButton.setOnAction(e -> switchToPage(primaryStage, "analytics"));
        calendarButton.setOnAction(e -> switchToPage(primaryStage, "calendar"));
        settingsButton.setOnAction(e -> switchToPage(primaryStage, "settings"));

        // Arrange buttons horizontally in an HBox
        HBox buttonBox = new HBox(50); // Spacing of 10 between buttons
        buttonBox.getChildren().addAll(
            homeButton,
            friendsButton,
            friendRequestsButton,
            sharedTasksButton,
            analyticsButton,
            calendarButton,
            settingsButton
        );
        buttonBox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        return buttonBox;
    }

    // General method to switch between pages dynamically
    private void switchToPage(Stage currentStage, String page) {
        Stage newStage = new Stage();

        // Set the dimensions and position to match the current stage
        newStage.setWidth(currentStage.getWidth());
        newStage.setHeight(currentStage.getHeight());
        newStage.setX(currentStage.getX());
        newStage.setY(currentStage.getY());

        // Dynamically update the new stage dimensions when the current stage resizes or moves
        currentStage.widthProperty().addListener((obs, oldWidth, newWidth) -> newStage.setWidth(newWidth.doubleValue()));
        currentStage.heightProperty().addListener((obs, oldHeight, newHeight) -> newStage.setHeight(newHeight.doubleValue()));
        currentStage.xProperty().addListener((obs, oldX, newX) -> newStage.setX(newX.doubleValue()));
        currentStage.yProperty().addListener((obs, oldY, newY) -> newStage.setY(newY.doubleValue()));

        try {
            // Open the corresponding page based on the page type passed
            switch (page) {
                case "friends":
                    new FriendsPage().start(newStage);
                    break;
                case "requests":
                    new RequestsPage().start(newStage);
                    break;
                case "sharedTasks":
                    new SharedTasksPage().start(newStage);
                    break;
                case "Home":
                    new Home().start(newStage);
                    break;
                case "analytics":
                    new AnalyticsDashboard().start(newStage);
                    break;
                case "calendar":
                    new CalendarView().start(newStage);
                    break;
                case "settings":
                    new SettingsPage().start(newStage);
                    break;
                default:
                    throw new IllegalArgumentException("Page type not recognized: " + page);
            }

            // Optionally close the current stage
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Utility method to create a rounded button with an icon and consistent style
    private Button createRoundedButtonWithIcon(String text, String iconPath) {
        Button button = new Button(text);

        // Load the icon
        ImageView iconView = createIcon(iconPath);

        if (iconView != null) {
            button.setGraphic(iconView);
        }

        button.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-padding: 10 20;");
        return button;
    }

    // Utility method to create an ImageView for the icon
    private ImageView createIcon(String iconPath) {
        try {
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            ImageView iconView = new ImageView(icon);
            iconView.setFitWidth(16); // Set desired icon width
            iconView.setFitHeight(16); // Set desired icon height
            return iconView;
        } catch (Exception e) {
            System.err.println("Icon not found: " + iconPath);
            return null;
        }
    }
}
