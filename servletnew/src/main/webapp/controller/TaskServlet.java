//package main.webapp.controller;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//@WebServlet("/employees")
//public class SQLServerConnection extends HttpServlet {
//    private static final long serialVersionUID = 1L;
//
//    // Database connection parameters
//    private static final String CONNECTION_URL = "jdbc:sqlserver://DESKTOP-O5U7QGL:1433;databaseName=TaskManagerDB;user=taskManagerUser;password=k3wan123;integratedSecurity=true;encrypt=false;trustServerCertificate=true;loginTimeout=30;";
//
//    // SQL queries
//    private static final String SELECT_QUERY = "SELECT * FROM employee order by newID()";
//    private static final String DELETE_QUERY = "DELETE FROM employee WHERE id = ?";
//    private static final String INSERT_QUERY = "INSERT INTO employee (Id, FirstName, LastName, SSN, SuperSSN, Salary, DepartmentNumber, Gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
//
//    // Database connection
//    private Connection connection;
//
//    @Override
//    public void init() throws ServletException {
//        super.init();
//        try {
//            // Load the SQL Server JDBC driver
//            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//            // Establish the connection
//            connection = DriverManager.getConnection(CONNECTION_URL);
//            System.out.println("Connection Established!!");
//        } catch (ClassNotFoundException | SQLException e) {
//            throw new ServletException("Failed to initialize database connection", e);
//        }
//    }
//
//    @Override
//    public void destroy() {
//        super.destroy();
//        if (connection != null) {
//            try {
//                //when the server closed
//                System.out.println("Connection Clossed!!");
//
//                connection.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
////        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
////        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
////        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
//
//        System.out.println("GE!!T");
//        response.setContentType("text/html");
//        PrintWriter out = response.getWriter();
//        List<Integer> ids = new ArrayList<>(); // Example data
//        List<String> fnames = new ArrayList<>(); // Example data
//        List<String> lnames = new ArrayList<>(); // Example data
//        List<Integer> salaries = new ArrayList<>(); // Example data
//
//        out.println("<html>");
//        out.println("<head><title>Employee Data</title></head>");
//        out.println("<body>");
//        out.println("<h1>Employee Data</h1>");
//        out.println("<table border='1'>");
//        out.println("<tr><th>ID</th><th>FirstName</th><th>LastName</th><th>SSN</th><th>SuperSSN</th><th>Salary</th><th>DepartmentNumber</th><th>Gender</th></tr>");
//
//        try (Statement statement = connection.createStatement();
//             ResultSet resultSet = statement.executeQuery(SELECT_QUERY)) {
//            while (resultSet.next()) {
//                int id = resultSet.getInt("Id");
//                String firstName = resultSet.getString("FirstName");
//                String lastName = resultSet.getString("LastName");
//                int ssn = resultSet.getInt("SSN");
//                int superSsn = resultSet.getInt("SuperSSN");
//                int salary = resultSet.getInt("Salary");
//                int departmentNumber = resultSet.getInt("DepartmentNumber");
//                String gender = resultSet.getString("Gender");
//                ids.add(id);
//                fnames.add(firstName);
//                lnames.add(lastName);
//                salaries.add(salary);
//
//                out.printf("<tr><td>%d</td><td>%s</td><td>%s</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td><td>%s</td></tr>",
//                        id, firstName, lastName, ssn, superSsn, salary, departmentNumber, gender);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace(out);
//        }
//
//        out.println("</table>");
//        out.println("</body>");
//        out.println("</html>");
//        request.setAttribute("ids", ids);
//        request.setAttribute("fnames", fnames);
//        request.setAttribute("lnames", lnames);
//        request.setAttribute("salaries", salaries);
//        getServletContext().getRequestDispatcher("/emps.jsp").forward(request, response);
//    }
//
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("text/html");
//        PrintWriter out = response.getWriter();
//
//        int id = Integer.parseInt(request.getParameter("id"));
//        String firstName = request.getParameter("firstName");
//        String lastName = request.getParameter("lastName");
//        int ssn = Integer.parseInt(request.getParameter("ssn"));
//        int superSsn = Integer.parseInt(request.getParameter("superSsn"));
//        int salary = Integer.parseInt(request.getParameter("salary"));
//        int departmentNumber = Integer.parseInt(request.getParameter("departmentNumber"));
//        String gender = request.getParameter("gender");
//
//        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_QUERY)) {
//            // Set parameters and execute insert
//            preparedStatement.setInt(1, id);
//            preparedStatement.setString(2, firstName);
//            preparedStatement.setString(3, lastName);
//            preparedStatement.setInt(4, ssn);
//            preparedStatement.setInt(5, superSsn);
//            preparedStatement.setInt(6, salary);
//            preparedStatement.setInt(7, departmentNumber);
//            preparedStatement.setString(8, gender);
//
//            int rowsInserted = preparedStatement.executeUpdate();
//
//            if (rowsInserted > 0) {
//                out.println("<p>Employee inserted successfully.</p>");
//            } else {
//                out.println("<p>Failed to insert employee.</p>");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace(out);
//        }
//    }
//
//    @Override
//    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("text/html");
//        PrintWriter out = response.getWriter();
//
//        String idParam = request.getParameter("id");
//        if (idParam == null || idParam.isEmpty()) {
//            out.println("<p>Employee ID is required.</p>");
//            return;
//        }
//
//        int id = Integer.parseInt(idParam);
//
//        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_QUERY)) {
//            // Set parameters and execute delete
//            preparedStatement.setInt(1, id);
//            int rowsDeleted = preparedStatement.executeUpdate();
//
//            if (rowsDeleted > 0) {
//                out.write("Success");
//            } else {
//                out.println("<p>Failed to delete employee. Employee ID not found.</p>");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace(out);
//            out.println("<p>Database error occurred.</p>");
//        }
//    }
//}

//
//package main.webapp.controller;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//@WebServlet("/tasks")
//public class SQLServerConnection extends HttpServlet {
//    private static final long serialVersionUID = 1L;
//
//    // Database connection parameters
////    private static final String CONNECTION_URL = "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;user=taskManagerUser;password=k3wan123;encrypt=false;trustServerCertificate=true;loginTimeout=30;";
////    private static final String CONNECTION_URL = "jdbc:sqlserver://localhost:1433;instanceName=MSSQLSERVER01;databaseName=TaskManagerDB;user=k3wan;password=123;encrypt=false;trustServerCertificate=true;";
//
//    private static final String CONNECTION_URL =
//            "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;user=k3wan;password=123;encrypt=false;trustServerCertificate=true;";
//
//    // SQL query to fetch tasks
//    private static final String SELECT_QUERY = "SELECT * FROM tasks ORDER BY created_at DESC";
//
//    // Database connection
//    private Connection connection;
//
//    @Override
//    public void init() throws ServletException {
//
//        super.init();
//        System.out.println("init");
//        try {
//
//            // Load the SQL Server JDBC driver
//            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//            // Establish the connection
//            connection = DriverManager.getConnection(CONNECTION_URL);
//            System.out.println("Connection Established!!");
//        } catch (ClassNotFoundException | SQLException e) {
//            throw new ServletException("Failed to initialize database connection", e);
//        }
//    }
//
//    @Override
//    public void destroy() {
//        super.destroy();
//        if (connection != null) {
//            try {
//                // Close the connection when the server shuts down
//                System.out.println("Connection Closed!!");
//                connection.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // GET request to list tasks
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        System.out.println("Gett happened");
//        response.setContentType("text/html");
//        PrintWriter out = response.getWriter();
//
//        List<Integer> ids = new ArrayList<>();
//        List<String> titles = new ArrayList<>();
//        List<String> descriptions = new ArrayList<>();
//        List<String> statuses = new ArrayList<>();
//        List<String> priorities = new ArrayList<>();
//
//        out.println("<html>");
//        out.println("<head><title>Task Manager</title></head>");
//        out.println("<body>");
//        out.println("<h1>Task Manager</h1>");
//        out.println("<table border='1'>");
//        out.println("<tr><th>ID</th><th>Title</th><th>Description</th><th>Due Date</th><th>Status</th><th>Priority</th></tr>");
//
//        try (Statement statement = connection.createStatement();
//             ResultSet resultSet = statement.executeQuery(SELECT_QUERY)) {
//            while (resultSet.next()) {
//                int id = resultSet.getInt("id");
//                String title = resultSet.getString("title");
//                String description = resultSet.getString("description");
//                Date dueDate = resultSet.getDate("due_date");
//                String status = resultSet.getString("status");
//                String priority = resultSet.getString("priority");
//
//                ids.add(id);
//                titles.add(title);
//                descriptions.add(description);
//                statuses.add(status);
//                priorities.add(priority);
//
//                // Print data in table rows
//                out.printf("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
//                        id, title, description, dueDate, status, priority);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace(out);
//        }
//
//        out.println("</table>");
//        out.println("</body>");
//        out.println("</html>");
//    }
//}
package main.webapp.controller;

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

@WebServlet(urlPatterns = "/tasks", loadOnStartup = 1)
public class TaskServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;user=k3wan;password=123;encrypt=false;trustServerCertificate=true;";
    private Connection connection;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(CONNECTION_URL);
            System.out.println("Connection Established!!");
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServletException("Failed to initialize database connection", e);
        }
    }

    @Override
    public void destroy() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection Closed!!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // GET request to fetch tasks for a specific user
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("userId");

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"User ID is required.\"}");
            return;
        }

        String selectQuery = "SELECT t.* FROM tasks t " +
                "JOIN user_tasks ut ON t.id = ut.task_id " +
                "WHERE ut.user_id = ? ORDER BY t.created_at DESC";

        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, Integer.parseInt(userId));
            ResultSet rs = ps.executeQuery();

            JSONArray tasks = new JSONArray();
            while (rs.next()) {
                JSONObject task = new JSONObject();
                task.put("id", rs.getInt("id"));
                task.put("title", rs.getString("title"));
                task.put("description", rs.getString("description"));
                task.put("dueDate", rs.getString("due_date"));
                task.put("priority", rs.getString("priority"));
                task.put("status", rs.getString("status"));
                task.put("created_at", rs.getString("created_at"));
                tasks.put(task);
            }

            response.setContentType("application/json");
            response.getWriter().write(tasks.toString());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error.\"}");
            e.printStackTrace();
        }
    }

    // POST request to add a new task and link it to a user
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        try {
            JSONObject json = new JSONObject(sb.toString());
            String title = json.getString("title");
            String description = json.getString("description");
            String dueDate = json.getString("dueDate");
            String priority = json.getString("priority");
            String status = json.getString("status");
            int userId = json.getInt("userId");

            String insertTaskQuery = "INSERT INTO tasks (title, description, due_date, priority, status) OUTPUT INSERTED.ID VALUES (?, ?, ?, ?, ?)";
            String insertUserTaskQuery = "INSERT INTO user_tasks (user_id, task_id) VALUES (?, ?)";

            try (PreparedStatement taskStmt = connection.prepareStatement(insertTaskQuery)) {
                taskStmt.setString(1, title);
                taskStmt.setString(2, description);
                taskStmt.setString(3, dueDate);
                taskStmt.setString(4, priority);
                taskStmt.setString(5, status);

                ResultSet rs = taskStmt.executeQuery();
                if (rs.next()) {
                    int taskId = rs.getInt(1);

                    try (PreparedStatement userTaskStmt = connection.prepareStatement(insertUserTaskQuery)) {
                        userTaskStmt.setInt(1, userId);
                        userTaskStmt.setInt(2, taskId);
                        userTaskStmt.executeUpdate();

                        response.setStatus(HttpServletResponse.SC_CREATED);
                        response.getWriter().write("{\"message\": \"Task created successfully.\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"message\": \"Failed to create task.\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Invalid JSON data.\"}");
            e.printStackTrace();
        }
    }

    // PUT request to update a task for a specific user
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder requestBody = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        try {
            JSONObject json = new JSONObject(requestBody.toString());
            int taskId = json.getInt("id");
            int userId = json.getInt("userId");
            String title = json.getString("title");
            String description = json.getString("description");
            String dueDate = json.getString("dueDate");
            String priority = json.getString("priority");
            String status = json.getString("status");

            String updateQuery = "UPDATE tasks SET title = ?, description = ?, due_date = ?, priority = ?, status = ? " +
                    "WHERE id = ? AND id IN (SELECT task_id FROM user_tasks WHERE user_id = ?)";

            try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
                ps.setString(1, title);
                ps.setString(2, description);
                ps.setString(3, dueDate);
                ps.setString(4, priority);
                ps.setString(5, status);
                ps.setInt(6, taskId);
                ps.setInt(7, userId);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("{\"message\": \"Task updated successfully.\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"message\": \"Task not found or you do not have permission to update it.\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Invalid JSON data.\"}");
            e.printStackTrace();
        }
    }

    // DELETE request to delete a task for a specific user
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder requestBody = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        try {
            JSONObject json = new JSONObject(requestBody.toString());
            int taskId = json.getInt("id");
            int userId = json.getInt("userId");

            String deleteQuery = "DELETE FROM tasks WHERE id = ? AND id IN (SELECT task_id FROM user_tasks WHERE user_id = ?)";

            try (PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
                ps.setInt(1, taskId);
                ps.setInt(2, userId);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("{\"message\": \"Task deleted successfully.\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"message\": \"Task not found or you do not have permission to delete it.\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Invalid JSON data.\"}");
            e.printStackTrace();
        }
    }
}
