package com.example.effortlessflow.model;

import com.google.gson.annotations.SerializedName;

public class FlaskRequest {

    @SerializedName("taskTitle")
    private String taskTitle;

    @SerializedName("taskDescription")
    private String taskDescription;

    @SerializedName("dueDate")
    private String dueDate;

    @SerializedName("effort")
    private String effort;

    public FlaskRequest(String taskTitle, String taskDescription, String dueDate, String effort) {
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.dueDate = dueDate;
        this.effort = effort;
    }

    // Getters and Setters
    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getEffort() {
        return effort;
    }

    public void setEffort(String effort) {
        this.effort = effort;
    }
}