package com.example.hellofx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CheckoutController {

    @FXML
    private Label totalLabel; // This should match the fx:id in your FXML

    @FXML
    private void initialize() {
        // Set the initial text for the total label or any other setup
        totalLabel.setText("Total: $0.00"); // Ensure totalLabel is not null
    }
}
