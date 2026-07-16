package com.example.hellofx.services;

import com.example.hellofx.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.time.Instant;
import java.time.Duration;

public class AIAssistantService {
    private static AIAssistantService instance;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private Instant lastRequestTime;
    private static final Duration MIN_REQUEST_INTERVAL = Duration.ofSeconds(1);
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";

    private AIAssistantService() {
        this.apiKey = "AIzaSyCkrPOjAZvQAu26r0vVpdqV98Kk3GK2n9I";
        this.objectMapper = new ObjectMapper();
        this.lastRequestTime = Instant.now().minus(MIN_REQUEST_INTERVAL);
        
        this.client = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    public static AIAssistantService getInstance() {
        if (instance == null) {
            instance = new AIAssistantService();
        }
        return instance;
    }

    public String getTaskAdvice(Task task) throws Exception {
        String prompt = String.format(
                "As a task management assistant, help me complete this task effectively:\n" +
                        "Task: %s\nDescription: %s\nDue Date: %s\n" +
                        "Please provide:\n" +
                        "1. A breakdown of steps\n" +
                        "2. Time estimates\n" +
                        "3. Key considerations\n" +
                        "Keep the response concise and practical.",
                task.getTitle(),
                task.getDescription(),
                task.getDueDate()
        );
        return getAIResponse(prompt);
    }

    public String getTaskPrioritization(List<Task> tasks) throws Exception {
        StringBuilder prompt = new StringBuilder("As a task prioritization expert, help me organize these tasks:\n\n");
        for (Task task : tasks) {
            prompt.append(String.format("- %s (Due: %s)\n", task.getTitle(), task.getDueDate()));
        }
        prompt.append("\nPlease provide:\n");
        prompt.append("1. Priority order\n");
        prompt.append("2. Suggested schedule\n");
        prompt.append("3. Time management tips\n");
        prompt.append("Keep the response concise and actionable.");

        return getAIResponse(prompt.toString());
    }

    public String getGeneralAdvice(String userQuery) throws Exception {
        String prompt = "As a task management assistant, please provide clear and practical advice for the following question:\n\n" + userQuery;
        return getAIResponse(prompt);
    }

    private synchronized String getAIResponse(String prompt) throws Exception {
        // Check if we need to wait before making another request
        Duration timeSinceLastRequest = Duration.between(lastRequestTime, Instant.now());
        if (timeSinceLastRequest.compareTo(MIN_REQUEST_INTERVAL) < 0) {
            Thread.sleep(MIN_REQUEST_INTERVAL.minus(timeSinceLastRequest).toMillis());
        }

        try {
            // Build request body according to Gemini API format
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> parts = new HashMap<>();
            parts.put("parts", Collections.singletonList(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(parts));

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            // Create request
            Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + apiKey)
                .post(RequestBody.create(MediaType.parse("application/json"), jsonRequest))
                .build();

            // Execute request
            try (Response response = client.newCall(request).execute()) {
                lastRequestTime = Instant.now();

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    if (response.code() == 429) {
                        return "The AI service is currently busy. Please try again in a minute.";
                    }
                    throw new Exception("API error: " + errorBody);
                }

                String responseStr = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseStr, Map.class);

                // Parse Gemini API response format
                if (responseMap.containsKey("candidates")) {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map<String, Object> candidate = candidates.get(0);
                        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                        List<Map<String, String>> part = (List<Map<String, String>>) content.get("parts");
                        if (!part.isEmpty()) {
                            return part.get(0).get("text");
                        }
                    }
                }
                return "No valid response from AI.";
            }
        } catch (Exception e) {
            if (e.getMessage().contains("rate limit")) {
                return "The AI service is currently at capacity. Please try again in a few minutes.";
            }
            throw new Exception("Error getting AI response: " + e.getMessage());
        }
    }
}