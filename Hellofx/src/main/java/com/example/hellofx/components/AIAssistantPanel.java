package com.example.hellofx.components;

import com.example.hellofx.Task;
import com.example.hellofx.services.AIAssistantService;
import com.example.hellofx.utils.NotificationManager;
import com.example.hellofx.utils.NotificationManager.NotificationType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;

public class AIAssistantPanel extends VBox {
    private final ScrollPane chatScrollPane;
    private final VBox messagesContainer;
    private  TextField userInput;
    private  Button sendButton;
    private final VBox quickActionsBox;
    private Task currentTask;
    private List<Task> allTasks;

    public AIAssistantPanel() {
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");

        // Header
        Label headerLabel = new Label("AI Task Assistant");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        headerLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Messages Container
        messagesContainer = new VBox(10);
        messagesContainer.setStyle("-fx-padding: 10;");

        // Scroll Pane for Messages
        chatScrollPane = new ScrollPane(messagesContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setStyle(
            "-fx-background: transparent; " +
            "-fx-background-color: transparent; " +
            "-fx-padding: 10;"
        );
        chatScrollPane.setPrefHeight(400);
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        // Quick Actions
        quickActionsBox = createQuickActionsBox();

        // Input Area
        HBox inputBox = createInputBox();

        // Add components
        getChildren().addAll(headerLabel, chatScrollPane, quickActionsBox, inputBox);
    }

    private HBox createInputBox() {
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);

        userInput = new TextField();
        userInput.setPromptText("Ask anything about your tasks...");
        userInput.setPrefWidth(300);
        userInput.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 8 15;"
        );
        HBox.setHgrow(userInput, Priority.ALWAYS);

        sendButton = new Button("Send");
        sendButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 8 20;"
        );
        sendButton.setOnAction(e -> handleUserInput());

        userInput.setOnAction(e -> handleUserInput());

        inputBox.getChildren().addAll(userInput, sendButton);
        return inputBox;
    }

    private VBox createQuickActionsBox() {
        VBox actionsBox = new VBox(5);
        actionsBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label quickActionsLabel = new Label("Quick Actions");
        quickActionsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        quickActionsLabel.setStyle("-fx-text-fill: #2c3e50;");

        FlowPane buttonsPane = new FlowPane(10, 10);
        buttonsPane.setPrefWrapLength(300);

        Button taskAdviceBtn = createActionButton("🎯 Task Steps", () -> {
            if (currentTask != null) {
                requestTaskAdvice(currentTask);
            } else {
                showMessage("Please select a task first", "error");
            }
        });

        Button prioritizeBtn = createActionButton("📋 Prioritize Tasks", () -> {
            if (allTasks != null && !allTasks.isEmpty()) {
                requestTaskPrioritization(allTasks);
            } else {
                showMessage("No tasks available to prioritize", "error");
            }
        });

        Button timeManagementBtn = createActionButton("⏰ Time Management", () -> 
            requestGeneralAdvice("Please provide time management tips for handling multiple tasks effectively."));

        Button productivityTipsBtn = createActionButton("💡 Productivity Tips", () ->
            requestGeneralAdvice("What are some productivity techniques I can use to complete my tasks more efficiently?"));

        buttonsPane.getChildren().addAll(taskAdviceBtn, prioritizeBtn, timeManagementBtn, productivityTipsBtn);
        actionsBox.getChildren().addAll(quickActionsLabel, buttonsPane);
        return actionsBox;
    }

    private Button createActionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #e9ecef; " +
            "-fx-border-radius: 20; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 8 15;"
        );
        button.setOnAction(e -> action.run());
        button.setMaxWidth(Double.MAX_VALUE);

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: #e9ecef; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-radius: 20; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 8 15;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #e9ecef; " +
            "-fx-border-radius: 20; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 8 15;"
        ));

        return button;
    }

    private void appendMessage(String message, String type) {
        VBox messageBox = new VBox(5);
        messageBox.setPadding(new Insets(10, 15, 10, 15));
        messageBox.setMaxWidth(500);

        // Style based on message type
        String backgroundColor;
        String textColor;
        String alignment;
        String icon;

        switch (type.toLowerCase()) {
            case "user":
                backgroundColor = "#007bff";
                textColor = "white";
                alignment = "RIGHT";
                icon = "👤";
                break;
            case "ai":
                backgroundColor = "#ffffff";
                textColor = "#2c3e50";
                alignment = "LEFT";
                icon = "🤖";
                break;
            case "error":
                backgroundColor = "#dc3545";
                textColor = "white";
                alignment = "CENTER";
                icon = "⚠️";
                break;
            default:
                backgroundColor = "#6c757d";
                textColor = "white";
                alignment = "CENTER";
                icon = "ℹ️";
        }

        messageBox.setStyle(
                String.format(
                        "-fx-background-color: %s; " +
                                "-fx-background-radius: 15; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);",
                        backgroundColor
                )
        );

        // Add icon and format the message
        TextFlow textFlow = new TextFlow();
        Text iconText = new Text(icon + " ");
        iconText.setFont(Font.font("System", FontWeight.BOLD, 14));
        iconText.setFill(Color.web(textColor));

        // Format the message content with bold sections and bullet points
        String formattedMessage = formatMessage(message);

        // Split message by <b> tags to apply bold style where necessary
        String[] parts = formattedMessage.split("(<b>|</b>)");
        for (String part : parts) {
            if (part.equals("<b>") || part.equals("</b>")) {
                // Skip <b> and </b> tags, we don't need to add them to the TextFlow
                continue;
            }

            if (part.contains("<b>")) {
                // Text inside <b> tags, apply bold style
                Text boldText = new Text(part.replaceAll("<b>", "").replaceAll("</b>", ""));
                boldText.setFont(Font.font("System", FontWeight.BOLD, 14));
                boldText.setFill(Color.web(textColor));
                textFlow.getChildren().add(boldText);
            } else {
                // Regular text
                Text normalText = new Text(part);
                normalText.setFont(Font.font("System", FontWeight.NORMAL, 14));
                normalText.setFill(Color.web(textColor));
                textFlow.getChildren().add(normalText);
            }
        }

        messageBox.getChildren().add(textFlow);

        // Set alignment
        switch (alignment) {
            case "RIGHT":
                messagesContainer.setAlignment(Pos.CENTER_RIGHT);
                break;
            case "LEFT":
                messagesContainer.setAlignment(Pos.CENTER_LEFT);
                break;
            default:
                messagesContainer.setAlignment(Pos.CENTER);
        }

        Platform.runLater(() -> {
            messagesContainer.getChildren().add(messageBox);
            chatScrollPane.setVvalue(1.0);
        });
    }

    private String formatMessage(String message) {
        // Format numbered lists
        message = message.replaceAll("(?m)^(\\d+\\.)", "• ");

        // Format bullet points
        message = message.replaceAll("(?m)^-", "•");

        // Add spacing after bullet points
        message = message.replaceAll("(?m)^•", "\n• ");

        // Bold important phrases
        String[] phrasesToBold = {
                "Key steps:", "Time estimate:", "Priority:", "Important:", "Note:",
                "Steps:", "Considerations:", "Tips:", "Warning:", "Remember:"
        };

        // Apply bold formatting to important phrases by replacing **bold** syntax
        for (String phrase : phrasesToBold) {
            // Replace phrase with bolded version, using ** for bold in the message
            message = message.replace(phrase, "<b>" + phrase + "</b>");
        }

        // Remove markdown-style stars and convert to bold (for **bold** text)
        message = message.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

        return message;
    }

    public void setCurrentTask(Task task) {
        this.currentTask = task;
    }

    public void setAllTasks(List<Task> tasks) {
        this.allTasks = tasks;
    }

    private void handleUserInput() {
        String input = userInput.getText().trim();
        if (input.isEmpty()) return;

        appendMessage(input, "user");
        userInput.clear();
        sendButton.setDisable(true);

        new Thread(() -> {
            try {
                String response = AIAssistantService.getInstance().getGeneralAdvice(input);
                handleAIResponse(response);
            } catch (Exception e) {
                handleError(e);
            }
        }).start();
    }

    private void handleAIResponse(String response) {
        Platform.runLater(() -> {
            appendMessage(response, "ai");
            sendButton.setDisable(false);
            NotificationManager.showNotification(
                (javafx.stage.Stage) getScene().getWindow(),
                "AI response received",
                NotificationType.SUCCESS
            );
        });
    }

    private void handleError(Exception e) {
        Platform.runLater(() -> {
            appendMessage(e.getMessage(), "error");
            sendButton.setDisable(false);
            NotificationManager.showNotification(
                (javafx.stage.Stage) getScene().getWindow(),
                "Error processing AI request",
                NotificationType.ERROR
            );
        });
    }

    public void requestTaskAdvice(Task task) {
        sendButton.setDisable(true);
        appendMessage("Analyzing task: " + task.getTitle(), "system");

        new Thread(() -> {
            try {
                String response = AIAssistantService.getInstance().getTaskAdvice(task);
                handleAIResponse(response);
            } catch (Exception e) {
                handleError(e);
            }
        }).start();
    }

    public void requestTaskPrioritization(List<Task> tasks) {
        sendButton.setDisable(true);
        appendMessage("Analyzing your tasks...", "system");

        new Thread(() -> {
            try {
                String response = AIAssistantService.getInstance().getTaskPrioritization(tasks);
                handleAIResponse(response);
            } catch (Exception e) {
                handleError(e);
            }
        }).start();
    }

    private void requestGeneralAdvice(String query) {
        sendButton.setDisable(true);
        appendMessage(query, "user");

        new Thread(() -> {
            try {
                String response = AIAssistantService.getInstance().getGeneralAdvice(query);
                handleAIResponse(response);
            } catch (Exception e) {
                handleError(e);
            }
        }).start();
    }

    private void showMessage(String message, String type) {
        appendMessage(message, type);
    }
}
