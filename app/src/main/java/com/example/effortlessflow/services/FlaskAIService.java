package com.example.effortlessflow.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.effortlessflow.api.FlaskApiInterface;
import com.example.effortlessflow.model.FlaskRequest;
import com.example.effortlessflow.model.FlaskResponse;
import com.example.effortlessflow.model.SubTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FlaskAIService {

    // *** IMPORTANT: Replace with your actual Render URL ***
    private static final String BASE_URL = "https://gemini-flask-api-u0nd.onrender.com/";
    private static final String TAG = "FlaskAIService";

    private FlaskApiInterface apiInterface;
    private Handler mainHandler;

    public FlaskAIService() {
        mainHandler = new Handler(Looper.getMainLooper());

        // Create logging interceptor for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create OkHttpClient with increased timeout for cold starts
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)  // Increased for Render cold starts
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface = retrofit.create(FlaskApiInterface.class);
    }

    /**
     * Check if the Flask API is running
     */
    public void checkApiStatus(OnApiStatusListener listener) {
        Call<FlaskResponse> call = apiInterface.checkStatus();

        call.enqueue(new Callback<FlaskResponse>() {
            @Override
            public void onResponse(Call<FlaskResponse> call, Response<FlaskResponse> response) {
                mainHandler.post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        FlaskResponse flaskResponse = response.body();
                        Log.d(TAG, "API Status: " + flaskResponse.getMessage());
                        listener.onStatusChecked(true, flaskResponse.getMessage());
                    } else {
                        listener.onStatusChecked(false, "API returned error: " + response.code());
                    }
                });
            }

            @Override
            public void onFailure(Call<FlaskResponse> call, Throwable t) {
                Log.e(TAG, "API status check failed: " + t.getMessage(), t);
                mainHandler.post(() ->
                        listener.onStatusChecked(false, "Connection failed: " + t.getMessage())
                );
            }
        });
    }

    /**
     * Generate subtasks using Flask API
     */
    public void generateSubtasks(String taskTitle, String taskDescription, Date dueDate,
                                 String effort, OnSubtasksGeneratedListener listener) {

        // Format due date
        String dueDateStr = "";
        if (dueDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dueDateStr = sdf.format(dueDate);
        }

        // Create request
        FlaskRequest request = new FlaskRequest(
                taskTitle != null ? taskTitle : "",
                taskDescription != null ? taskDescription : "",
                dueDateStr,
                effort != null ? effort : "Medium"
        );

        Log.d(TAG, "Sending request to Flask API for task: " + taskTitle);

        // Make API call
        Call<FlaskResponse> call = apiInterface.generateSubtasks(request);

        call.enqueue(new Callback<FlaskResponse>() {
            @Override
            public void onResponse(Call<FlaskResponse> call, Response<FlaskResponse> response) {
                mainHandler.post(() -> handleApiResponse(response, listener));
            }

            @Override
            public void onFailure(Call<FlaskResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                mainHandler.post(() -> {
                    listener.onError("Network error: " + t.getMessage());
                    listener.onSubtasksGenerated(getFallbackSubtasks());
                });
            }
        });
    }

    private void handleApiResponse(Response<FlaskResponse> response, OnSubtasksGeneratedListener listener) {
        try {
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "API Response successful");

                FlaskResponse flaskResponse = response.body();
                List<SubTask> subtasks = flaskResponse.getSubtasks();

                if (subtasks != null && !subtasks.isEmpty()) {
                    Log.d(TAG, "Received " + subtasks.size() + " subtasks");
                    listener.onSubtasksGenerated(subtasks);
                } else {
                    Log.w(TAG, "No subtasks in response, using fallback");
                    String errorMsg = flaskResponse.getError() != null ?
                            flaskResponse.getError() : "No subtasks generated";
                    listener.onError(errorMsg);
                    listener.onSubtasksGenerated(getFallbackSubtasks());
                }
            } else {
                Log.e(TAG, "API Response failed: " + response.code() + " - " + response.message());
                String errorMessage = "API call failed: " + response.code();

                if (response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Error body: " + errorBody);
                        errorMessage += " - " + errorBody;
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }

                listener.onError(errorMessage);
                listener.onSubtasksGenerated(getFallbackSubtasks());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling API response", e);
            listener.onError("Error processing response: " + e.getMessage());
            listener.onSubtasksGenerated(getFallbackSubtasks());
        }
    }

    private List<SubTask> getFallbackSubtasks() {
        List<SubTask> fallbackTasks = new ArrayList<>();
        fallbackTasks.add(new SubTask("Analyze Requirements", "Understand the core objectives and deliverables.", "1 hour"));
        fallbackTasks.add(new SubTask("Plan Execution", "Outline the steps and resources needed.", "30 minutes"));
        fallbackTasks.add(new SubTask("Execute Main Tasks", "Implement the core work required.", "2 hours"));
        fallbackTasks.add(new SubTask("Review and Refine", "Check for completeness and accuracy.", "45 minutes"));
        return fallbackTasks;
    }

    // Listener interfaces
    public interface OnSubtasksGeneratedListener {
        void onSubtasksGenerated(List<SubTask> subtasks);
        void onError(String error);
    }

    public interface OnApiStatusListener {
        void onStatusChecked(boolean isRunning, String message);
    }
}