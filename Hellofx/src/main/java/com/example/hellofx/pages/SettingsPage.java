package com.example.hellofx.pages;

import com.example.hellofx.Switch;
import com.example.hellofx.utils.ThemeManager;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsPage extends VBox {
    private Scene scene;
    HBox btnBox;

    public SettingsPage(Scene scene) {
        this.scene = scene;

    }

    public SettingsPage() {

        // Default constructor if Scene is not pre-provided
    }

    public void start(Stage primaryStage) {
         btnBox = new Switch().createNavigationButtons(primaryStage);

        initializeUI();
        if (scene == null) {
            scene = new Scene(this, 800, 600); // Create a new scene with default dimensions
        }

        primaryStage.setTitle("Settings");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeUI() {

        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("settings-page");

        Label titleLabel = new Label("Settings");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Theme Settings
        VBox themeSection = createSection("Appearance");
        ToggleSwitch darkModeToggle = new ToggleSwitch("Dark Mode");
        darkModeToggle.setSelected(ThemeManager.isDarkMode());
        darkModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            ThemeManager.setDarkMode(newVal, scene);
        });
        themeSection.getChildren().add(darkModeToggle);

        // Notification Settings
        VBox notificationSection = createSection("Notifications");
        CheckBox emailNotifications = new CheckBox("Email Notifications");
        CheckBox pushNotifications = new CheckBox("Push Notifications");
        CheckBox soundNotifications = new CheckBox("Sound Notifications");
        notificationSection.getChildren().addAll(
                emailNotifications,
                pushNotifications,
                soundNotifications
        );

        // Privacy Settings
        VBox privacySection = createSection("Privacy");
        CheckBox showOnlineStatus = new CheckBox("Show Online Status");
        CheckBox showLastSeen = new CheckBox("Show Last Seen");
        privacySection.getChildren().addAll(showOnlineStatus, showLastSeen);

        // Data Settings
        VBox dataSection = createSection("Data Management");
        Button exportData = new Button("Export Data");
        Button importData = new Button("Import Data");
        Button clearData = new Button("Clear All Data");
        clearData.setStyle("-fx-background-color: #ff4444;");

        exportData.setOnAction(e -> handleExportData());
        importData.setOnAction(e -> handleImportData());
        clearData.setOnAction(e -> handleClearData());

        dataSection.getChildren().addAll(exportData, importData, clearData);

        getChildren().addAll(
                btnBox,
                titleLabel,
                themeSection,
                notificationSection,
                privacySection,
                dataSection
        );
    }

    private VBox createSection(String title) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Label sectionTitle = new Label(title);
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        section.getChildren().add(sectionTitle);
        return section;
    }

    private void handleExportData() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Data");
        alert.setHeaderText("Data Export");
        alert.setContentText("Your data has been exported successfully!");
        alert.showAndWait();
    }

    private void handleImportData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Import Data");
        alert.setHeaderText("Data Import");
        alert.setContentText("Are you sure you want to import data? This may override existing data.");
        alert.showAndWait();
    }

    private void handleClearData() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Clear Data");
        alert.setHeaderText("Clear All Data");
        alert.setContentText("Are you sure you want to clear all data? This action cannot be undone.");

        ButtonType clearButton = new ButtonType("Clear", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(clearButton, cancelButton);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == clearButton) {
                // Implement clear data functionality
            }
        });
    }

    // Custom ToggleSwitch component
    private static class ToggleSwitch extends StackPane {
        private final Label label = new Label();
        private final ToggleButton toggle = new ToggleButton();

        public ToggleSwitch(String text) {
            label.setText(text);

            toggle.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-color: #cccccc;"
            );
            toggle.setPrefWidth(40);

            HBox layout = new HBox(10);
            layout.setAlignment(Pos.CENTER_LEFT);
            layout.getChildren().addAll(toggle, label);

            getChildren().add(layout);
        }

        public boolean isSelected() {
            return toggle.isSelected();
        }

        public void setSelected(boolean selected) {
            toggle.setSelected(selected);
        }

        public BooleanProperty selectedProperty() {
            return toggle.selectedProperty();
        }
    }
}