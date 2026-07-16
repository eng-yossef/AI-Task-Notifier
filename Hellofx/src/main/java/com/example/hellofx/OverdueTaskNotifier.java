package com.example.hellofx;

import jakarta.mail.MessagingException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OverdueTaskNotifier {

    private final String userEmail;

    public OverdueTaskNotifier(String userEmail) {
        this.userEmail = userEmail;
    }

    public void startNotificationService() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        try {
            scheduler.scheduleAtFixedRate(this::checkForOverdueTasks, 0, 1, TimeUnit.HOURS);
            System.out.println("Overdue task notification service started.");
        } catch (Exception e) {
            System.err.println("Failed to start notification service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkForOverdueTasks() {
        List<Task> tasks = TaskClient.getTasks(); // Fetch all tasks
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("No tasks found for overdue notification.");
            return;
        }

        StringBuilder overdueTasks = new StringBuilder("You have overdue tasks:\n\n");
        boolean hasOverdue = false;

        for (Task task : tasks) {
            try {
                LocalDate dueDate = LocalDate.parse(task.getDueDate()); // Parse due date
                if (dueDate.isBefore(LocalDate.now()) && ! task.getStatus().equalsIgnoreCase("Completed")) {
                    overdueTasks.append("- ").append(task.getTitle())
                            .append(" (Due: ").append(task.getDueDate()).append(")\n");
                    hasOverdue = true;
                }
            } catch (DateTimeParseException e) {
                System.err.println("Invalid due date for task: " + task.getTitle() + ". Skipping.");
            } catch (NullPointerException e) {
                System.err.println("Task or due date is null. Skipping.");
            }
        }

        if (hasOverdue) {
            try {
                EmailSender.sendEmail(userEmail, "Overdue Task Notification", overdueTasks.toString());
                System.out.println("Overdue email sent successfully to: " + userEmail);
            } catch (MessagingException e) {
                System.err.println("Failed to send email to " + userEmail + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No overdue tasks to notify for user: " + userEmail);
        }
    }
}
