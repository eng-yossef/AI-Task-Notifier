package com.example.hellofx;

import com.example.hellofx.components.AIAssistantPanel;
import com.example.hellofx.utils.ThemeManager;
import com.example.hellofx.utils.NotificationManager;
import com.example.hellofx.utils.NotificationManager.NotificationType;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class SharedTasksPage extends Application {
    private VBox tasksContainer;
    private AIAssistantPanel aiAssistantPanel;
    private TaskClient taskClient;
    private List<Task> currentTasks;

    @Override
    public void start(Stage primaryStage) {
        // Initialize components
        taskClient = new TaskClient();
        aiAssistantPanel = new AIAssistantPanel();

        // Create navigation buttons
        HBox btnBox = new Switch().createNavigationButtons(primaryStage);

        // Add theme toggle button
        Button themeToggleButton = new Button("Toggle Theme");
        themeToggleButton.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-padding: 10px 20px;");
        themeToggleButton.setOnAction(e -> ThemeManager.toggleTheme(primaryStage.getScene()));

        // Create main content container
        VBox contentContainer = new VBox(20);
        contentContainer.setStyle("-fx-padding: 20;");

        // Add to layout
        contentContainer.getChildren().addAll(btnBox, themeToggleButton);

        // Create tasks section
        VBox tasksSection = createTasksSection();

        // Create split pane for tasks and AI assistant
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(tasksSection, aiAssistantPanel);
        splitPane.setDividerPositions(0.6);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // Add components to content container
        contentContainer.getChildren().addAll(splitPane);

        // Apply initial theme
        Scene scene = new Scene(contentContainer, 1200, 800);
        ThemeManager.applyTheme(scene);

        primaryStage.setTitle("Task Management with AI Assistant");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Show welcome notification
        NotificationManager.showNotification(
            primaryStage,
            "Welcome to Task Management with AI Assistant!",
            NotificationType.INFO
        );

        // Load tasks
        loadTasks();
    }

    private VBox createTasksSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white;");

        // Header
        Label headerLabel = new Label(extractUserName(CurrentUser.getInstance().getUsername())+" 's Tasks");

        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Tasks container
        tasksContainer = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(tasksContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        section.getChildren().addAll(headerLabel, scrollPane);
        return section;
    }

    private void loadTasks() {
        try {
            currentTasks = TaskClient.getTasks();
            updateTasksDisplay();

            // Update AI assistant with all tasks
            TaskClient.getTasks();
        } catch (Exception e) {
            showError("Error loading tasks: " + e.getMessage());
        }
    }

    private void updateTasksDisplay() {
        tasksContainer.getChildren().clear();

        if (currentTasks.isEmpty()) {
            Label emptyLabel = new Label("No tasks available");
            emptyLabel.setStyle("-fx-text-fill: #666;");
            tasksContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Task task : currentTasks) {
            HBox taskBox = createTaskBox(task);
            tasksContainer.getChildren().add(taskBox);
        }
    }

    private static String extractUserName(String email) {
        if (email == null || !email.contains("@")) {
            return "User"; // Default fallback name
        }
        return email.substring(0, email.indexOf('@')); // Extract name before '@'
    }

    private HBox createTaskBox(Task task) {
        HBox box = new HBox(10);
        box.setPadding(new Insets(10));
        box.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );

        // Task details
        VBox detailsBox = new VBox(5);
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold;");
        Label dateLabel = new Label("Due: " + task.getDueDate());
        dateLabel.setStyle("-fx-text-fill: #666;");
        detailsBox.getChildren().addAll(titleLabel, dateLabel);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        // Action buttons
        Button getAdviceBtn = new Button("Get AI Advice");
        getAdviceBtn.setStyle(
            "-fx-background-color: #007bff; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 3;"
        );
        getAdviceBtn.setOnAction(e -> {
            aiAssistantPanel.setCurrentTask(task);
            aiAssistantPanel.requestTaskAdvice(task);
        });

        box.getChildren().addAll(detailsBox, getAdviceBtn);
        return box;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}