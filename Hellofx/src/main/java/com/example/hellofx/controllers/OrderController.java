package com.example.hellofx.controllers;




import com.example.hellofx.models.FoodItem;
import com.example.hellofx.utils.DataHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

public class OrderController {

//    @FXML
//    private ListView<FoodItem> orderList;

    @FXML
    private Button checkoutButton;

    @FXML
    private ListView<String> orderList; // Ensure this matches your FXML

    @FXML
    public void initialize() {
        // Ensure that orderList is initialized properly
        if (orderList != null) {
            orderList.getItems().addAll("Item 1", "Item 2", "Item 3"); // Example items
        } else {
            System.out.println("orderList is null!");
        }
    }
    @FXML
    public void goToCheckoutPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hellofx/views/checkout.fxml"));
            Stage stage = (Stage) checkoutButton.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Checkout");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
