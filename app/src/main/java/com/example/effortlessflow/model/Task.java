package com.example.effortlessflow.model;

import com.google.firebase.firestore.PropertyName;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Task {
    private String id;
    private String title;
    private String description;
    private Date dueDate;
    private String effort; // "Low", "Medium", "High"
    private boolean completed;
    private Date createdAt;
    private Date completedAt;
    private List<SubTask> subTasks;
    private String userId;

    // Required empty constructor for Firestore
    public Task() {
        this.subTasks = new ArrayList<>();
        this.completed = false;
        this.createdAt = new Date();
    }
    public Task(String title, String description, Date dueDate, String effort, String userId) {
        this();
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.effort = effort;
        this.userId = userId;
    }
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @PropertyName("dueDate")
    public Date getDueDate() { return dueDate; }
    @PropertyName("dueDate")
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public String getEffort() { return effort; }
    public void setEffort(String effort) { this.effort = effort; }
    public boolean isCompleted() { return completed; }
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

    @PropertyName("subTasks")
    public List<SubTask> getSubTasks() {
        if (subTasks == null) {
            subTasks = new ArrayList<>();
        }
        return subTasks;
    }
    @PropertyName("subTasks")
    public void setSubTasks(List<SubTask> subTasks) { this.subTasks = subTasks; }

    @PropertyName("userId")
    public String getUserId() { return userId; }
    @PropertyName("userId")
    public void setUserId(String userId) { this.userId = userId; }

    // Helper methods
    public boolean isOverdue() {
        return !completed && dueDate != null && dueDate.before(new Date());
    }

    public int getEstimatedDuration() {
        // Calculate estimated duration based on subtasks
        if (subTasks == null || subTasks.isEmpty()) {
            // Default duration based on effort level
            switch (effort) {
                case "High": return 60; // 1 hour
                case "Medium": return 30; // 30 minutes
                case "Low": return 15; // 15 minutes
                default: return 30;
            }
        }
        // If there are subtasks, calculate based on their estimated times
        if (subTasks != null && !subTasks.isEmpty()) {
            int totalMinutes = 0;
            for (SubTask subTask : subTasks) {
                // Each subtask contributes to the total estimated time
                totalMinutes += subTask.getEstimatedDuration();
            }
            return totalMinutes;
        }
        // Default return if no other condition is met
        return 30; // Default fallback
    }

    public int getCompletedSubTaskCount() {
        if (subTasks == null) return 0;
        int count = 0;
        for (SubTask subTask : subTasks) {
            if (subTask.isCompleted()) count++;
        }
        return count;
    }

    public int getTotalSubTaskCount() {
        return subTasks != null ? subTasks.size() : 0;
    }

    public double getCompletionPercentage() {
        if (getTotalSubTaskCount() == 0) return completed ? 100.0 : 0.0;
        return (getCompletedSubTaskCount() * 100.0) / getTotalSubTaskCount();
    }
    // Effort priority for sorting
    public int getEffortPriority() {
        switch (effort) {
            case "High": return 3;
            case "Medium": return 2;
            case "Low": return 1;
            default: return 0;
        }
    }
}
