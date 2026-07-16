package main.webapp.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = "/autoLogin", loadOnStartup = 1)
public class NewAutoLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Database connection parameters
    private static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;user=k3wan;password=123;encrypt=false;trustServerCertificate=true;";

    private static final String SELECT_USER_QUERY =
            "SELECT username FROM users WHERE id = ?";

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();

        try {
            // Extract JWT from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                jsonResponse.put("message", "Missing or invalid Authorization header");
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            System.out.println(authHeader);

            String jwt = authHeader.substring(7); // Remove "Bearer " prefix

            // Validate and parse the JWT
            SecretKey key = Secret.getKey(); // Ensure consistent key retrieval
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            // Extract user information from the JWT claims
            int userId = claims.get("id", Integer.class); // Extract id as an integer
            String role = claims.get("role", String.class); // Extract role as a string

            // Fetch username from the database using the extracted user ID
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_QUERY)) {
                preparedStatement.setInt(1, userId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // User found, retrieve username
                        String username = resultSet.getString("username");

                        // Prepare the response JSON
                        jsonResponse.put("id", userId);
                        jsonResponse.put("role", role);
                        jsonResponse.put("username", username);
                        jsonResponse.put("token", jwt); // Include the token

                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        // User not found
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.put("message", "User not found");
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("message", "Error processing request: " + e.getMessage());
            e.printStackTrace();
        }

        response.getWriter().write(jsonResponse.toString());
    }
}
