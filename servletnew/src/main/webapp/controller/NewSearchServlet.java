package main.webapp.controller;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = "/search", loadOnStartup = 1)
public class NewSearchServlet extends HttpServlet {
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

    private static final long serialVersionUID = 1L;

    private static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;user=k3wan;password=123;encrypt=false;trustServerCertificate=true;";
    private static final String SEARCH_QUERY =
            "SELECT id, username FROM users WHERE username LIKE ? and id != ?";

    private Connection connection;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(CONNECTION_URL);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServletException("Failed to initialize database connection", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id=authenticate(request);
        response.setContentType("application/json");
        String searchTerm = request.getParameter("searchTerm");
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Search term is required.\"}");
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(SEARCH_QUERY)) {
            stmt.setString(1, "%" + searchTerm + "%");
            stmt.setInt(2,id);
            ResultSet resultSet = stmt.executeQuery();

            JSONArray searchResults = new JSONArray();
            while (resultSet.next()) {
                JSONObject user = new JSONObject();
                user.put("id", resultSet.getInt("id"));
                user.put("username", resultSet.getString("username"));
                searchResults.put(user);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(searchResults.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error.\"}");
        }
    }

    @Override
    public void destroy() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
