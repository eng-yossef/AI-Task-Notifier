package com.example.hellofx;

import com.example.hellofx.MenuPage;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DetailsPage {
    private VBox view;
    private Stage stage;

    public DetailsPage(Stage stage, String item) {
        this.stage = stage;
        view = new VBox(10);  // Vertical box with 10px spacing
        view.setPadding(new Insets(20));

        // Create a label to show the item details
        Label detailsLabel = new Label("Details of: " + item);

        // Add a "Back to Menu" button to navigate back to the MenuPage
        Button backButton = new Button("Back to Menu");
        backButton.setOnAction(e -> goBackToMenu());

        // Add the label and button to the VBox
        view.getChildren().addAll(detailsLabel, backButton);
    }

    // Method to return the view
    public VBox getView() {
        return view;
    }

    // Navigate back to the MenuPage
    private void goBackToMenu() {
        MenuPage menuPage = new MenuPage(stage);
        Scene menuScene = new Scene(menuPage.getView(), 600, 400);

        stage.setScene(menuScene);
        stage.setTitle("Menu Page");
    }
}

