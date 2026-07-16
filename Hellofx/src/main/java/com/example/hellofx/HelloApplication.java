package com.example.hellofx;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class HelloApplication extends Application {
    CurrentUser currentUser =null;

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private int userid;
    private String username;
    private String role; // User's role, extracted from the server response
    private String adminToken;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");
        primaryStage.getIcons().add(new Image(getClass().getResource("/icon.png").toString())); // Replace 'icon.png' with your image file name

        // Username and Password Fields
        usernameField.setPromptText("email");
        usernameField.setMaxWidth(300);
        usernameField.setStyle("-fx-padding: 10px; -fx-font-size: 14px;");

        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-padding: 10px; -fx-font-size: 14px;");

        // Buttons for Login and Signup
        Button loginButton = new Button("Login");
        loginButton.setStyle(
                "-fx-background-color: #4CAF50;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10px 20px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 5px;"
        );

        Button signupButton = new Button("Sign Up");
        signupButton.setStyle(
                "-fx-background-color: #2196F3;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10px 20px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 5px;"
        );

        // Event Handlers for Login and Signup
        loginButton.setOnAction(event -> handleLogin(primaryStage));
        signupButton.setOnAction(event -> handleSignup(primaryStage));

        // Layout for Buttons
        HBox buttonLayout = new HBox(10, loginButton, signupButton);
        buttonLayout.setAlignment(Pos.CENTER);

        // Layout for the entire form
        VBox formLayout = new VBox(20, usernameField, passwordField, buttonLayout);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setStyle(
                "-fx-padding: 30px;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-color: rgba(255, 255, 255, 0.8);"
        );

        // Root layout with background styling
        StackPane rootPane = new StackPane(formLayout);
        rootPane.setStyle(
                "-fx-background-image: url('img.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;" +
                        "-fx-background-repeat: no-repeat;"
        );

        Scene loginScene = new Scene(rootPane, 600, 700);
        primaryStage.setScene(loginScene);
        primaryStage.setResizable(true);
         handleAutoLogin(primaryStage);

    }

    private void handleLogin(Stage stage) {
        username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (!username.isEmpty() && !password.isEmpty()) {
            JSONObject requestBody = new JSONObject();
            requestBody.put("username", username);
            requestBody.put("password", password);

            boolean isAuthenticated = sendRequestToServer("/login", requestBody.toString());

            if (isAuthenticated) {
                navigateToHelloApplication(stage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter both username and password.");
        }
    }

    private void handleSignup(Stage stage) {
        username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Regular expression for validating email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (!username.isEmpty() && !password.isEmpty()) {
            if (!username.matches(emailRegex)) {
                showAlert(Alert.AlertType.WARNING, "Invalid Email", "The username must be a valid email address.");
                return;
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("username", username);
            requestBody.put("password", password);

            boolean isRegistered = sendRequestToServer("/signup", requestBody.toString());

            if (isRegistered) {
//                sendWelcomeEmail(username);
                navigateToHelloApplication(stage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Signup Failed", "Could not register user. Please try again.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter both username and password.");
        }
    }

    private boolean sendRequestToServer(String endpoint, String jsonBody) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/servletnew" + endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization","Bearer " + jsonBody)//handle if the user send token in autologin
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                currentUser =    CurrentUser.getInstance() ;
                JSONObject jsonResponse = new JSONObject(response.body());
                String token=null;

                if (jsonResponse.has("token")) {
                    token = jsonResponse.getString("token");
                    adminToken=token;
                    Token.getInstance().setToken(token);
                    updateAuthFile(token);
                }

                if (jsonResponse.has("id")) {
                    userid = jsonResponse.getInt("id");
                    currentUser.setId(userid);

                }

                else{
                    userid= (extractUserIdFromToken(token));
                }

                 if (jsonResponse.has("role")) {
                    role = jsonResponse.getString("role");
                }
                if (jsonResponse.has("username")) {
                    username = jsonResponse.getString("username"); // Extract the user's role
                    currentUser.setUsername(username);

                }

                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void navigateToHelloApplication(Stage stage) {
        if ("Admin".equalsIgnoreCase(role)) {
            showAdminPopup(stage);
        } else {
            goToHomePage(stage);
        }
    }
    // Decode the JWT token and extract the user ID
    private int extractUserIdFromToken(String token) {
        try {
            // JWT is in the format of <header>.<payload>.<signature>
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                // Decode the payload from Base64
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

                // Parse the payload as JSON
                JSONObject payloadJson = new JSONObject(payload);

                // Extract user ID from the payload
                if (payloadJson.has("id")) {
                    return payloadJson.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  // Handle the error if necessary
        }
        return 0; // Return null if user ID is not found
    }

    private void showAdminPopup(Stage stage) {
        Alert adminPopup = new Alert(Alert.AlertType.CONFIRMATION);
        adminPopup.setTitle("Admin Options");
        adminPopup.setHeaderText("Welcome, Admin!");
        adminPopup.setContentText("Where would you like to go?");

        ButtonType userPageButton = new ButtonType("Home Page");
        ButtonType adminPageButton = new ButtonType("Admin Page");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        adminPopup.getButtonTypes().setAll(userPageButton, adminPageButton, cancelButton);

        adminPopup.showAndWait().ifPresent(response -> {
            if (response == userPageButton) {
                goToHomePage(stage);
            } else if (response == adminPageButton) {
                goToAdminPage(stage);
            }
        });
    }

    private void goToHomePage(Stage sourceStage) {
        Home homePage = new Home(userid, username);

        // Create a new stage for the Home page
        Stage homeStage = new Stage();

        // Set the dimensions and position of the Home stage to match the source stage
        homeStage.setWidth(sourceStage.getWidth());
        homeStage.setHeight(sourceStage.getHeight());
        homeStage.setX(sourceStage.getX());
        homeStage.setY(sourceStage.getY());

        // Dynamically update Home stage dimensions and position when the source stage resizes or moves
        sourceStage.widthProperty().addListener((obs, oldWidth, newWidth) -> homeStage.setWidth(newWidth.doubleValue()));
        sourceStage.heightProperty().addListener((obs, oldHeight, newHeight) -> homeStage.setHeight(newHeight.doubleValue()));
        sourceStage.xProperty().addListener((obs, oldX, newX) -> homeStage.setX(newX.doubleValue()));
        sourceStage.yProperty().addListener((obs, oldY, newY) -> homeStage.setY(newY.doubleValue()));

        try {
            // Start the Home page using the Home stage
            homePage.start(homeStage);

            // Close the source stage if you want to switch completely
            sourceStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void goToAdminPage(Stage sourceStage) {
        Admin adminPage = new Admin(adminToken);

        // Create a new stage for the Admin page
        Stage adminStage = new Stage();

        // Set the dimensions and position of the Admin stage to match the source stage
        adminStage.setWidth(sourceStage.getWidth());
        adminStage.setHeight(sourceStage.getHeight());
        adminStage.setX(sourceStage.getX());
        adminStage.setY(sourceStage.getY());

        // Dynamically update Admin stage dimensions and position when the source stage resizes or moves
        sourceStage.widthProperty().addListener((obs, oldWidth, newWidth) -> adminStage.setWidth(newWidth.doubleValue()));
        sourceStage.heightProperty().addListener((obs, oldHeight, newHeight) -> adminStage.setHeight(newHeight.doubleValue()));
        sourceStage.xProperty().addListener((obs, oldX, newX) -> adminStage.setX(newX.doubleValue()));
        sourceStage.yProperty().addListener((obs, oldY, newY) -> adminStage.setY(newY.doubleValue()));

        try {
            // Start the Admin page using the Admin stage
            adminPage.start(adminStage);

            // Close the source stage if you want to switch completely
            sourceStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateAuthFile(String token) {
        String tokenFilePath = "C:\\Users\\MSI\\IdeaProjects\\Hellofx\\src\\main\\java\\com\\example\\hellofx\\auth.txt"; // Path to the auth file

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tokenFilePath, false))) { // false ensures overwrite mode
            writer.write(token); // Overwrite the file with the new token
            writer.flush(); // Ensure the data is written immediately
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAutoLogin(Stage stage) {
        boolean isAuthenticated=false;
        try {
            String tokenFilePath = "C:\\Users\\MSI\\IdeaProjects\\Hellofx\\src\\main\\java\\com\\example\\hellofx\\auth.txt";
            String token = new String(Files.readAllBytes(Paths.get(tokenFilePath)));
            if(token.length()>0)    //check if there's token
             isAuthenticated= sendRequestToServer("/autoLogin",token);
            if (isAuthenticated) {
                navigateToHelloApplication(stage);
            }else {
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendWelcomeEmail(String userEmail) {
        try {
            // Extract user name from the email address
            String userName = extractUserName(userEmail);

            // Construct the email's subject and body
            String subject = "Welcome to Our Service, " + userName + "!";
            String body = "Dear " + userName + ",\n\n"
                    + "Thank you for signing up for our platform! We are excited to have you on board.\n"
                    + "Feel free to explore and let us know if you have any questions.\n\n"
                    + "Best regards,\n"
                    + "The Team";

            // Attempt to send the email
            EmailSender.sendEmail(userEmail, subject, body);
            System.out.println("Welcome email sent successfully to " + userEmail);

        } catch (Exception e) {
            // Handle any exceptions during email sending
            System.err.println("Failed to send welcome email to " + userEmail);
            e.printStackTrace();
            // Optionally, log the error or retry email sending
        }
    }

    // Simulated helper method to extract the user's name from the email address
    private static String extractUserName(String email) {
        if (email == null || !email.contains("@")) {
            return "User"; // Default fallback name
        }
        return email.substring(0, email.indexOf('@')); // Extract name before '@'
    }


    public static void main(String[] args) {
        launch(args);
    }
}
