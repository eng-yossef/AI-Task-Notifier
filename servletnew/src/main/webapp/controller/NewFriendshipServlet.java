package main.webapp.controller;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = "/friendship", loadOnStartup = 1)
public class NewFriendshipServlet extends HttpServlet {

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

    // Handle GET: List mutual friends
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = authenticate(request);
        if (userId == -1) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized\"}");
            return;
        }

        String action = request.getParameter("action");
        if ("mutual".equals(action)) {
            listMutualFriends(userId, response);
        } else {
            listFriends(userId, response);
        }
    }

    private void listFriends(int userId, HttpServletResponse response) throws IOException {
        String query = "SELECT DISTINCT u.id, u.username, f.friend_status " +
                "FROM users u " +
                "JOIN friendships f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? AND friend_status = 'accepted' " +
                "UNION " +
                "SELECT DISTINCT u.id, u.username, f.friend_status " +
                "FROM users u " +
                "JOIN friendships f ON u.id = f.user_id " +
                "WHERE f.friend_id = ? AND friend_status = 'accepted';";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[");
            while (rs.next()) {
                if (jsonBuilder.length() > 1) jsonBuilder.append(",");
                jsonBuilder.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
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

    private void listMutualFriends(int userId, HttpServletResponse response) throws IOException {
        String query = "SELECT DISTINCT mutual.id, mutual.username " +
                "FROM users mutual " +
                "WHERE mutual.id != ? " +
                "AND mutual.id NOT IN ( " +
                "    SELECT DISTINCT f.friend_id " +
                "    FROM friendships f " +
                "    WHERE (f.user_id = ? OR f.friend_id = ?) AND f.friend_status = 'accepted' " +
                ") " +
                "AND mutual.id IN ( " +
                "    SELECT DISTINCT friend_friends.mutual_id " +
                "    FROM ( " +
                "        SELECT DISTINCT f3.user_id AS mutual_id, f3.friend_id AS friend_id " +
                "        FROM friendships f3 WHERE f3.friend_status = 'accepted' " +
                "        UNION " +
                "        SELECT DISTINCT f4.friend_id AS mutual_id, f4.user_id AS friend_id " +
                "        FROM friendships f4 WHERE f4.friend_status = 'accepted' " +
                "    ) friend_friends " +
                "    JOIN ( " +
                "        SELECT DISTINCT f1.friend_id AS friend_id " +
                "        FROM friendships f1 WHERE f1.user_id = ? AND f1.friend_status = 'accepted' " +
                "        UNION " +
                "        SELECT DISTINCT f2.user_id AS friend_id " +
                "        FROM friendships f2 WHERE f2.friend_id = ? AND f2.friend_status = 'accepted' " +
                "    ) user_friends " +
                "    ON user_friends.friend_id = friend_friends.friend_id " +
                ");";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId); // Exclude the user themselves
            stmt.setInt(2, userId); // Exclude the user's existing friends
            stmt.setInt(3, userId); // Exclude the user's existing friends
            stmt.setInt(4, userId); // Match mutual friends
            stmt.setInt(5, userId); // Match mutual friends

            ResultSet rs = stmt.executeQuery();

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[");
            while (rs.next()) {
                if (jsonBuilder.length() > 1) jsonBuilder.append(",");
                jsonBuilder.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"username\":\"").append(rs.getString("username")).append("\"")
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

    // Handle DELETE: Remove a friendship
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = authenticate(request);
        if (userId == -1) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized\"}");
            return;
        }

        String friendIdParam = request.getParameter("friendId");
        if (friendIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Friend ID is required.\"}");
            return;
        }

        int friendId = Integer.parseInt(friendIdParam);
        String query = "DELETE FROM friendships " +
                "WHERE (user_id = ? AND friend_id = ?) " +
                "OR (user_id = ? AND friend_id = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setInt(4, userId);
            stmt.setInt(3, friendId);
            stmt.executeUpdate();
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
