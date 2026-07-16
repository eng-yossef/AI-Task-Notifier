package com.example.hellofx.pages;

import com.example.hellofx.CurrentUser;
import com.example.hellofx.Switch;
import com.example.hellofx.Task;
import com.example.hellofx.TaskClient;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.DataFormat;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarView extends VBox {
    private final TaskClient taskClient = new TaskClient();
    private GridPane calendarGrid;
    private YearMonth currentYearMonth;
    private final VBox taskDetailsPane;
    private Scene scene;
    HBox btnBox;

    // Define a custom DataFormat for Task
    private static final DataFormat TASK_DATA_FORMAT = new DataFormat("com.example.hellofx.Task");

    public CalendarView() {
        this.currentYearMonth = YearMonth.now();
        this.taskDetailsPane = new VBox(10);

        setPadding(new Insets(20));
        setSpacing(10);
    }

    private void initializeCalendar() {
        // Ensure btnBox is initialized before adding it to the layout
        if (btnBox == null) {
            btnBox = new Switch().createNavigationButtons(new Stage());
        }

        // Navigation controls
        HBox navigation = new HBox(10);
        Button prevMonth = new Button("←");
        Button nextMonth = new Button("→");
        Label monthLabel = new Label(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        monthLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        prevMonth.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            monthLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            updateCalendar();
        });

        nextMonth.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            monthLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            updateCalendar();
        });

        navigation.getChildren().addAll(prevMonth, monthLabel, nextMonth);
        navigation.setSpacing(20);
        navigation.setStyle("-fx-alignment: center;");

        // Calendar grid
        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");

        // Ensure calendar grid takes full width/height
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);
        calendarGrid.prefWidthProperty().bind(widthProperty());
        calendarGrid.prefHeightProperty().bind(heightProperty().multiply(0.8));

        // Add constraints for grid columns and rows
        for (int i = 0; i < 7; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 7);
            calendarGrid.getColumnConstraints().add(column);
        }
        for (int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / 6);
            calendarGrid.getRowConstraints().add(row);
        }

        // Day labels
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setStyle("-fx-font-weight: bold;");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setMaxHeight(Double.MAX_VALUE);
            dayLabel.setStyle("-fx-alignment: center;");
            calendarGrid.add(dayLabel, i, 0);
        }

        // Add all components to the main VBox
        getChildren().addAll(btnBox, navigation, calendarGrid, new Separator(), taskDetailsPane);
    }

    private void updateCalendar() {
        // Clear previous calendar
        calendarGrid.getChildren().clear();
        // Get all tasks for the month
        List<Task> monthTasks = TaskClient.getTasks().stream()
                .filter(task -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Adjust pattern if necessary

                    LocalDate taskDate = LocalDate.parse(task.getDueDate(), formatter);
                    return taskDate.getYear() == currentYearMonth.getYear()
                            && taskDate.getMonth() == currentYearMonth.getMonth();
                })
                .toList();
        // Fill calendar with dates
        int day = 1;
        int monthLength = currentYearMonth.lengthOfMonth();

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                if (day <= monthLength) {
                    VBox dayCell = createDayCell(day, monthTasks);
                    calendarGrid.add(dayCell, j, i + 1);
                    day++;
                }
            }
        }
    }

    private VBox createDayCell(int day, List<Task> tasks) {
        VBox cell = new VBox(5);
        cell.setPadding(new Insets(5));

        // Default background color for day cells
        cell.setBackground(new Background(new BackgroundFill(
                Color.LIGHTGRAY.deriveColor(1, 1, 1, 0.2),
                new CornerRadii(5),
                Insets.EMPTY
        )));

        Label dateLabel = new Label(String.valueOf(day));
        cell.getChildren().add(dateLabel);

        // Add tasks for this day
        String cellDate = currentYearMonth.atDay(day).toString();

        tasks.stream()
                .filter(task -> task.getDueDate().equals(cellDate))
                .forEach(task -> {
                    Label taskLabel = new Label(task.getTitle());
                    taskLabel.setStyle("-fx-background-color: " + getStatusColor(task.getStatus()) + ";");
                    cell.getChildren().add(taskLabel);
                });

        // Set background color of the day based on task status
        String cellBackgroundColor = determineDayBackgroundColor(tasks);
        cell.setBackground(new Background(new BackgroundFill(
                Color.web(cellBackgroundColor),
                new CornerRadii(5),
                Insets.EMPTY
        )));

        // Drag-and-Drop handlers
        cell.setOnDragOver(e -> {
            if (e.getDragboard().hasContent(TASK_DATA_FORMAT)) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
        });

        cell.setOnDragDropped(e -> {
            if (e.getDragboard().hasContent(TASK_DATA_FORMAT)) {
                Task droppedTask = (Task) e.getDragboard().getContent(TASK_DATA_FORMAT);
                if (droppedTask != null) {
                    droppedTask.setDueDate(cellDate); // Assuming cellDate is defined
                    TaskClient.updateTask(
                            droppedTask.getId(),
                            droppedTask.getTitle(),
                            droppedTask.getDescription(),
                            droppedTask.getDueDate(),
                            droppedTask.getPriority(),
                            droppedTask.getStatus(),
                            CurrentUser.getInstance().getId()
                    );
                    updateCalendar(); // Refresh calendar UI after update
                }
                e.setDropCompleted(true);
            } else {
                e.setDropCompleted(false);
            }
        });

        return cell;
    }

    private String determineDayBackgroundColor(List<Task> tasks) {
        // Check task statuses and prioritize colors
        if (tasks.stream().anyMatch(task -> "Cancelled".equals(task.getStatus()))) {
            return "#D3D3D3"; // Light grey for cancelled tasks
        } else if (tasks.stream().anyMatch(task -> "On Hold".equals(task.getStatus()))) {
            return "#FFD700"; // Gold for on hold tasks
        } else if (tasks.stream().anyMatch(task -> "In Progress".equals(task.getStatus()))) {
            return "#FFB347"; // Light orange for in-progress tasks
        } else if (tasks.stream().anyMatch(task -> "Pending".equals(task.getStatus()))) {
            return "#ADD8E6"; // Light blue for pending tasks
        } else if (tasks.stream().anyMatch(task -> "Completed".equals(task.getStatus()))) {
            return "#90EE90"; // Light green for completed tasks
        } else {
            return "#E8E8E8"; // Default gray if no task
        }
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "Completed" -> "#32CD32"; // Lime green for completed tasks
            case "In Progress" -> "#FFA500"; // Orange for in-progress tasks
            case "Pending" -> "#ADD8E6"; // Light blue for pending tasks
            case "On Hold" -> "#FF6347"; // Tomato red for on hold tasks
            case "Cancelled" -> "#DCDCDC"; // Gainsboro grey for cancelled tasks
            default -> "#E8E8E8"; // Default gray if status is unknown
        };

    }

    // start method to initialize scene with a Stage
    public void start(Stage primaryStage) {
        // Initialize the page layout and scene
        scene = new Scene(this, 800, 600);
//        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        btnBox = new Switch().createNavigationButtons(primaryStage);

        initializeCalendar();
        updateCalendar();
        primaryStage.setTitle("Calendar View");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
