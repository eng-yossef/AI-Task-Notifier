package main.webapp.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = "/admin", loadOnStartup = 1)
public class NewAdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;user=k3wan;password=123;encrypt=false;trustServerCertificate=true;";

    private static final String SELECT_USERS_QUERY =
            "SELECT id, username, role FROM users where role!='Admin'";

    private static final String DELETE_USER_QUERY =
            "DELETE FROM users WHERE id = ?";

    private static final String DELETE_USER_TASKS_QUERY =
            "DELETE FROM user_tasks WHERE user_id = ?";

    private static final String DELETE_TASKS_QUERY =
            "DELETE FROM tasks WHERE id IN (SELECT task_id FROM user_tasks WHERE user_id = ?)";

    private static final String COUNT_USER_TASKS_QUERY =
            "SELECT COUNT(*) FROM user_tasks WHERE user_id = ?";

    private Connection connection;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(CONNECTION_URL);
            System.out.println("Database connection established.");
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServletException("Failed to initialize database connection", e);
        }
    }

    @Override
    public void destroy() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to decode JWT and check role
    private boolean validateToken(String token) {
        try {
            // Decode the token (no signature verification in this simple case)
            DecodedJWT decodedJWT = JWT.decode(token);

            // Extract the role from the token's payload
            String role = decodedJWT.getClaim("role").asString();

            // Check if the role is Admin
            return "Admin".equals(role);
        } catch (Exception e) {
            return false;  // Invalid token
        }
    }

    // Handle GET to fetch all users and their task counts
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Missing or invalid token.\"}");
            return;
        }

        String token = authorizationHeader.substring(7); // Extract the token

        if (!validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized: Invalid token or insufficient role.\"}");
            return;
        }

        response.setContentType("application/json");
        JSONArray usersJson = new JSONArray();

        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USERS_QUERY);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String role = resultSet.getString("role");

                // Count the number of tasks for each user
                try (PreparedStatement countStmt = connection.prepareStatement(COUNT_USER_TASKS_QUERY)) {
                    countStmt.setInt(1, userId);
                    try (ResultSet countResultSet = countStmt.executeQuery()) {
                        if (countResultSet.next()) {
                            int taskCount = countResultSet.getInt(1);

                            // Add user details along with task count to the JSON response
                            JSONObject userJson = new JSONObject();
                            userJson.put("id", userId);
                            userJson.put("username", username);
                            userJson.put("role", role);
                            userJson.put("taskCount", taskCount);
                            usersJson.put(userJson);
                        }
                    }
                }
            }

            // Return all users and their task counts as JSON response
            response.getWriter().write(usersJson.toString());
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Error retrieving users.\"}");
            e.printStackTrace();
        }
    }

    // Handle DELETE to delete a specific user by ID and associated tasks
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("id");

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"User ID is required.\"}");
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Missing or invalid token.\"}");
            return;
        }

        String token = authorizationHeader.substring(7); // Extract the token

        if (!validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized: Invalid token or insufficient role.\"}");
            return;
        }

        try {
            // First, delete the tasks associated with the user
            // Delete tasks from the tasks table that are associated with this user
            try (PreparedStatement deleteTasksStmt = connection.prepareStatement(DELETE_TASKS_QUERY)) {
                deleteTasksStmt.setInt(1, Integer.parseInt(userId));
                deleteTasksStmt.executeUpdate();
            }
            try (PreparedStatement deleteUserTasksStmt = connection.prepareStatement(DELETE_USER_TASKS_QUERY)) {
                deleteUserTasksStmt.setInt(1, Integer.parseInt(userId));
                deleteUserTasksStmt.executeUpdate();
            }


            // Now delete the user
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_USER_QUERY)) {
                preparedStatement.setInt(1, Integer.parseInt(userId));

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("{\"message\": \"User and associated tasks deleted successfully.\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"message\": \"User not found.\"}");
                }
            }

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Error deleting user and associated tasks.\"}");
            e.printStackTrace();
        }
    }




    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Missing or invalid token.\"}");
            return;
        }

        String token = authorizationHeader.substring(7); // Extract the token

        if (!validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized: Invalid token or insufficient role.\"}");
            return;
        }

        response.setContentType("application/json");

        try {
            // Read the JSON request body
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            JSONObject jsonObject = new JSONObject(requestBody.toString());

            // Extract task details and user ID
            String title = jsonObject.getString("title");
            String description = jsonObject.optString("description", null); // Optional field
            String dueDate = jsonObject.optString("dueDate", null); // Optional field
            String priority = jsonObject.optString("priority", null); // Optional field
            String status = jsonObject.optString("status", null); // Optional field
            int userId = jsonObject.getInt("userId");

            // Insert the task into the tasks table
            String insertTaskQuery =
                    "INSERT INTO tasks (title, description, due_date, priority, status) VALUES (?, ?, ?, ?, ?)";
            String insertUserTaskQuery = "INSERT INTO user_tasks (user_id, task_id) VALUES (?, ?)";
            int taskId;

            try (PreparedStatement taskStmt = connection.prepareStatement(insertTaskQuery, Statement.RETURN_GENERATED_KEYS)) {
                taskStmt.setString(1, title);
                taskStmt.setString(2, description);
                if (dueDate != null) {
                    taskStmt.setDate(3, Date.valueOf(dueDate));
                } else {
                    taskStmt.setNull(3, Types.DATE);
                }
                taskStmt.setString(4, priority);
                taskStmt.setString(5, status);

                int rowsAffected = taskStmt.executeUpdate();
                if (rowsAffected == 0) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"message\": \"Failed to insert task.\"}");
                    return;
                }

                // Retrieve the generated task ID
                try (ResultSet generatedKeys = taskStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        taskId = generatedKeys.getInt(1);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().write("{\"message\": \"Failed to retrieve task ID.\"}");
                        return;
                    }
                }
            }

            // Link the task with the user in the user_tasks table
            try (PreparedStatement userTaskStmt = connection.prepareStatement(insertUserTaskQuery)) {
                userTaskStmt.setInt(1, userId);
                userTaskStmt.setInt(2, taskId);

                int rowsAffected = userTaskStmt.executeUpdate();
                if (rowsAffected == 0) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"message\": \"Failed to associate task with user.\"}");
                    return;
                }
            }

            // Respond with success message
            JSONObject successResponse = new JSONObject();
            successResponse.put("message", "Task created and associated with user successfully.");
            successResponse.put("taskId", taskId);
            response.getWriter().write(successResponse.toString());
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"An error occurred while processing the request.\"}");
            e.printStackTrace();
        }
    }

}
