package com.example.hellofx;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TaskClient {


    // Base URL of your servlet
    private static final String BASE_URL = "http://localhost:8080/servletnew/tasks"; // Update with your servlet URL
    private static List<Task> tasks = new ArrayList<>();

    // Create a new task (POST)
    public static void createTask(
            String title,
            String description,
            String dueDate,
            String priority,
            String status,
            int userId

    ) {
        try {
            String token=Token.getInstance().getToken();

            URL url = new URL(BASE_URL + "?userId=" + userId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);

            // Task JSON to send in the request body
            String jsonBody = String.format("""
                {
                    "title": "%s",
                    "description": "%s",
                    "dueDate": "%s",
                    "priority": "%s",
                    "status": "%s",
                    "userId": "%d"
                }
            """, title, description, dueDate, priority, status, userId);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                printResponse(connection);
            } else {
                printResponse(connection);
            }

            connection.disconnect();
            readTasks(userId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read all tasks (GET)
    public static void readTasks(int userId) {
        try {
            String token=Token.getInstance().getToken();

            // Add the userId as a query parameter to the URL
            URL url = new URL(BASE_URL + "?userId=" + userId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }

                // Parse the response and update the tasks list
                tasks.clear(); // Clear old tasks
                JSONArray taskArray = new JSONArray(response.toString());
                for (int i = 0; i < taskArray.length(); i++) {
                    JSONObject taskJson = taskArray.getJSONObject(i);
                    Task task = new Task(
                            taskJson.getInt("id"),
                            taskJson.getString("title"),
                            taskJson.getString("description"),
                            taskJson.getString("dueDate"),
                            taskJson.getString("priority"),
                            taskJson.getString("status"),
                            taskJson.getString("created_at")
                    );
                    tasks.add(task);
                }
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update an existing task (PUT)
    public static void updateTask(
            int id,
            String title,
            String description,
            String dueDate,
            String priority,
            String status,
            int userId
    ) {
        try {
            String token=Token.getInstance().getToken();


            URL url = new URL(BASE_URL + "?userId=" + userId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);

            // Task JSON to send in the request body
            String jsonBody = String.format("""
                {
                    "id": %d,
                    "title": "%s",
                    "description": "%s",
                    "dueDate": "%s",
                    "priority": "%s",
                    "status": "%s",
                    "userId": "%d"
                }
            """, id, title, description, dueDate, priority, status, userId);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                printResponse(connection);
            } else {
                printResponse(connection);
            }

            connection.disconnect();
            readTasks(userId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Delete a task (DELETE)
    public static void deleteTask(int id, int userId) {
        try {
            String token=Token.getInstance().getToken();


            URL url = new URL(BASE_URL + "?userId=" + userId);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);

            // Task ID JSON to send in the request body
            String jsonBody = String.format("""
                {
                    "id": %d,
                    "userId": %d
                }
            """, id, userId);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                printResponse(connection);
            } else {
                printResponse(connection);
            }

            connection.disconnect();
            readTasks(userId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Utility method to print response
    private static void printResponse(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Return the list of tasks
    public static List<Task> getTasks() {
        return tasks;
    }
}
