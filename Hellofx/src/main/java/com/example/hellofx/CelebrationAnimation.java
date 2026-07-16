package com.example.hellofx;

import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class CelebrationAnimation {
    public static void showCelebrationAnimation(Stage stage) {
        Pane animationPane = new Pane();
        animationPane.setPrefSize(400, 500);

        Random random = new Random();
        int confettiCount = 50;

        for (int i = 0; i < confettiCount; i++) {
            // Create confetti circles
            Circle confetti = new Circle(5, Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
            confetti.setTranslateX(random.nextInt(400)); // Random horizontal start position
            confetti.setTranslateY(random.nextInt(100)); // Random vertical start position

            // Add confetti to the pane
            animationPane.getChildren().add(confetti);

            // Animate the confetti
            TranslateTransition fall = new TranslateTransition(Duration.seconds(2), confetti);
            fall.setByY(400 + random.nextInt(100)); // Fall to the bottom
            fall.setInterpolator(Interpolator.EASE_OUT);

            RotateTransition rotate = new RotateTransition(Duration.seconds(2), confetti);
            rotate.setByAngle(360);

            // Combine animations
            ParallelTransition animation = new ParallelTransition(fall, rotate);
            animation.setOnFinished(event -> animationPane.getChildren().remove(confetti)); // Remove after animation
            animation.play();
        }

        // Add animation pane to the stage
        StackPane root = (StackPane) stage.getScene().getRoot();
        root.getChildren().add(animationPane);

        // Remove animation pane after a delay
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> root.getChildren().remove(animationPane));
        delay.play();
    }

}
