package com.example.hellofx;

import com.example.hellofx.utils.NotificationManager;
import com.example.hellofx.utils.TaskAnalytics;
import com.example.hellofx.utils.TaskExporter;
import com.example.hellofx.utils.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class Home extends Application {
    private int userId;
    private String username;

    public Home() {
        this.userId = CurrentUser.getInstance().getId();
        this.username = CurrentUser.getInstance().getUsername();
    }

    public Home(int id, String username) {
        this.userId = id;
        this.username = username;
    }
    private VBox rootLayout = new VBox(20);
    // UI Components
    private final TextField taskTitleField = new TextField();
    private final TextField taskSearchField = new TextField();
    private final TextArea taskDescriptionArea = new TextArea();
    private final DatePicker taskDueDatePicker = new DatePicker();
    private final ComboBox<String> priorityComboBox = new ComboBox<>();
    private final ComboBox<String> statusComboBox = new ComboBox<>();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> priorityFilter = new ComboBox<>();
    private final GridPane taskGrid = new GridPane();
    List<TaskAnalytics.Task> analyticsTasks;
    List<Task> tasks = null;
    private Task selectedTask = null; // To track selected tasks for updates

    @Override
    public void start(Stage primaryStage) {

        HBox btnBox = new Switch(userId, username).createNavigationButtons(primaryStage);

        Button exportPdfBtn = new Button("Export to PDF");
        exportPdfBtn.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-padding: 10px 20px;");
        exportPdfBtn.setOnAction(e -> exportTasksToPDF());

        ImageView pdf = createIcon("icons/pdf.png");


            exportPdfBtn.setGraphic(pdf);


        Button exportExcelBtn = new Button("Export to Excel");
        exportExcelBtn.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-padding: 10px 20px;");

        ImageView xls = createIcon("icons/xls.png");


        exportExcelBtn.setGraphic(xls);

        exportExcelBtn.setOnAction(e -> exportTasksToExcel());

        HBox exportBox = new HBox(10,exportExcelBtn,exportPdfBtn);



        // Theme toggle button
        Button themeToggleButton = new Button("Toggle Theme");
        themeToggleButton.setStyle("""
                -fx-background-color: #4285F4;
                -fx-text-fill: white;
                -fx-padding: 10px 20px;
                -fx-font-size: 14px;
                -fx-background-radius: 5px;
        """);

        // Create analytics container
        HBox analyticsContainer = new HBox(40);
        analyticsContainer.setStyle("""
                -fx-padding: 20;
                -fx-background-color: rgba(255,255,255,0.1);
                -fx-background-radius: 10;
        """);

        Label analyticsTitle = new Label("Task Analytics");
        analyticsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        analyticsContainer.getChildren().add(analyticsTitle);

        primaryStage.setTitle("Task Manager");
        primaryStage.getIcons().add(new Image(getClass().getResource("/icon.png").toString())); // Replace 'icon.png' with your image file name

        // Main layout

        rootLayout.setStyle("-fx-padding: 20px; -fx-background-color: #f8f8f8;");
//        rootLayout.setAlignment(Pos.CENTER);

        // Task Input Fields
        taskTitleField.setPromptText("Enter task title...");
        taskDescriptionArea.setPromptText("Enter task description...");
        taskDueDatePicker.setPromptText("Select due date");
// Task Search Fields
        taskSearchField.setPromptText("Enter Search text...");

        // Initialize ComboBoxes
        statusFilter.getItems().addAll("All", "Pending", "In Progress", "On Hold", "Cancelled", "Completed");
        priorityFilter.getItems().addAll("All", "High", "Medium", "Low");

        // Set default values
        statusFilter.setValue("All");
        priorityFilter.setValue("All");

        // Add listeners for filters
        taskSearchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        priorityFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Priority and Status ComboBoxes
        priorityComboBox.getItems().addAll("High", "Medium", "Low");

        statusComboBox.getItems().addAll("Pending", "In Progress", "On Hold", "Cancelled", "Completed");


        // Buttons
        Button addButton = new Button("Add Task");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px;");
        addButton.setOnAction(e -> addTask());

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-padding: 10px 20px;");
        logoutButton.setOnAction(e -> logout(primaryStage));


        // Task Grid for displaying tasks
        taskGrid.setHgap(10);
        taskGrid.setVgap(10);

        TaskClient.readTasks(userId);
        Task.notifyTasks(TaskClient.getTasks());
        String userEmail = username; // Replace with actual user email
//        OverdueTaskNotifier notifier = new OverdueTaskNotifier(userEmail);
//        notifier.startNotificationService();

        // Load tasks into the grid
        loadTasksFromServer();

        // Convert tasks to the format expected by TaskAnalytics
        analyticsTasks = TaskClient.getTasks().stream()
                .map(task -> {
                    TaskAnalytics.Task analyticsTask = new TaskAnalytics.Task();
                    analyticsTask.setStatus(task.getStatus());
                    analyticsTask.setCreationDate(task.getCreationDate());
                    return analyticsTask;
                })
                .collect(Collectors.toList());

        if (!analyticsTasks.isEmpty()) {
            PieChart statusChart = TaskAnalytics.createTaskStatusChart(analyticsTasks);
            BarChart<String, Number> trendChart = TaskAnalytics.createTaskTrendChart(analyticsTasks);

            statusChart.setMaxSize(400, 300);
            trendChart.setMaxSize(400, 300);

            analyticsContainer.getChildren().addAll(statusChart, trendChart);
        } else {
            Label noDataLabel = new Label("No tasks available for analytics");
            noDataLabel.setStyle("-fx-font-style: italic;");
            analyticsContainer.getChildren().add(noDataLabel);
        }

        // Input Layout as HBox for horizontal arrangement
        HBox middleLayout = new HBox(10,
                taskDueDatePicker,
                new Label("Priority : "),
                priorityComboBox,
                new Label("Status : "),
                statusComboBox
        );
        middleLayout.setAlignment(Pos.CENTER); // Align the elements to the center horizontally
        middleLayout.setSpacing(10); // Set spacing between elements
        // Input Layout
        VBox inputLayout = new VBox(
                10,
                taskTitleField,
                taskDescriptionArea,
                middleLayout,
                addButton
        );
        inputLayout.setAlignment(Pos.CENTER);

        // Assemble main layout

        Text welcomeText = extractUserName(username);
        welcomeText.setStyle("-fx-font-weight: bold;-fx-font-size: 25px;"); // Make the text bold
        HBox search_Status_priority_Box = createFilterBar();

//        addExportButtons(rootLayout);

        rootLayout.getChildren().addAll(
                btnBox,
//                welcomeText,
                analyticsContainer,
                exportBox,
                search_Status_priority_Box,
                taskGrid,
                inputLayout,
                themeToggleButton,
                logoutButton
        );
        if (isAdmin()) {
            Button switchToAdminButton = new Button("Switch to Admin Mode");
            switchToAdminButton.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-padding: 10px 20px;");
            switchToAdminButton.setOnAction(event -> switchToAdminPage(primaryStage));
            rootLayout.getChildren().add(switchToAdminButton);
        }


        // Wrapping the root layout in a ScrollPane for overall scrolling
        ScrollPane rootScrollPane = new ScrollPane(rootLayout);
        rootScrollPane.setFitToWidth(true);

        // Create scene and apply theme
        Scene scene = new Scene(rootScrollPane, 800, 800);
        ThemeManager.applyTheme(scene);

        themeToggleButton.setOnAction(e -> {
            ThemeManager.toggleTheme(scene);
            NotificationManager.showNotification(
                    primaryStage,
                    "Theme updated!",
                    NotificationManager.NotificationType.INFO
            );
        });

        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();


        // Welcome notification
        NotificationManager.showNotification(
                primaryStage,
                "Welcome back, " + username + "!",
                NotificationManager.NotificationType.SUCCESS
        );
    }

    private void loadTasksFromServer() {
        tasks = TaskClient.getTasks();
        loadTasksToGrid(tasks);
        applyFilters();
    }

    private void loadTasksToGrid(List<Task> tasks) {
        taskGrid.getChildren().clear(); // Clear the grid before loading tasks
        int row = 0;

        for (Task task : tasks) {
            VBox card = createTaskCard(task);
            taskGrid.add(card, row % 3, row / 3); // Arrange cards in a grid
            row++;
        }
    }

    private VBox createTaskCard(Task task) {
        VBox card = new VBox(10);

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Ensure the description is exactly 77 characters long
        String description = task.getDescription();
        if (description.length() < 77) {
            description = String.format("%-77s", description); // Pads with spaces to the right
        } else if (description.length() > 77) {
            description = description.substring(0, 77); // Truncate to the first 77 characters
        }

        Label descriptionLabel = new Label(description);

        Button detailsButton = new Button("View Details");
        detailsButton.setOnAction(e -> showTaskDetails(task));

        Button deleteButton = new Button("Delete Task");
        deleteButton.setStyle("-fx-background-color: #FF5733; -fx-text-fill: white; -fx-padding: 5px 10px;");
        deleteButton.setOnAction(e -> {
            boolean confirmed = showConfirmationDialog();
            if (confirmed) {
                removeTask(task);
            }
        });

        // Add status-based color styling
        switch (task.getStatus().toLowerCase()) {
            case "completed":
                card.setStyle("-fx-background-color: lightgreen; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");
                break;
            case "pending":
                card.setStyle("-fx-background-color: lightcoral; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");
                break;
            case "in progress":
                card.setStyle("-fx-background-color: lightblue; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");
                break;
            case "on hold":
                card.setStyle("-fx-background-color: lightgoldenrodyellow; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");
                break;
            case "cancelled":
                card.setStyle("-fx-background-color: lightgray; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");
                break;
            default:
                card.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");
                break;
        }

// Create an HBox to hold the buttons
        HBox buttonHBox = new HBox(10, detailsButton, deleteButton);
        buttonHBox.setSpacing(10); // Add some space between the buttons if desired

        // Optionally, style the HBox or buttons if needed
        buttonHBox.setStyle("-fx-alignment: center-right;");

        card.getChildren().addAll(titleLabel, descriptionLabel, buttonHBox);

        return card;
    }

    public HBox createFilterBar() {
        // Create labels
        Label statusLabel = new Label("Status:");
        Label priorityLabel = new Label("Priority:");
        Label searchLabel = new Label("Search:");

        // Add labels and controls to an HBox
        HBox filterBar = new HBox(20); // Spacing of 10 between elements
        filterBar.getChildren().addAll(
                searchLabel, taskSearchField,
                statusLabel, statusFilter,
                priorityLabel, priorityFilter
        );
        HBox.setHgrow(taskSearchField, Priority.ALWAYS);

        return filterBar;
    }

    private boolean showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Are you sure you want to delete this task?");
        alert.setContentText("This action cannot be undone.");

        ButtonType confirmButton = new ButtonType("Yes");
        ButtonType cancelButton = new ButtonType("No");
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        return alert.showAndWait().filter(response -> response == confirmButton).isPresent();
    }

    private void addTask() {
        String title = taskTitleField.getText();
        String description = taskDescriptionArea.getText();
        String dueDate = taskDueDatePicker.getValue() != null ? taskDueDatePicker.getValue().toString() : null;
        String priority = priorityComboBox.getValue();
        String status = statusComboBox.getValue();

        if (title != null && !title.isEmpty() && description != null && !description.isEmpty()
                && dueDate != null && priority != null && status != null) {
            TaskClient.createTask(title, description, dueDate, priority, status, userId);
            clearInputs();
            
            // Reload tasks and update UI
            tasks = TaskClient.getTasks();
            loadTasksToGrid(tasks);
            
            // Clear and update analytics
            HBox analyticsContainer = (HBox) rootLayout.getChildren().get(1);
            analyticsContainer.getChildren().clear();
            analyticsContainer.getChildren().add(new Label("Task Analytics"));
            updateTaskAnalytics(analyticsContainer);
            
            // Reapply filters
            applyFilters();
        } else {
            showAlert("Validation Error", "All fields must be filled.");
        }
    }

    private Text extractUserName(String username) {
        // Assuming 'username' is the full email address
        String email = username.trim(); // Ensure no extra spaces
        String displayName = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;

        return new Text("Welcome, " + displayName);
    }

    private void removeTask(Task task) {
        if (task != null) {
            TaskClient.deleteTask(task.getId(), userId);
            loadTasksFromServer();
            updateTaskAnalytics((HBox) rootLayout.getChildren().get(1)); // Update analytics after removing task
        }
    }

    private void showTaskDetails(Task task) {
        if (task != null) {
            selectedTask = task;

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
            Button updateButton = new Button("Update Task");
            updateButton.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");
            updateButton.setOnAction(e -> {
                if (!titleField.getText().isEmpty() && priorityComboBox.getValue() != null && statusComboBox.getValue() != null) {
                    // Update the task with new details
                    task.setTitle(titleField.getText());
                    task.setDescription(descriptionArea.getText());
                    LocalDate selectedDate = dueDatePicker.getValue();
                    task.setDueDate(selectedDate != null ? selectedDate.toString() : null);
                    task.setPriority(priorityComboBox.getValue());
                    task.setStatus(statusComboBox.getValue());

                    TaskClient.updateTask(
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            task.getDueDate(),
                            task.getPriority(),
                            task.getStatus(),
                            userId
                    );

                    loadTasksFromServer();
                    updateTaskAnalytics((HBox) rootLayout.getChildren().get(1)); // Update analytics after updating task
                    detailsStage.close();
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
            clearInputs();
        }
    }

    private void clearInputs() {
        taskTitleField.clear();
        taskDescriptionArea.clear();
        taskDueDatePicker.setValue(null);
        priorityComboBox.setValue(null);
        statusComboBox.setValue(null);
        selectedTask = null;
    }

    private void applyFilters() {
        String searchQuery = taskSearchField.getText().toLowerCase();
        String selectedStatus = statusFilter.getValue();
        String selectedPriority = priorityFilter.getValue();

        // Filter the tasks using a stream
        List<Task> filteredTasks = tasks.stream()
                .filter(task -> {
                    // Filter by search query
                    boolean matchesSearch = task.getTitle().toLowerCase().contains(searchQuery);

                    // Filter by status
                    boolean matchesStatus = selectedStatus.equals("All") ||
                            task.getStatus().equalsIgnoreCase(selectedStatus);

                    // Filter by priority
                    boolean matchesPriority = selectedPriority.equals("All") ||
                            task.getPriority().equalsIgnoreCase(selectedPriority);

                    return matchesSearch && matchesStatus && matchesPriority;
                })
                .collect(Collectors.toList());

        // Update the task view with the filtered tasks
        loadTasksToGrid(filteredTasks);
    }

    private ImageView createIcon(String iconPath) {
        try {
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            ImageView iconView = new ImageView(icon);
            iconView.setFitWidth(16); // Set desired icon width
            iconView.setFitHeight(16); // Set desired icon height
            return iconView;
        } catch (Exception e) {
            System.err.println("Icon not found: " + iconPath);
            return null;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logout(Stage currentStage) {
        try {
            clearAuthFile();
            // Delete the current token
            Token.getInstance().clearToken();
            CurrentUser.getInstance().resetUser();
            // Initialize the login application
            HelloApplication loginApp = new HelloApplication();

            Stage loginStage = new Stage();

            // Set the dimensions and position of the login stage to match the current stage
            loginStage.setWidth(currentStage.getWidth());
            loginStage.setHeight(currentStage.getHeight());
            loginStage.setX(currentStage.getX());
            loginStage.setY(currentStage.getY());

            // Dynamically update login stage dimensions and position when the current stage resizes or moves
            currentStage.widthProperty().addListener((obs, oldWidth, newWidth) -> loginStage.setWidth(newWidth.doubleValue()));
            currentStage.heightProperty().addListener((obs, oldHeight, newHeight) -> loginStage.setHeight(newHeight.doubleValue()));
            currentStage.xProperty().addListener((obs, oldX, newX) -> loginStage.setX(newX.doubleValue()));
            currentStage.yProperty().addListener((obs, oldY, newY) -> loginStage.setY(newY.doubleValue()));

            // Start the login application
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

    public boolean isAdmin() {
        try {
            // Extract payload from the token
            String[] parts = Token.getInstance().getToken().split("\\.");
            if (parts.length < 2) {
                return false; // Invalid token structure
            }

            // Decode the payload (Base64URL decoded JSON)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JSONObject jsonPayload = new JSONObject(payload);

            // Check the "role" field
            String role = jsonPayload.optString("role", "");
            return "Admin".equalsIgnoreCase(role);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void switchToAdminPage(Stage currentStage) {
        Admin adminPage = new Admin(Token.getInstance().getToken()); // Pass the token to the Admin page

        // Create a new stage for the Admin page
        Stage adminStage = new Stage();

        // Set dimensions and position to match the current stage
        adminStage.setWidth(currentStage.getWidth());
        adminStage.setHeight(currentStage.getHeight());
        adminStage.setX(currentStage.getX());
        adminStage.setY(currentStage.getY());

        // Dynamically update Admin stage dimensions and position when the current stage resizes or moves
        currentStage.widthProperty().addListener((obs, oldWidth, newWidth) -> adminStage.setWidth(newWidth.doubleValue()));
        currentStage.heightProperty().addListener((obs, oldHeight, newHeight) -> adminStage.setHeight(newHeight.doubleValue()));
        currentStage.xProperty().addListener((obs, oldX, newX) -> adminStage.setX(newX.doubleValue()));
        currentStage.yProperty().addListener((obs, oldY, newY) -> adminStage.setY(newY.doubleValue()));

        try {
            // Start the Admin page using the Admin stage
            adminPage.start(adminStage);

            // Close the current stage if switching completely
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTaskAnalytics(HBox analyticsContainer) {
        // Clear existing analytics
        analyticsContainer.getChildren().clear();
        Label analyticsTitle = new Label("Task Analytics");
        analyticsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        analyticsContainer.getChildren().add(analyticsTitle);

        // Get latest tasks and update analytics
        List<Task> currentTasks = TaskClient.getTasks();
        analyticsTasks = currentTasks.stream()
                .map(task -> {
                    TaskAnalytics.Task analyticsTask = new TaskAnalytics.Task();
                    analyticsTask.setStatus(task.getStatus());
                    analyticsTask.setCreationDate(task.getCreationDate());
                    return analyticsTask;
                })
                .collect(Collectors.toList());

        if (!analyticsTasks.isEmpty()) {
            PieChart statusChart = TaskAnalytics.createTaskStatusChart(analyticsTasks);
            BarChart<String, Number> trendChart = TaskAnalytics.createTaskTrendChart(analyticsTasks);

            statusChart.setMaxSize(400, 300);
            trendChart.setMaxSize(400, 300);

            analyticsContainer.getChildren().addAll(statusChart, trendChart);
        } else {
            Label noDataLabel = new Label("No tasks available for analytics");
            noDataLabel.setStyle("-fx-font-style: italic;");
            analyticsContainer.getChildren().add(noDataLabel);
        }
    }
    private void exportTasksToPDF() {
        try {
            String fileName = "tasks_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            String filePath = System.getProperty("user.home") + "/Downloads/" + fileName;
            TaskExporter.exportToPDF(TaskClient.getTasks(), filePath);
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", 
                     "Tasks have been exported to PDF", 
                     "File saved as: " + fileName + " in your Downloads folder");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", 
                     "Failed to export tasks to PDF", 
                     "Error: " + e.getMessage());
        }
    }

    private void exportTasksToExcel() {
        try {
            String fileName = "tasks_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
            String filePath = System.getProperty("user.home") + "/Downloads/" + fileName;
            TaskExporter.exportToExcel(TaskClient.getTasks(), filePath);
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", 
                     "Tasks have been exported to Excel", 
                     "File saved as: " + fileName + " in your Downloads folder");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", 
                     "Failed to export tasks to Excel", 
                     "Error: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
