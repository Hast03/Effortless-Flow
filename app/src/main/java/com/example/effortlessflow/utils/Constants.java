package com.example.effortlessflow.utils;

public class Constants {
    // Firebase Collections
    public static final String USERS_COLLECTION = "users";
    public static final String TASKS_COLLECTION = "tasks";

    // Task Effort Levels
    public static final String EFFORT_LOW = "Low";
    public static final String EFFORT_MEDIUM = "Medium";
    public static final String EFFORT_HIGH = "High";

    // Notification Constants
    public static final String NOTIFICATION_CHANNEL = "task_notifications";
    public static final int OVERDUE_NOTIFICATION_ID = 1001;

    // Shared Preferences
    public static final String PREFS_NAME = "TaskManagerPrefs";
    public static final String PREF_DARK_MODE = "dark_mode";
    public static final String PREF_NOTIFICATIONS = "notifications_enabled";
}
