package com.example.hellofx.utils;



import com.example.hellofx.models.FoodItem;
import java.util.List;
import java.util.ArrayList;

public class DataHandler {

    private static List<FoodItem> orderedItems = new ArrayList<>();
    private static List<FoodItem> menuItems = new ArrayList<>();

    static {
        menuItems.add(new FoodItem("Burger", 5.99));
        menuItems.add(new FoodItem("Pizza", 8.99));
        menuItems.add(new FoodItem("Pasta", 7.99));
        // Add more items here
    }

    public static List<FoodItem> getMenuItems() {
        return menuItems;
    }

    public static List<FoodItem> getOrderedItems() {
        return orderedItems;
    }

    public static void addToOrder(FoodItem item) {
        orderedItems.add(item);
    }
}
