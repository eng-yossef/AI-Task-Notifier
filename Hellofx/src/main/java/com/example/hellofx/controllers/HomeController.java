package com.example.hellofx.controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

public class HomeController {
    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to my Restaurant App!");
    }

    @FXML
    private Button menuButton;

    @FXML
    public void goToMenuPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hellofx/views/menu.fxml"));
            Stage stage = (Stage) menuButton.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Menu");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

