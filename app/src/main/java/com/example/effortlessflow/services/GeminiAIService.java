//package com.example.effortlessflow.services;
//
//import android.util.Log;
//import com.example.effortlessflow.model.SubTask;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class GeminiAIService {
//    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
//    private static final String API_KEY = "AIzaSyChDBPfiRGxwAWneyT9cpNvIJQCVJNPi7w"; // Replace with your actual API key
//    private static final String TAG = "GeminiAIService";
//
//    private ExecutorService executor;
//
//    public GeminiAIService() {
//        executor = Executors.newSingleThreadExecutor();
//    }
//
//    public void generateSubtasks(String taskTitle, String taskDescription, Date dueDate,
//                                 String effort, OnSubtasksGeneratedListener listener) {
//        executor.execute(() -> {
//            try {
//                String prompt = buildPrompt(taskTitle, taskDescription, dueDate, effort);
//                String response = callGeminiAPI(prompt);
//                List<SubTask> subtasks = parseSubtasksFromResponse(response);
//
//                // Post back to main thread
//                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
//                    if (subtasks.isEmpty()) {
//                        listener.onError("Failed to generate subtasks");
//                    } else {
//                        listener.onSubtasksGenerated(subtasks);
//                    }
//                });
//
//            } catch (Exception e) {
//                Log.e(TAG, "Error generating subtasks", e);
//                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
//                        listener.onError("Failed to generate subtasks: " + e.getMessage()));
//            }
//        });
//    }
//
//    private String buildPrompt(String taskTitle, String taskDescription, Date dueDate, String effort) {
//        StringBuilder prompt = new StringBuilder();
//        prompt.append("Break down this task into 3-5 actionable subtasks with time estimates:\n\n");
//        prompt.append("Task: ").append(taskTitle).append("\n");
//
//        if (taskDescription != null && !taskDescription.isEmpty()) {
//            prompt.append("Description: ").append(taskDescription).append("\n");
//        }
//
//        if (dueDate != null) {
//            prompt.append("Due Date: ").append(dueDate.toString()).append("\n");
//        }
//
//        prompt.append("Effort Level: ").append(effort).append("\n\n");
//
//        prompt.append("Please respond with ONLY a JSON array in this format:\n");
//        prompt.append("[\n");
//        prompt.append("  {\n");
//        prompt.append("    \"title\": \"Subtask name\",\n");
//        prompt.append("    \"description\": \"Brief description\",\n");
//        prompt.append("    \"estimatedTime\": \"2 hours\"\n");
//        prompt.append("  }\n");
//        prompt.append("]\n\n");
//        prompt.append("Make subtasks specific, actionable, and realistic. Time estimates should be in hours or minutes.");
//
//        return prompt.toString();
//    }
//
//    private String callGeminiAPI(String prompt) throws Exception {
//        URL url = new URL(API_URL + "?key=" + API_KEY);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setDoOutput(true);
//
//        // Build request body
//        JSONObject requestBody = new JSONObject();
//        JSONArray contents = new JSONArray();
//        JSONObject content = new JSONObject();
//        JSONArray parts = new JSONArray();
//        JSONObject part = new JSONObject();
//
//        part.put("text", prompt);
//        parts.put(part);
//        content.put("parts", parts);
//        contents.put(content);
//        requestBody.put("contents", contents);
//
//        // Send request
//        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
//        writer.write(requestBody.toString());
//        writer.flush();
//        writer.close();
//
//        // Read response
//        int responseCode = connection.getResponseCode();
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            StringBuilder response = new StringBuilder();
//            String line;
//
//            while ((line = reader.readLine()) != null) {
//                response.append(line);
//            }
//            reader.close();
//
//            return response.toString();
//        } else {
//            throw new Exception("API call failed with response code: " + responseCode);
//        }
//    }
//
//    private List<SubTask> parseSubtasksFromResponse(String response) {
//        List<SubTask> subtasks = new ArrayList<>();
//
//        try {
//            JSONObject jsonResponse = new JSONObject(response);
//            JSONArray candidates = jsonResponse.getJSONArray("candidates");
//
//            if (candidates.length() > 0) {
//                JSONObject candidate = candidates.getJSONObject(0);
//                JSONObject content = candidate.getJSONObject("content");
//                JSONArray parts = content.getJSONArray("parts");
//
//                if (parts.length() > 0) {
//                    String text = parts.getJSONObject(0).getString("text");
//
//                    // Extract JSON from the text response
//                    int startIndex = text.indexOf("[");
//                    int endIndex = text.lastIndexOf("]") + 1;
//
//                    if (startIndex != -1 && endIndex > startIndex) {
//                        String jsonText = text.substring(startIndex, endIndex);
//                        JSONArray subtaskArray = new JSONArray(jsonText);
//
//                        for (int i = 0; i < subtaskArray.length(); i++) {
//                            JSONObject subtaskJson = subtaskArray.getJSONObject(i);
//                            SubTask subtask = new SubTask(
//                                    subtaskJson.getString("title"),
//                                    subtaskJson.optString("description", ""),
//                                    subtaskJson.getString("estimatedTime")
//                            );
//                            subtasks.add(subtask);
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error parsing AI response", e);
//            // Fallback to hardcoded suggestions
//            return getFallbackSubtasks();
//        }
//
//        return subtasks;
//    }
//
//    private List<SubTask> getFallbackSubtasks() {
//        List<SubTask> fallbackTasks = new ArrayList<>();
//        fallbackTasks.add(new SubTask("Research and planning", "Gather requirements and plan approach", "1 hour"));
//        fallbackTasks.add(new SubTask("Initial setup", "Set up necessary tools and environment", "30 minutes"));
//        fallbackTasks.add(new SubTask("Main implementation", "Complete the core work", "2 hours"));
//        fallbackTasks.add(new SubTask("Review and testing", "Test and refine the work", "45 minutes"));
//        fallbackTasks.add(new SubTask("Final touches", "Polish and finalize", "30 minutes"));
//        return fallbackTasks;
//    }
//
//    public interface OnSubtasksGeneratedListener {
//        void onSubtasksGenerated(List<SubTask> subtasks);
//        void onError(String error);
//    }
//}


//package com.example.effortlessflow.services;
//
//import android.util.Log;
//import com.example.effortlessflow.model.SubTask; // Assuming this is your SubTask model class
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.OutputStream; // Changed from OutputStreamWriter to OutputStream for byte handling
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets; // For UTF-8 encoding
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale; // For SimpleDateFormat locale
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class GeminiAIService {
//
//    // *** IMPORTANT: Replace "YOUR_ACTUAL_API_KEY_HERE" with your actual Google Cloud API Key ***
//    private static final String API_KEY = "AIzaSyA48YyFhFv_C4sfqOMtmkPJKrWDACC3gEc"; //old key AIzaSyD8IBolYRs1RkWOEa-GKv_GEf06oguM2PI
//    // Changed to gemini-pro for more stable and structured output
//    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
//    private static final String TAG = "GeminiAIService";
//
//    private ExecutorService executor;
//
//    public GeminiAIService() {
//        executor = Executors.newSingleThreadExecutor();
//    }
//
//    public void generateSubtasks(String taskTitle, String taskDescription, Date dueDate,
//                                 String effort, OnSubtasksGeneratedListener listener) {
//        executor.execute(() -> {
//            try {
//                // Check if API_KEY is set
//                if (API_KEY.equals("YOUR_ACTUAL_API_KEY_HERE") || API_KEY.isEmpty()) {
//                    Log.e(TAG, "API Key is not set. Please replace 'YOUR_ACTUAL_API_KEY_HERE' with your actual API key.");
//                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
//                            listener.onError("API Key is not configured. Please set your API key in GeminiAIService.java"));
//                    return; // Stop execution if API key is not set
//                }
//
//                String prompt = buildPrompt(taskTitle, taskDescription, dueDate, effort);
//                Log.d(TAG, "Sending prompt to Gemini: " + prompt);
//
//                String rawApiResponse = callGeminiAPI(prompt);
//                Log.d(TAG, "Raw Gemini API Response: " + rawApiResponse);
//
//                List<SubTask> subtasks = parseSubtasksFromResponse(rawApiResponse);
//
//                // Post back to main thread
//                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
//                    if (subtasks != null && !subtasks.isEmpty()) { // Ensure subtasks list is not null and not empty
//                        listener.onSubtasksGenerated(subtasks);
//                    } else {
//                        // This case is likely handled by parseSubtasksFromResponse returning fallback,
//                        // but adding an explicit check here for safety.
//                        listener.onError("Failed to parse subtasks from AI response. Returning fallback.");
//                        listener.onSubtasksGenerated(getFallbackSubtasks()); // Ensure fallback is sent to listener
//                    }
//                });
//
//            } catch (Exception e) {
//                Log.e(TAG, "Error generating subtasks: " + e.getMessage(), e);
//                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
//                    listener.onError("Failed to generate subtasks: " + e.getMessage());
//                    listener.onSubtasksGenerated(getFallbackSubtasks()); // Always provide fallback on error
//                });
//            }
//        });
//    }
//
//    private String buildPrompt(String taskTitle, String taskDescription, Date dueDate, String effort) {
//        StringBuilder prompt = new StringBuilder();
//        prompt.append("You are an expert task breakdown assistant. Your goal is to break down the given task into 3 to 5 actionable subtasks. Each subtask must have a realistic time estimate (in hours or minutes) and a brief description.\n\n");
//        prompt.append("Task Title: ").append(taskTitle).append("\n");
//
//        if (taskDescription != null && !taskDescription.isEmpty()) {
//            prompt.append("Task Description: ").append(taskDescription).append("\n");
//        }
//
//        if (dueDate != null) {
//            // Format the date for better AI understanding
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//            prompt.append("Due Date: ").append(sdf.format(dueDate)).append("\n");
//        }
//
//        prompt.append("Effort Level (intended by user): ").append(effort).append("\n\n");
//
//        prompt.append("Generate ONLY a JSON array of subtask objects. DO NOT include any other text, preambles, or explanations outside the JSON. The JSON structure must be exactly as follows:\n");
//        prompt.append("```json\n"); // Using markdown code block for emphasis to AI
//        prompt.append("[\n");
//        prompt.append("  {\n");
//        prompt.append("    \"title\": \"Subtask name (e.g., Research project requirements)\",\n");
//        prompt.append("    \"description\": \"A concise description of what needs to be done for this subtask (e.g., Identify key stakeholders and project goals)\",\n");
//        prompt.append("    \"estimatedTime\": \"Time duration in hours or minutes (e.g., 2 hours, 30 minutes)\"\n");
//        prompt.append("  }\n");
//        prompt.append("]\n");
//        prompt.append("```\n\n");
//        prompt.append("Ensure the subtasks are specific to the given task, actionable, and that the estimated times are realistic and formatted correctly.");
//
//        return prompt.toString();
//    }
//
//    private String callGeminiAPI(String prompt) throws Exception {
//        URL url = new URL(API_URL + "?key=" + API_KEY);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setDoOutput(true);
//
//        // Build request body with proper JSON escaping
//        JSONObject requestBody = new JSONObject();
//        JSONArray contents = new JSONArray();
//        JSONObject content = new JSONObject();
//        JSONArray parts = new JSONArray();
//        JSONObject part = new JSONObject();
//
//        part.put("text", escapeJson(prompt)); // Use the escapeJson helper
//        parts.put(part);
//        content.put("parts", parts);
//        contents.put(content);
//        requestBody.put("contents", contents);
//
//        Log.d(TAG, "Request Body: " + requestBody.toString());
//
//        // Send request using OutputStream for byte handling and UTF-8
//        try (OutputStream os = connection.getOutputStream()) {
//            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
//            os.write(input, 0, input.length);
//        }
//
//        // Read response
//        int responseCode = connection.getResponseCode();
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
//                StringBuilder response = new StringBuilder();
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    response.append(line);
//                }
//                return response.toString();
//            }
//        } else {
//            // Read error stream for more details if the response code is not OK
//            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
//                StringBuilder errorResponse = new StringBuilder();
//                String errorLine;
//                while ((errorLine = errorReader.readLine()) != null) {
//                    errorResponse.append(errorLine);
//                }
//                Log.e(TAG, "Gemini API Error Response (Code " + responseCode + "): " + errorResponse.toString());
//                throw new Exception("API call failed with response code: " + responseCode + " - " + errorResponse.toString());
//            }
//        }
//    }
//
//    private List<SubTask> parseSubtasksFromResponse(String response) {
//        List<SubTask> subtasks = new ArrayList<>();
//        Log.d(TAG, "Raw AI Response (for parsing): " + response);
//
//        try {
//            JSONObject jsonResponse = new JSONObject(response);
//            JSONArray candidates = jsonResponse.getJSONArray("candidates");
//
//            if (candidates.length() > 0) {
//                JSONObject candidate = candidates.getJSONObject(0);
//                JSONObject content = candidate.getJSONObject("content");
//                JSONArray parts = content.getJSONArray("parts");
//
//                if (parts.length() > 0) {
//                    String text = parts.getJSONObject(0).getString("text");
//                    Log.d(TAG, "Extracted AI Text: " + text);
//
//                    // --- Improved JSON extraction ---
//                    // Find the start and end of the actual JSON array, robustly handling surrounding text
//                    int jsonStartIndex = text.indexOf("[");
//                    int jsonEndIndex = text.lastIndexOf("]");
//
//                    if (jsonStartIndex != -1 && jsonEndIndex != -1 && jsonEndIndex > jsonStartIndex) {
//                        String jsonText = text.substring(jsonStartIndex, jsonEndIndex + 1); // +1 to include the ']'
//                        Log.d(TAG, "Parsed JSON Text: " + jsonText);
//
//                        JSONArray subtaskArray = new JSONArray(jsonText);
//
//                        for (int i = 0; i < subtaskArray.length(); i++) {
//                            JSONObject subtaskJson = subtaskArray.getJSONObject(i);
//                            SubTask subtask = new SubTask(
//                                    subtaskJson.getString("title"),
//                                    subtaskJson.optString("description", ""), // Use optString for optional fields
//                                    subtaskJson.getString("estimatedTime")
//                            );
//                            subtasks.add(subtask);
//                        }
//                        return subtasks; // Return here if parsing is successful
//                    } else {
//                        Log.e(TAG, "Could not find valid JSON array in AI response. Text: " + text);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error parsing AI response, returning fallback: " + e.getMessage(), e);
//        }
//
//        // If anything goes wrong, or no valid JSON was found, return fallback tasks
//        Log.d(TAG, "Returning fallback subtasks.");
//        return getFallbackSubtasks();
//    }
//
//    /**
//     * Helper method to escape special characters in a string so it can be safely embedded in JSON.
//     * Required for the prompt text within the API request body.
//     */
//    private String escapeJson(String text) {
//        return text.replace("\\", "\\\\")
//                .replace("\"", "\\\"")
//                .replace("\n", "\\n")
//                .replace("\r", "\\r")
//                .replace("\t", "\\t");
//    }
//
//    private List<SubTask> getFallbackSubtasks() {
//        List<SubTask> fallbackTasks = new ArrayList<>();
//        fallbackTasks.add(new SubTask("Analyze Requirements", "Understand the core objectives and deliverables.", "1 hour"));
//        fallbackTasks.add(new SubTask("Plan Execution", "Outline the steps and resources needed.", "30 minutes"));
//        fallbackTasks.add(new SubTask("Review and Refine", "Check for completeness and accuracy.", "1 hour"));
//        return fallbackTasks;
//    }
//
//    public interface OnSubtasksGeneratedListener {
//        void onSubtasksGenerated(List<SubTask> subtasks);
//        void onError(String error);
//    }
//
//    // Assuming SubTask is defined in com.example.effortlessflow.model.SubTask
//    // If not, please ensure your SubTask class has a constructor matching
//    // public SubTask(String title, String description, String estimatedTime)
//}


//package com.example.effortlessflow.services;
//
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//
//import com.example.effortlessflow.api.GeminiApiInterface;
//import com.example.effortlessflow.model.GeminiRequest;
//import com.example.effortlessflow.model.GeminiResponse;
//import com.example.effortlessflow.model.SubTask;
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.google.gson.reflect.TypeToken;
//
//import java.lang.reflect.Type;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class GeminiAIService {
//
//    // *** IMPORTANT: Replace "YOUR_ACTUAL_API_KEY_HERE" with your actual Google Cloud API Key ***
//    private static final String API_KEY = "AIzaSyA48YyFhFv_C4sfqOMtmkPJKrWDACC3gEc";
//    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
//    private static final String TAG = "GeminiAIService";
//
//    private GeminiApiInterface apiInterface;
//    private ExecutorService executor;
//    private Handler mainHandler;
//    private Gson gson;
//
//    public GeminiAIService() {
//        executor = Executors.newSingleThreadExecutor();
//        mainHandler = new Handler(Looper.getMainLooper());
//        gson = new Gson();
//
//        // Create logging interceptor
//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        // Create OkHttpClient with timeout settings
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(logging)
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .writeTimeout(30, TimeUnit.SECONDS)
//                .build();
//
//        // Create Retrofit instance
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .client(okHttpClient)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        apiInterface = retrofit.create(GeminiApiInterface.class);
//    }
//
//    public void generateSubtasks(String taskTitle, String taskDescription, Date dueDate,
//                                 String effort, OnSubtasksGeneratedListener listener) {
//
//        // Check if API_KEY is set
//        if (API_KEY.equals("YOUR_ACTUAL_API_KEY_HERE") || API_KEY.isEmpty()) {
//            Log.e(TAG, "API Key is not set. Please replace 'YOUR_ACTUAL_API_KEY_HERE' with your actual API key.");
//            mainHandler.post(() -> {
//                listener.onError("API Key is not configured. Please set your API key in GeminiAIService.java");
//                listener.onSubtasksGenerated(getFallbackSubtasks());
//            });
//            return;
//        }
//
//        executor.execute(() -> {
//            try {
//                String prompt = buildPrompt(taskTitle, taskDescription, dueDate, effort);
//                Log.d(TAG, "Sending prompt to Gemini: " + prompt);
//
//                // Create request
//                GeminiRequest.Part part = new GeminiRequest.Part(prompt);
//                GeminiRequest.Content content = new GeminiRequest.Content(Arrays.asList(part));
//                GeminiRequest request = new GeminiRequest(Arrays.asList(content));
//
//                // Make API call
//                Call<GeminiResponse> call = apiInterface.generateContent(API_KEY, request);
//
//                call.enqueue(new Callback<GeminiResponse>() {
//                    @Override
//                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
//                        mainHandler.post(() -> handleApiResponse(response, listener));
//                    }
//
//                    @Override
//                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
//                        Log.e(TAG, "API call failed: " + t.getMessage(), t);
//                        mainHandler.post(() -> {
//                            listener.onError("Network error: " + t.getMessage());
//                            listener.onSubtasksGenerated(getFallbackSubtasks());
//                        });
//                    }
//                });
//
//            } catch (Exception e) {
//                Log.e(TAG, "Error generating subtasks: " + e.getMessage(), e);
//                mainHandler.post(() -> {
//                    listener.onError("Failed to generate subtasks: " + e.getMessage());
//                    listener.onSubtasksGenerated(getFallbackSubtasks());
//                });
//            }
//        });
//    }
//
//    private void handleApiResponse(Response<GeminiResponse> response, OnSubtasksGeneratedListener listener) {
//        try {
//            if (response.isSuccessful() && response.body() != null) {
//                Log.d(TAG, "API Response successful");
//
//                GeminiResponse geminiResponse = response.body();
//                List<SubTask> subtasks = parseSubtasksFromResponse(geminiResponse);
//
//                if (subtasks != null && !subtasks.isEmpty()) {
//                    listener.onSubtasksGenerated(subtasks);
//                } else {
//                    Log.w(TAG, "No valid subtasks found, using fallback");
//                    listener.onError("Failed to parse subtasks from AI response");
//                    listener.onSubtasksGenerated(getFallbackSubtasks());
//                }
//            } else {
//                Log.e(TAG, "API Response failed: " + response.code() + " - " + response.message());
//                String errorMessage = "API call failed: " + response.code() + " - " + response.message();
//
//                if (response.errorBody() != null) {
//                    try {
//                        String errorBody = response.errorBody().string();
//                        Log.e(TAG, "Error body: " + errorBody);
//                        errorMessage += " - " + errorBody;
//                    } catch (Exception e) {
//                        Log.e(TAG, "Error reading error body", e);
//                    }
//                }
//
//                listener.onError(errorMessage);
//                listener.onSubtasksGenerated(getFallbackSubtasks());
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error handling API response", e);
//            listener.onError("Error processing response: " + e.getMessage());
//            listener.onSubtasksGenerated(getFallbackSubtasks());
//        }
//    }
//
//    private String buildPrompt(String taskTitle, String taskDescription, Date dueDate, String effort) {
//        StringBuilder prompt = new StringBuilder();
//        prompt.append("You are an expert task breakdown assistant. Your goal is to break down the given task into 3 to 5 actionable subtasks. Each subtask must have a realistic time estimate and a brief description.\n\n");
//
//        prompt.append("Task Title: ").append(taskTitle).append("\n");
//
//        if (taskDescription != null && !taskDescription.trim().isEmpty()) {
//            prompt.append("Task Description: ").append(taskDescription.trim()).append("\n");
//        }
//
//        if (dueDate != null) {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//            prompt.append("Due Date: ").append(sdf.format(dueDate)).append("\n");
//        }
//
//        prompt.append("Effort Level: ").append(effort).append("\n\n");
//
//        prompt.append("IMPORTANT: Respond with ONLY a valid JSON array. No other text, explanations, or markdown formatting. The response should start with '[' and end with ']'.\n\n");
//        prompt.append("JSON format required:\n");
//        prompt.append("[\n");
//        prompt.append("  {\n");
//        prompt.append("    \"title\": \"Subtask name\",\n");
//        prompt.append("    \"description\": \"Brief description of what needs to be done\",\n");
//        prompt.append("    \"estimatedTime\": \"2 hours\"\n");
//        prompt.append("  }\n");
//        prompt.append("]\n\n");
//        prompt.append("Make sure the estimatedTime format is consistent (e.g., '1 hour', '30 minutes', '2 hours').");
//
//        return prompt.toString();
//    }
//
//    private List<SubTask> parseSubtasksFromResponse(GeminiResponse response) {
//        List<SubTask> subtasks = new ArrayList<>();
//
//        try {
//            if (response.getCandidates() != null && !response.getCandidates().isEmpty()) {
//                GeminiResponse.Candidate candidate = response.getCandidates().get(0);
//
//                if (candidate.getContent() != null &&
//                        candidate.getContent().getParts() != null &&
//                        !candidate.getContent().getParts().isEmpty()) {
//
//                    String text = candidate.getContent().getParts().get(0).getText();
//                    Log.d(TAG, "Raw AI text response: " + text);
//
//                    // Clean the text - remove any markdown formatting or extra text
//                    String cleanedText = text.trim();
//
//                    // Find JSON array boundaries
//                    int jsonStart = cleanedText.indexOf('[');
//                    int jsonEnd = cleanedText.lastIndexOf(']');
//
//                    if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
//                        String jsonText = cleanedText.substring(jsonStart, jsonEnd + 1);
//                        Log.d(TAG, "Extracted JSON: " + jsonText);
//
//                        // Parse JSON directly using Gson
//                        Type listType = new TypeToken<List<SubTaskJson>>(){}.getType();
//                        List<SubTaskJson> subtaskJsonList = gson.fromJson(jsonText, listType);
//
//                        if (subtaskJsonList != null) {
//                            for (SubTaskJson subtaskJson : subtaskJsonList) {
//                                if (subtaskJson.title != null && !subtaskJson.title.trim().isEmpty()) {
//                                    SubTask subtask = new SubTask(
//                                            subtaskJson.title.trim(),
//                                            subtaskJson.description != null ? subtaskJson.description.trim() : "",
//                                            subtaskJson.estimatedTime != null ? subtaskJson.estimatedTime.trim() : "1 hour"
//                                    );
//                                    subtasks.add(subtask);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error parsing subtasks from response", e);
//        }
//
//        Log.d(TAG, "Parsed " + subtasks.size() + " subtasks");
//
//        if (subtasks.isEmpty()) {
//            Log.w(TAG, "No subtasks parsed, returning fallback");
//            return getFallbackSubtasks();
//        }
//
//        return subtasks;
//    }
//
//    // Helper class for JSON parsing
//    private static class SubTaskJson {
//        String title;
//        String description;
//        String estimatedTime;
//    }
//
//    private List<SubTask> getFallbackSubtasks() {
//        List<SubTask> fallbackTasks = new ArrayList<>();
//        fallbackTasks.add(new SubTask("Analyze Requirements", "Understand the core objectives and deliverables.", "1 hour"));
//        fallbackTasks.add(new SubTask("Plan Execution", "Outline the steps and resources needed.", "30 minutes"));
//        fallbackTasks.add(new SubTask("Execute Main Tasks", "Implement the core work required.", "2 hours"));
//        fallbackTasks.add(new SubTask("Review and Refine", "Check for completeness and accuracy.", "45 minutes"));
//        return fallbackTasks;
//    }
//
//    public void shutdown() {
//        if (executor != null && !executor.isShutdown()) {
//            executor.shutdown();
//        }
//    }
//
//    public interface OnSubtasksGeneratedListener {
//        void onSubtasksGenerated(List<SubTask> subtasks);
//        void onError(String error);
//    }
//}