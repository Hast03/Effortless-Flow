package com.example.effortlessflow.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FlaskResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("subtasks")
    private List<SubTask> subtasks;

    @SerializedName("error")
    private String error;

    @SerializedName("status")
    private String status;

    @SerializedName("endpoints")
    private List<String> endpoints;

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<SubTask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<SubTask> subtasks) {
        this.subtasks = subtasks;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }
}