package main.webapp.controller;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = "/friendrequest", loadOnStartup = 1)
public class NewFriendRequestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Database connection parameters
    private static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;user=k3wan;password=123;encrypt=false;trustServerCertificate=true;";

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

    // Utility method to authenticate user and return their ID
    private int authenticate(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " prefix
            try {
                SecretKey key = Secret.getKey(); // Get the consistent key
                return (int) Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .get("id");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1; // Invalid token
    }

    // Handle GET: View incoming friend requests
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = authenticate(request);
        if (userId == -1) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized\"}");
            return;
        }

        // Check if the "sent" parameter is present in the request
        String sentParam = request.getParameter("sent");
        String query;

        if (sentParam != null && sentParam.equalsIgnoreCase("true")) {
            // Query to fetch sent friend requests
            query = "SELECT f.friend_id AS user_id, u.username, f.friend_status FROM friendships f " +
                    "JOIN users u ON f.friend_id = u.id " +
                    "WHERE f.user_id = ? AND f.friend_status = 'pending'";
        } else {
            // Query to fetch received friend requests
            query = "SELECT f.user_id, u.username, f.friend_status FROM friendships f " +
                    "JOIN users u ON f.user_id = u.id " +
                    "WHERE f.friend_id = ? AND f.friend_status = 'pending'";
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[");
            while (rs.next()) {
                if (jsonBuilder.length() > 1) jsonBuilder.append(",");
                jsonBuilder.append("{")
                        .append("\"userId\":").append(rs.getInt("user_id")).append(",")
                        .append("\"username\":\"").append(rs.getString("username")).append("\",")
                        .append("\"status\":\"").append(rs.getString("friend_status")).append("\"")
                        .append("}");
            }
            jsonBuilder.append("]");

            response.setContentType("application/json");
            response.getWriter().write(jsonBuilder.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // Handle PUT: Accept or reject a friend request

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = authenticate(request);
        if (userId == -1) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized\"}");
            return;
        }

        StringBuilder requestBody = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        JSONObject jsonRequest = new JSONObject(requestBody.toString());
        int senderId = jsonRequest.getInt("userId"); // The user who sent the request
        String status = jsonRequest.getString("status"); // "accepted" or "rejected"

        // Check if the userId and senderId exist in the friendships table
        String checkQuery = "SELECT COUNT(*) FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, senderId);
            checkStmt.setInt(2, userId);
            checkStmt.setInt(3, userId);
            checkStmt.setInt(4, senderId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // If no rows are found, return an error response
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"message\": \"Friendship record not found for the specified users.\"}");
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // If friendship record exists, update the status
        String updateQuery = "UPDATE friendships SET friend_status = ? WHERE user_id = ? AND friend_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, status);
            stmt.setInt(2, senderId);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\": \"Friend request " + status + ".\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    @Override

    // Handle POST: Send a friend request
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = authenticate(request);
        if (userId == -1) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized\"}");
            return;
        }

        StringBuilder requestBody = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        JSONObject jsonRequest = new JSONObject(requestBody.toString());
        int friendId = jsonRequest.getInt("friendId");

        // Check if friendId exists in the database
        String checkFriendQuery = "SELECT COUNT(*) FROM users WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkFriendQuery)) {
            checkStmt.setInt(1, friendId);
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) == 0) {
                // friendId does not exist
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\": \"Friend ID does not exist\"}");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error\"}");
            return;
        }

        // Check if the friendship already exists
        String checkFriendshipQuery = "SELECT COUNT(*) FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkFriendshipQuery)) {
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, friendId);
            checkStmt.setInt(3, friendId);
            checkStmt.setInt(4, userId);
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                // Friendship already exists
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"message\": \"Friend request already sent or friendship exists\"}");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error\"}");
            return;
        }

        // Insert the friendship record
        String query = "INSERT INTO friendships (user_id, friend_id, friend_status) VALUES (?, ?, 'pending')";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.executeUpdate();
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"message\": \"Friend request sent\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Failed to send friend request\"}");
        }
    }



    // Handle DELETE: Cancel a pending friend request
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = authenticate(request);
        if (userId == -1) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized\"}");
            return;
        }

        StringBuilder requestBody = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        JSONObject jsonRequest = new JSONObject(requestBody.toString());
        int friendId = jsonRequest.getInt("friendId"); // The recipient of the friend request

        // Check if friendId exists in the database
        String checkFriendQuery = "SELECT COUNT(*) FROM users WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkFriendQuery)) {
            checkStmt.setInt(1, friendId);
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) == 0) {
                // friendId does not exist
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\": \"Friend ID does not exist\"}");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error\"}");
            return;
        }

        // Proceed with deleting the friendship
        String query = "DELETE FROM friendships WHERE ((user_id = ? AND friend_id = ?) " +
                "OR (user_id = ? AND friend_id = ?)) AND friend_status = 'pending'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setInt(3, friendId);
            stmt.setInt(4, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Friend request canceled.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\": \"No pending friend request found to cancel.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Failed to cancel friend request\"}");
        }
    }

}
