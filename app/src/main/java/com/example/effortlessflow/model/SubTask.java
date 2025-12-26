//package com.example.effortlessflow.model;
//
//import com.google.firebase.firestore.PropertyName;
//import java.util.Date;
//
//public class SubTask {
//    private String id;
//    private String title;
//    private String description;
//    private boolean completed;
//    private Date createdAt;
//    private Date completedAt;
//    private String estimatedTime;
//
//    // Required empty constructor for Firestore
//    public SubTask() {
//        this.completed = false;
//        this.createdAt = new Date();
//    }
//
//    public SubTask(String title, String description, String estimatedTime) {
//        this();
//        this.title = title;
//        this.description = description;
//        this.estimatedTime = estimatedTime;
//    }
//
//    // Getters and Setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }
//
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//
//    public boolean isCompleted() { return completed; }
//    public void setCompleted(boolean completed) {
//        this.completed = completed;
//        if (completed && completedAt == null) {
//            this.completedAt = new Date();
//        } else if (!completed) {
//            this.completedAt = null;
//        }
//    }
//    @PropertyName("createdAt")
//    public Date getCreatedAt() { return createdAt; }
//    @PropertyName("createdAt")
//    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
//
//    @PropertyName("completedAt")
//    public Date getCompletedAt() { return completedAt; }
//    @PropertyName("completedAt")
//    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
//
//    @PropertyName("estimatedTime")
//    public String getEstimatedTime() { return estimatedTime; }
//    @PropertyName("estimatedTime")
//    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
//}



//package com.example.effortlessflow.model;
//
//import com.google.gson.annotations.SerializedName;
//
//public class SubTask {
//
//    @SerializedName("title")
//    private String title;
//
//    @SerializedName("description")
//    private String description;
//
//    @SerializedName("estimatedTime")
//    private String estimatedTime;
//
//    private boolean isCompleted;
//
//    public SubTask(String title, String description, String estimatedTime) {
//        this.title = title;
//        this.description = description;
//        this.estimatedTime = estimatedTime;
//        this.isCompleted = false;
//    }
//
//    // Getters and Setters
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getEstimatedTime() {
//        return estimatedTime;
//    }
//
//    public void setEstimatedTime(String estimatedTime) {
//        this.estimatedTime = estimatedTime;
//    }
//
//    public boolean isCompleted() {
//        return isCompleted;
//    }
//
//    public void setCompleted(boolean completed) {
//        isCompleted = completed;
//    }
//}








package com.example.effortlessflow.model;

import com.google.firebase.firestore.PropertyName;
import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class SubTask {
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    private boolean completed;
    private Date createdAt;
    private Date completedAt;

    @SerializedName("estimatedTime")
    private String estimatedTime;

    // Required empty constructor for Firestore
    public SubTask() {
        this.completed = false;
        this.createdAt = new Date();
    }

    public SubTask(String title, String description, String estimatedTime) {
        this();
        this.title = title;
        this.description = description;
        this.estimatedTime = estimatedTime;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("title")
    public String getTitle() { return title; }
    @PropertyName("title")
    public void setTitle(String title) { this.title = title; }

    @PropertyName("description")
    public String getDescription() { return description; }
    @PropertyName("description")
    public void setDescription(String description) { this.description = description; }

    @PropertyName("completed")
    public boolean isCompleted() { return completed; }
    @PropertyName("completed")
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && completedAt == null) {
            this.completedAt = new Date();
        } else if (!completed) {
            this.completedAt = null;
        }
    }

    @PropertyName("createdAt")
    public Date getCreatedAt() { return createdAt; }
    @PropertyName("createdAt")
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @PropertyName("completedAt")
    public Date getCompletedAt() { return completedAt; }
    @PropertyName("completedAt")
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    @PropertyName("estimatedTime")
    public String getEstimatedTime() { return estimatedTime; }
    @PropertyName("estimatedTime")
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

    public int getEstimatedDuration() {
        // Parse the estimated time string to get minutes
        if (estimatedTime == null || estimatedTime.isEmpty()) {
            return 15; // Default 15 minutes
        }
        try {
            // Try to parse different time formats
            if (estimatedTime.contains("min")) {
                // Format: "15 min"
                String[] parts = estimatedTime.split(" ");
                return Integer.parseInt(parts[0]);
            } else if (estimatedTime.contains("h")) {
                // Format: "1 h" or "1 h 30 min"
                String[] parts = estimatedTime.split(" ");
                int hours = Integer.parseInt(parts[0]);
                int minutes = parts.length > 1 ? Integer.parseInt(parts[1].replace("min", "")) : 0;
                return hours * 60 + minutes;
            } else if (estimatedTime.contains("d")) {
                // Format: "2 d" or "2 d 1 h"
                String[] parts = estimatedTime.split(" ");
                int days = Integer.parseInt(parts[0]);
                int hours = parts.length > 1 ? Integer.parseInt(parts[1].replace("h", "")) : 0;
                return days * 24 * 60 + hours * 60;
            } else {
                // Default fallback
                return 15;
            }
        } catch (NumberFormatException e) {
            return 15; // Default fallback
        }
    }
}