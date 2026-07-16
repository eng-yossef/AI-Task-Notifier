package com.example.hellofx;

import jakarta.mail.MessagingException;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Admin extends Application {
    private String token;

    // Constructor to accept the token
    public Admin() {}

    public Admin(String token) {
        this.token = token;
    }

    private TableView<JSONObject> tableView;

    @Override
    public void start(Stage primaryStage) {
        tableView = new TableView<>();
        primaryStage.getIcons().add(new Image(getClass().getResource("/icon.png").toString())); // Replace 'icon.png' with your image file name


        // Table columns
        TableColumn<JSONObject, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getInt("id"))));
        idColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<JSONObject, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("username")));
        usernameColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<JSONObject, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("role")));
        roleColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<JSONObject, String> taskCountColumn = new TableColumn<>("Task Count");
        taskCountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getInt("taskCount"))));
        taskCountColumn.setStyle("-fx-alignment: CENTER;");



        // Add "Send Task" button column
        TableColumn<JSONObject, Void> sendTaskColumn = new TableColumn<>("Send Task");
        sendTaskColumn.setStyle("-fx-alignment: CENTER;");

        sendTaskColumn.setCellFactory(column -> new TableCell<>() {
            private final Button sendTaskButton = new Button("Send Task");

            {
                sendTaskButton.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");
                sendTaskButton.setMaxWidth(Double.MAX_VALUE); // Allow the button to stretch
                sendTaskButton.setOnAction(event -> {
                    JSONObject user = getTableView().getItems().get(getIndex());
                    String username = user.getString("username");
                    showTaskDetailsPopup(username,user.getInt("id"));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(sendTaskButton);
                }
            }
        });



        tableView.getColumns().add(idColumn);
        tableView.getColumns().add(usernameColumn);
        tableView.getColumns().add(roleColumn);
        tableView.getColumns().add(taskCountColumn);
        tableView.getColumns().add(sendTaskColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Ensure all columns resize proportionally


        // Load users when the page is loaded
        loadUsers();

        // Delete button
        Button deleteButton = new Button("Delete Selected User");
        deleteButton.setOnAction(event -> deleteUser());

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> logout(primaryStage));

        // Switch to User button
        Button switchToUserButton = new Button("Switch to User");
        switchToUserButton.setOnAction(event -> switchToUser(primaryStage));

        // Layout
        HBox buttonLayout = new HBox(100,  switchToUserButton,deleteButton,logoutButton);
        buttonLayout.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(tableView, buttonLayout);

        // Create a responsive layout with scene resize
        Scene scene = new Scene(layout, 800, 600);
        scene.widthProperty().addListener((obs, oldWidth, newWidth) -> adjustTableColumnWidths(newWidth.doubleValue()));
        scene.heightProperty().addListener((obs, oldHeight, newHeight) -> adjustTableColumnWidths(scene.getWidth()));

        primaryStage.setTitle("Admin Page");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    // Method to adjust table column widths based on scene size
    private void adjustTableColumnWidths(double width) {
        if (width > 600) {
            tableView.setPrefWidth(width - 50);  // Adjust width dynamically
        }
    }
    private  void loadUsers() {
        try {
            // Send GET request to /admin to fetch all users with the Authorization header
            URL url = new URL("http://localhost:8080/servletnew/admin");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Set the Authorization header with the token
            connection.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse the response into JSON array and update the table
            JSONArray users = new JSONArray(response.toString());
            tableView.getItems().clear();
            for (int i = 0; i < users.length(); i++) {
                tableView.getItems().add(users.getJSONObject(i));
            }

        } catch (Exception e) {
            showAlert("Error", "Error loading users.");
            e.printStackTrace();
        }
    }
    private void deleteUser() {
        // Get the selected user
        JSONObject selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            int userId = selectedUser.getInt("id");

            try {
                // Send DELETE request to /admin with the user ID to delete
                URL url = new URL("http://localhost:8080/servletnew/admin?id=" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                // Set the Authorization header with the token
                connection.setRequestProperty("Authorization", "Bearer " + token);

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("message").contains("successfully")) {
                    loadUsers(); // Reload the list of users
                    showAlert("Success", "User deleted successfully.");
                } else {
                    showAlert("Error", "Failed to delete user.");
                }

            } catch (Exception e) {
                showAlert("Error", "Error deleting user.");
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "No user selected.");
        }
    }

    // Logout method - clears the token and closes the admin window
    private void logout(Stage currentStage) {
        try {
            clearAuthFile();
            // Delete the current token
            Token.getInstance().clearToken();
            // Initialize the login application
            HelloApplication loginApp = new HelloApplication();
            Stage loginStage = new Stage();

            // Set dimensions and position of the login stage to match the current stage
            loginStage.setWidth(currentStage.getWidth());
            loginStage.setHeight(currentStage.getHeight());
            loginStage.setX(currentStage.getX());
            loginStage.setY(currentStage.getY());

            // Dynamically synchronize the login stage dimensions and position with the current stage
            currentStage.widthProperty().addListener((obs, oldWidth, newWidth) -> loginStage.setWidth(newWidth.doubleValue()));
            currentStage.heightProperty().addListener((obs, oldHeight, newHeight) -> loginStage.setHeight(newHeight.doubleValue()));
            currentStage.xProperty().addListener((obs, oldX, newX) -> loginStage.setX(newX.doubleValue()));
            currentStage.yProperty().addListener((obs, oldY, newY) -> loginStage.setY(newY.doubleValue()));

            // Start the login stage
            loginApp.start(loginStage);

            // Close the current stage
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void clearAuthFile() {
        String tokenFilePath = "C:\\Users\\MSI\\IdeaProjects\\Hellofx\\src\\main\\java\\com\\example\\hellofx\\auth.txt"; // Path to the auth file

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tokenFilePath, false))) { // false ensures overwrite mode
            writer.write(""); // Write an empty string to clear the file
            writer.flush(); // Ensure the file is immediately cleared
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Switch to user view
    private void switchToUser(Stage primaryStage) {
        Home homePage = new Home(1, "admin");

        // Create a new stage for the Home page
        Stage homeStage = new Stage();

        // Set the dimensions and position of the Home stage to match the primary stage
        homeStage.setWidth(primaryStage.getWidth());
        homeStage.setHeight(primaryStage.getHeight());
        homeStage.setX(primaryStage.getX());
        homeStage.setY(primaryStage.getY());

        // Dynamically update Home stage dimensions and position when primary stage resizes or moves
        primaryStage.widthProperty().addListener((obs, oldWidth, newWidth) -> homeStage.setWidth(newWidth.doubleValue()));
        primaryStage.heightProperty().addListener((obs, oldHeight, newHeight) -> homeStage.setHeight(newHeight.doubleValue()));
        primaryStage.xProperty().addListener((obs, oldX, newX) -> homeStage.setX(newX.doubleValue()));
        primaryStage.yProperty().addListener((obs, oldY, newY) -> homeStage.setY(newY.doubleValue()));

        try {
            // Start the Home page using the Home stage
            homePage.start(homeStage);

            // Close the primary stage if you want to switch completely
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // Method to show task details pop-up for a specific user
    private void showTaskDetailsPopup(String username,int id) {
        // Example task for demonstration purposes
        Task exampleTask = new Task(id,"", "", "", "Low", "Pending");
        // Call the existing method to display task details
        showTaskDetails(exampleTask,username, id);
    }

    private void showTaskDetails(Task task,String username,int id) {
        if (task != null) {
            Task selectedTask = task;

            // Create a new Stage for the pop-up window
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Task Details");

            // Layout for the pop-up
            VBox layout = new VBox(10);
            layout.setStyle("-fx-padding: 20px; -fx-background-color: #f9f9f9;");

            // Task detail input fields
            TextField titleField = new TextField(task.getTitle());
            titleField.setPromptText("Task Title");

            TextArea descriptionArea = new TextArea(task.getDescription());
            descriptionArea.setPromptText("Task Description");
            descriptionArea.setWrapText(true);

            DatePicker dueDatePicker = new DatePicker();
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dueDate = LocalDate.parse(task.getDueDate(), formatter);
                dueDatePicker.setValue(dueDate);
            } catch (DateTimeParseException e) {
                dueDatePicker.setValue(null);
            }

            ComboBox<String> priorityComboBox = new ComboBox<>();
            priorityComboBox.getItems().addAll("High", "Medium", "Low");
            priorityComboBox.setValue(task.getPriority());

            ComboBox<String> statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll("Completed", "Pending", "In Progress", "On Hold", "Cancelled");
            statusComboBox.setValue(task.getStatus());

            // Update button to save changes
            Button updateButton = new Button("Send Task");
            updateButton.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");
            updateButton.setOnAction(e -> {


                // Optionally save the updated task to a database or list here
                if (!titleField.getText().isEmpty() && priorityComboBox.getValue() != null && statusComboBox.getValue() != null) {
                    // Update the task with new details
                    task.setTitle(titleField.getText());
                    task.setDescription(descriptionArea.getText());
                    LocalDate selectedDate = dueDatePicker.getValue();
                    task.setDueDate(selectedDate != null ? selectedDate.toString() : null);
                    task.setPriority(priorityComboBox.getValue());
                    task.setStatus(statusComboBox.getValue());

                     createTask(
                           task.getTitle(),
                            task.descriptionProperty().getValue(),
                            task.dueDateProperty().getValue(),
                            task.priorityProperty().getValue(),
                            task.statusProperty().getValue()
                             , id);
                    loadUsers();
                     // Close the pop-up
                    detailsStage.close();
                    sendTaskEmail(task,username);

                } else {
                    showAlert("Validation Error", "All fields must be filled.");
                }
            });

            // Close button to close the pop-up
            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
            closeButton.setOnAction(e -> detailsStage.close());

            // Add elements to the layout
            layout.getChildren().addAll(
                    new Label("Title:"), titleField,
                    new Label("Description:"), descriptionArea,
                    new Label("Due Date:"), dueDatePicker,
                    new Label("Priority:"), priorityComboBox,
                    new Label("Status:"), statusComboBox,
                    new HBox(10, updateButton, closeButton) // Add buttons horizontally
            );

            // Center align
            layout.setAlignment(Pos.CENTER_LEFT);


            // Root layout with background styling
            StackPane rootPane = new StackPane(layout);
            rootPane.setStyle(
                    "-fx-background-image: url('img.png');" + // Replace with your image file name
                            "-fx-background-size: cover;" +          // Cover the entire window
                            "-fx-background-position: center center;" +
                            "-fx-background-repeat: no-repeat;"
            );

            // Set the scene for the pop-up
            Scene scene = new Scene(rootPane, 400, 500);
            detailsStage.setScene(scene);

            // Show the pop-up
            detailsStage.showAndWait();
        } else {
//            clearInputs();
        }
    }
    public static void sendTaskEmail(Task task, String username) {
        // Generate the subject
        String subject = "New Task Assigned: " + task.getTitle();

        // Generate the email body
        String body = "Hello " + extractUserName(username)  + ",\n\n"
                + "You have been assigned the following task:\n\n"
                + "Title: " + task.getTitle() + "\n"
                + "Description: " + task.getDescription() + "\n"
                + "Due Date: " + (task.getDueDate() != null ? task.getDueDate() : "No due date assigned") + "\n"
                + "Priority: " + task.getPriority() + "\n"
                + "Status: " + task.getStatus() + "\n\n"
                + "Please complete this task at your earliest convenience.\n\n"
                + "Best regards,\n"
                + "Your Task Management Team";

        // Send the email using the provided sendEmail method
        try {
            EmailSender.sendEmail(username, subject, body);
            System.out.println("Email sent successfully to " + username);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + username);
            e.printStackTrace();
        }
    }
    private static String extractUserName(String email) {
        if (email == null || !email.contains("@")) {
            return "User"; // Default fallback name
        }
        return email.substring(0, email.indexOf('@')); // Extract name before '@'
    }
    public static void createTask(
            String title,
            String description,
            String dueDate,
            String priority,
            String status,
            int userId
    ) {
        try {
            String token = Token.getInstance().getToken();

            URL url = new URL("http://localhost:8080/servletnew/admin");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);

            // Construct JSON body
            String jsonBody = String.format("""
            {
                "title": "%s",
                "description": "%s",
                "dueDate": "%s",
                "priority": "%s",
                "status": "%s",
                "userId": %d
            }
        """,
                    title != null ? title : "",
                    description != null ? description : "",
                    dueDate != null ? dueDate : "",
                    priority != null ? priority : "",
                    status != null ? status : "",
                    userId);

            System.out.println("Sending JSON body: " + jsonBody); // Debugging Log

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode); // Debugging Log

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line.trim());
                    }
                    System.out.println("Task created successfully: " + response);
                }
            } else {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line.trim());
                    }
                    System.err.println("Failed to create task. Server responded with: " + errorResponse);
                }
            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
