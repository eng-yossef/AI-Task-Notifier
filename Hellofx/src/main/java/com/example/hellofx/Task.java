package com.example.hellofx;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Represents a task with a title, description, due date, priority, and status.
 */
public class Task {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty dueDate = new SimpleStringProperty();
    private final StringProperty priority = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private LocalDate creationDate;

    // Constructor
    public Task(int id, String title, String description, String dueDate, String priority, String status) {
        this();
        setId(id);
        setTitle(title);
        setDescription(description);
        setDueDate(dueDate);
        setPriority(priority);
        setStatus(status);
    }

    public Task(int id, String title, String description, String dueDate, String priority, String status,String creation ){
        this(id,title,description,dueDate,priority,status);
        // Parse it to a LocalDateTime

        LocalDateTime localDateTime = parseDateTime(creation);

        // Extract LocalDate
        LocalDate localDate = localDateTime.toLocalDate();
        this.setCreationDate(localDate);

    }

    public Task() {
        this.creationDate = LocalDate.now();
    }


    // Getter and Setter for id
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Getter and Setter for title
    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    // Getter and Setter for description
    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    // Getter and Setter for dueDate
    public String getDueDate() {
        return dueDate.get();
    }

    public void setDueDate(String dueDate) {
        this.dueDate.set(dueDate);
    }

    public StringProperty dueDateProperty() {
        return dueDate;
    }

    // Getter and Setter for priority
    public String getPriority() {
        return priority.get();
    }

    public void setPriority(String priority) {
        this.priority.set(priority);
    }

    public StringProperty priorityProperty() {
        return priority;
    }

    // Getter and Setter for status
    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public static void notifyTasks(List<Task> tasks) {
        Thread notifierThread = new Thread(() -> {
            while (true) {
                try {
                    LocalDate today = LocalDate.now();

                    for (Task task : tasks) {
                        try {
                            LocalDate taskDueDate = LocalDate.parse(task.getDueDate(), FORMATTER);
                            if (taskDueDate.isEqual(today)) {
                                Platform.runLater(() -> {
                                    showNotification(task);
                                });
                            }
                        } catch (Exception e) {
                            System.err.println("Invalid date format for task: " + task.getTitle());
                        }
                    }

                    Thread.sleep(24 * 60 * 60 * 1000); // Check once per day
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });

        notifierThread.setDaemon(true);
        notifierThread.start();
    }

    private static void showNotification(Task task) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task Due Reminder");
        alert.setHeaderText("Task Due!");
        alert.setContentText("The task \"" + task.getTitle() + "\" is due now!");
        alert.showAndWait();
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        // Define possible formats
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"), // Three fractional digits
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"),  // Two fractional digits
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"),  // one fractional digits
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")      // No fractional seconds
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                // Attempt to parse using the current formatter
                return LocalDateTime.parse(dateTimeString, formatter);
            } catch (DateTimeParseException ignored) {
                // Continue to next format if parsing fails
            }
        }

        // If no format matches, throw an exception
        throw new IllegalArgumentException("Invalid date-time format: " + dateTimeString);
    }


    // Override the toString() method to return the title
    @Override
    public String toString() {
        return getTitle() +" ("+getStatus()+")";  // Return the task title when the task is displayed in the ListView
    }
}
