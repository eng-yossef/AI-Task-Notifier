//package com.example.hellofx;
//
//import javafx.application.Application;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//
//public class test extends Application {
//    @Override // Override the start method in the Application class
//    public void start(Stage primaryStage) {
//        Label label=new Label("Name");
//        TextField tf=new TextField();
//        Button btn=new Button("Show");
//
//        VBox sp=new VBox();
//        sp.setAlignment(Pos.CENTER);
//
//        sp.getChildren().addAll(
//                label,
//                tf,
//                btn
//        );
//        // Create rectangles
//
//        btn.setOnAction(e->{
//            String s=tf.getText();
//            label.setText(s);
//        });
//
//
//
//        // Create a scene and place it in the stage
//        Scene scene = new Scene(sp, 250, 150);
//        primaryStage.setTitle("ShowRectangle"); // Set the stage title
//        primaryStage.setScene(scene); // Place the scene in the stage
//        primaryStage.show(); // Display the stage
//    }
//}
