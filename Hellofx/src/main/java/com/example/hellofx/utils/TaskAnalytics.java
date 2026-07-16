package com.example.hellofx.utils;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskAnalytics {
    
    public static PieChart createTaskStatusChart(List<Task> tasks) {
        Map<String, Long> statusCount = tasks.stream()
            .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        statusCount.forEach((status, count) -> 
            pieChartData.add(new PieChart.Data(status, count))
        );

        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Task Status Distribution");
        return chart;
    }

    public static BarChart<String, Number> createTaskTrendChart(List<Task> tasks) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        xAxis.setLabel("Last 7 Days");
        yAxis.setLabel("Number of Tasks");
        barChart.setTitle("Task Creation Trend");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks Created");

        // Group tasks by date for the last 7 days
        Map<LocalDate, Long> tasksByDate = tasks.stream()
            .filter(task -> task.getCreationDate() != null &&
                          task.getCreationDate().isAfter(LocalDate.now().minusDays(7)))
            .collect(Collectors.groupingBy(Task::getCreationDate, Collectors.counting()));

        // Add data points
        tasksByDate.forEach((date, count) ->
            series.getData().add(new XYChart.Data<>(date.toString(), count))
        );

        barChart.getData().add(series);
        return barChart;
    }

    public static class Task {
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

        public static void notifyTasks(List<com.example.hellofx.Task> tasks) {
            Thread notifierThread = new Thread(() -> {
                while (true) {
                    try {
                        LocalDate today = LocalDate.now();

                        for (com.example.hellofx.Task task : tasks) {
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

        private static void showNotification(com.example.hellofx.Task task) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Task Due Reminder");
            alert.setHeaderText("Task Due!");
            alert.setContentText("The task \"" + task.getTitle() + "\" is due now!");
            alert.showAndWait();
        }

        // Override the toString() method to return the title
        @Override
        public String toString() {
            return getTitle() +" ("+getStatus()+")";  // Return the task title when the task is displayed in the ListView
        }
    }

}
