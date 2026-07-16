package com.example.hellofx.pages;

import com.example.hellofx.Switch;
import com.example.hellofx.Task;
import com.example.hellofx.TaskClient;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsDashboard extends VBox {
    private PieChart taskStatusChart;
    private LineChart<String, Number> taskTrendChart;
    private BarChart<String, Number> categoryChart;
    HBox btnBox;

    public AnalyticsDashboard() {
        setPadding(new Insets(20));
        setSpacing(20);
    }

    private void initializeCharts() {
        // Task Status Distribution Chart
        taskStatusChart = new PieChart();
        taskStatusChart.setTitle("Task Status Distribution");

        // Task Trend Chart
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        taskTrendChart = new LineChart<>(xAxis, yAxis);
        taskTrendChart.setTitle("Task Completion Trend");

        // Category Distribution Chart
        final CategoryAxis catAxis = new CategoryAxis();
        final NumberAxis numAxis = new NumberAxis();
        categoryChart = new BarChart<>(catAxis, numAxis);
        categoryChart.setTitle("Tasks by Priority");

        GridPane chartsGrid = new GridPane();
        chartsGrid.add(taskStatusChart, 0, 0);
        chartsGrid.add(taskTrendChart, 1, 0);
        chartsGrid.add(categoryChart, 0, 1, 2, 1);
        chartsGrid.setHgap(10);
        chartsGrid.setVgap(10);

        getChildren().addAll(btnBox,new Label("Analytics Dashboard"), chartsGrid);
    }

    public void updateCharts() {
        List<Task> tasks = TaskClient.getTasks();

        // Update Status Chart
        Map<String, Long> statusCount = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        taskStatusChart.getData().clear();
        statusCount.forEach((status, count) ->
                taskStatusChart.getData().add(new PieChart.Data(status, count)));

        // Update Trend Chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        // Assuming Task::getDueDate returns a String
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<LocalDate, Long> completionTrend = tasks.stream()
                .filter(task -> "COMPLETED".equals(task.getStatus()))
                .collect(Collectors.groupingBy(task -> LocalDate.parse(task.getDueDate(), formatter), Collectors.counting()));
        completionTrend.forEach((date, count) ->
                series.getData().add(new XYChart.Data<>(date.toString(), count)));

        taskTrendChart.getData().clear();
        taskTrendChart.getData().add(series);

        // Update Category Chart
        XYChart.Series<String, Number> categorySeries = new XYChart.Series<>();
        Map<String, Long> categoryCount = tasks.stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));

        categoryCount.forEach((category, count) ->
                categorySeries.getData().add(new XYChart.Data<>(category, count)));

        categoryChart.getData().clear();
        categoryChart.getData().add(categorySeries);
    }

    // start method to initialize scene with a Stage
    public void start(Stage primaryStage) {
        // Initialize the page layout and scene
        Scene scene = new Scene(this, 800, 600);
//        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
         btnBox = new Switch().createNavigationButtons(primaryStage);

        initializeCharts();
        updateCharts();

        primaryStage.setTitle("Analytics Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
