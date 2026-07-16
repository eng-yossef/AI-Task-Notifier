package com.example.hellofx;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MenuPage {
    private VBox view;
    private Stage stage;

    public MenuPage(Stage stage) {
        this.stage = stage;
        view = new VBox(10);  // Vertical box with 10px spacing
        view.setPadding(new Insets(20));

        // Sample data (can be dynamically generated)
        String[] items = {"Item 1", "Item 2", "Item 3"};

        for (String item : items) {
            // Create a label for the item
            Label itemLabel = new Label(item);

            // Create a "View Details" button
            Button detailsButton = new Button("View Details");
            detailsButton.setOnAction(e -> showDetails(item)); // Pass the item to the details page

            // Add the label and button to the VBox
            view.getChildren().addAll(itemLabel, detailsButton);
        }
    }

    // Method to return the view
    public VBox getView() {
        return view;
    }

    // Navigate to the details page
    private void showDetails(String item) {
        DetailsPage detailsPage = new DetailsPage(stage, item);  // Pass the item to the details page
        Scene detailsScene = new Scene(detailsPage.getView(), 600, 400);

        // Change the scene to the details page
        stage.setScene(detailsScene);
        stage.setTitle("Details of " + item);
    }
}
