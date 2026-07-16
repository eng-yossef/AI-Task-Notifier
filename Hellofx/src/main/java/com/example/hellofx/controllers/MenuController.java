package com.example.hellofx.controllers;
import com.example.hellofx.models.FoodItem;
import com.example.hellofx.utils.DataHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

public class MenuController {

    @FXML
    private ListView<FoodItem> menuList;

    @FXML
    private Button orderButton;

    // Initialize method to load menu items into the ListView
    @FXML
    public void initialize() {
        // Check if DataHandler has menu items and add them to the ListView
        if (DataHandler.getMenuItems() != null) {
            menuList.getItems().addAll(DataHandler.getMenuItems());
        }
    }



    // Method to navigate to the Order page
    @FXML
    public void goToOrderPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hellofx/views/order.fxml"));
            Stage stage = (Stage) orderButton.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Your Order");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
