package main.webapp.controller;


import io.jsonwebtoken.Jwts;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.util.Date;

@WebServlet(urlPatterns = "/signup", loadOnStartup = 1)
public class signUp extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Database connection parameters
    private static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;user=k3wan;password=123;encrypt=false;trustServerCertificate=true;";

    private static final String INSERT_USER_QUERY =
            "INSERT INTO users (username, password, role) OUTPUT INSERTED.id VALUES (?, ?, ?)";
    private static final String CHECK_EXISTENCE_QUERY =
            "SELECT COUNT(*) FROM users WHERE username = ?";

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

        // Check if the username already exists
        try (PreparedStatement checkStmt = connection.prepareStatement(CHECK_EXISTENCE_QUERY)) {
            checkStmt.setString(1, username);

            try (ResultSet resultSet = checkStmt.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    // Username already exists
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.getWriter().write("{\"message\": \"Username already exists.\"}");
                    return;
                }
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error during validation.\"}");
            e.printStackTrace();
            return;
        }

        // Hash the password using Bcrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Insert the new user into the database with a default role of "User"
        try (PreparedStatement insertStmt = connection.prepareStatement(INSERT_USER_QUERY)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, hashedPassword); // Store the hashed password
            insertStmt.setString(3, "User"); // Default role

            ResultSet rs = insertStmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt(1);

                SecretKey key = Secret.getKey(); // This gets the consistent key
                String jwt = Jwts.builder()
                        .setSubject(username)
                        .claim("role", "User") // Default role
                        .claim("id", userId)
                        .setIssuer("your-app")
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                        .signWith(key)
                        .compact();

                // Return JWT in response
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("message", "User registered successfully.");

                // Construct the email's subject and body
                String subject = "Welcome to Our Service, " + extractUserName(username) + "!";
                String body = "Dear " + extractUserName(username) + ",\n\n"
                        + "Thank you for signing up for our platform! We are excited to have you on board.\n"
                        + "Feel free to explore and let us know if you have any questions.\n\n"
                        + "Best regards,\n"
                        + "K3wan Team";

                // Attempt to send the email
                EmailSender.sendEmail(username, subject, body);
                jsonResponse.put("token", jwt);
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(jsonResponse.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"message\": \"Failed to register user.\"}");
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error during insertion.\"}");
            e.printStackTrace();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
//        catch (MessagingException m){
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            response.getWriter().write("{\"message\": \"Failed to send welcome email+\"}");
//
//        }
    }
    private static String extractUserName(String email) {
        if (email == null || !email.contains("@")) {
            return "User"; // Default fallback name
        }
        return email.substring(0, email.indexOf('@')); // Extract name before '@'
    }
}
