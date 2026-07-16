package com.example.hellofx.utils;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class NotificationManager {
    private static Popup currentNotification;

    public static void showNotification(Stage owner, String message, NotificationType type) {
        Platform.runLater(() -> {
            if (currentNotification != null) {
                currentNotification.hide();
            }

            Label notificationLabel = new Label(message);
            notificationLabel.setStyle(getStyleForType(type));
            notificationLabel.setWrapText(true);
            notificationLabel.setMaxWidth(300);

            StackPane container = new StackPane(notificationLabel);
            container.setStyle("-fx-background-radius: 5; -fx-padding: 10;");
            container.setPrefWidth(300);
            container.setMaxWidth(300);
            container.setMinHeight(50);

            Popup popup = new Popup();
            popup.getContent().add(container);
            popup.setAutoHide(true);

            currentNotification = popup;

            // Position the popup
            Scene scene = owner.getScene();
            double centerX = owner.getX() + scene.getWidth()/2 - 150;
            double topY = owner.getY() + 50;

            popup.show(owner, centerX, topY);

            // Auto hide after 3 seconds
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> popup.hide());
            delay.play();
        });
    }

    private static String getStyleForType(NotificationType type) {
        String baseStyle = """
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-padding: 10px;
                -fx-font-size: 14px;
                -fx-background-radius: 5;
                -fx-alignment: center;
                -fx-min-width: 200;
                """;

        return switch (type) {
            case SUCCESS -> String.format(baseStyle, "#4CAF50");
            case ERROR -> String.format(baseStyle, "#f44336");
            case WARNING -> String.format(baseStyle, "#ff9800");
            case INFO -> String.format(baseStyle, "#2196F3");
        };
    }

    public enum NotificationType {
        SUCCESS, ERROR, WARNING, INFO
    }
}
