package main.webapp.controller;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt; // Add this library to your project

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.util.Date;

@WebServlet(urlPatterns = "/login", loadOnStartup = 1)
public class NewLogin extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Database connection parameters
    private static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;user=k3wan;password=123;encrypt=false;trustServerCertificate=true;";

    private static final String SELECT_USER_QUERY =
            "SELECT password, role, id FROM users WHERE username = ?";

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

        // Read the request body
        StringBuilder requestBody = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        // Parse the JSON body
        JSONObject jsonRequest = new JSONObject(requestBody.toString());
        String username = jsonRequest.getString("username");
        String password = jsonRequest.getString("password");

        // Validate the user by comparing the hashed password in the database
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_QUERY)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Retrieve the stored hashed password, role, and id
                    String storedHashedPassword = resultSet.getString("password");
                    String role = resultSet.getString("role");
                    int id = resultSet.getInt("id");

                    // Verify the provided password with the stored hashed password
                    if (BCrypt.checkpw(password, storedHashedPassword)) {
                        // Password matches, generate JWT
                        SecretKey key = Secret.getKey(); // This gets the consistent key
                        String jwt = Jwts.builder()
                                .setSubject(username)
                                .claim("role", role)  // Add role to JWT claims
                                .claim("id", id)      // Add id to JWT claims
                                .setIssuer("your-app")
                                .setIssuedAt(new Date())
                                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                                .signWith(key)
                                .compact();

                        // Send the JWT as a response along with role, id, and username
                        JSONObject jsonResponse = new JSONObject();
                        jsonResponse.put("token", jwt);
                        jsonResponse.put("role", role);   // Include role in the response
                        jsonResponse.put("id", id);       // Include id in the response
                        jsonResponse.put("username", username);  // Include username in the response
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write(jsonResponse.toString());
                    } else {
                        // Invalid password
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"message\": \"Invalid username or password.\"}");
                    }
                } else {
                    // Username not found
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"message\": \"Invalid username or password.\"}");
                }
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error.\"}");
            e.printStackTrace();
        }
    }
}
