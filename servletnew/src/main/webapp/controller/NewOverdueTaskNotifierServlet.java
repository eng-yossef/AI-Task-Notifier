//package main.webapp.controller;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//
//import java.sql.*;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@WebServlet(urlPatterns = "/overdueTaskNotifier", loadOnStartup = 1)
//public class NewOverdueTaskNotifierServlet extends HttpServlet {
//
//    private static final String CONNECTION_URL =
//            "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;user=k3wan;password=123;encrypt=false;trustServerCertificate=true;";
//    private static final String OVERDUE_TASKS_QUERY =
//            "SELECT tasks.id, tasks.title, tasks.due_date, tasks.status, users.username " +
//                    "FROM tasks " +
//                    "JOIN user_tasks ON tasks.id = user_tasks.task_id " +
//                    "JOIN users ON user_tasks.user_id = users.id " +
//                    "WHERE tasks.due_date < GETDATE() AND tasks.status != 'Completed'";
//
//    private ScheduledExecutorService scheduler;
//
//    @Override
//    public void init() throws ServletException {
//        super.init();
//        scheduler = Executors.newScheduledThreadPool(1);
//        scheduler.scheduleAtFixedRate(this::checkForOverdueTasks, 0, 6, TimeUnit.HOURS);//check every 6 hours
//        System.out.println("Overdue Task Notifier Service Started.");
//    }
//
//    private void checkForOverdueTasks() {
//        try (Connection connection = DriverManager.getConnection(CONNECTION_URL);
//             PreparedStatement preparedStatement = connection.prepareStatement(OVERDUE_TASKS_QUERY);
//             ResultSet resultSet = preparedStatement.executeQuery()) {
//
//            while (resultSet.next()) {
//                String taskTitle = resultSet.getString("title");
//                String dueDate = resultSet.getString("due_date");
//                String userEmail = resultSet.getString("username");
//
//                String emailBody = "Dear User,\n\n" +
//                        "You have an overdue task:\n" +
//                        "- Task: " + taskTitle + "\n" +
//                        "- Due Date: " + dueDate + "\n\n" +
//                        "Please complete it as soon as possible.\n\n" +
//                        "Best regards,\nTask Manager";
//
//                try {
//                    EmailSender.sendEmail(userEmail, "Overdue Task Notification", emailBody);
//                    System.out.println("Overdue email sent to: " + userEmail);
//                }catch (Exception e){}
////                catch (MessagingException e) {
////                    System.err.println("Failed to send email to " + userEmail + ": " + e.getMessage());
////                }
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Database error during overdue task check: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void destroy() {
//        if (scheduler != null && !scheduler.isShutdown()) {
//            scheduler.shutdown();
//        }
//        System.out.println("Overdue Task Notifier Service Stopped.");
//    }
//}
