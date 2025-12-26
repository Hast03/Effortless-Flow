package com.example.effortlessflow.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.effortlessflow.model.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskRepository {
    private static TaskRepository instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference tasksRef;
    private MutableLiveData<List<Task>> tasksLiveData;
    private MutableLiveData<String> errorLiveData;

    private TaskRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        tasksLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        initializeTasksReference();
    }

    public static synchronized TaskRepository getInstance() {
        if (instance == null) {
            instance = new TaskRepository();
        }
        return instance;
    }

    private void initializeTasksReference() {
        String userId = getCurrentUserId();
        if (userId != null) {
            tasksRef = db.collection("users").document(userId).collection("tasks");
        }
    }

    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public MutableLiveData<List<Task>> getTasksLiveData() {
        return tasksLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void loadTasks() {
        if (tasksRef == null) {
            initializeTasksReference();
            if (tasksRef == null) {
                errorLiveData.setValue("User not authenticated");
                return;
            }
        }

        tasksRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        errorLiveData.setValue("Error loading tasks: " + error.getMessage());
                        return;
                    }

                    List<Task> tasks = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Task task = doc.toObject(Task.class);
                            if (task != null) {
                                task.setId(doc.getId());
                                tasks.add(task);
                            }
                        }
                    }
                    tasksLiveData.setValue(tasks);
                });
    }

    public void addTask(Task task, OnTaskOperationListener listener) {
        if (tasksRef == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        task.setId(UUID.randomUUID().toString());
        task.setUserId(getCurrentUserId());

        tasksRef.document(task.getId())
                .set(task)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure("Failed to add task: " + e.getMessage()));
    }

    public void updateTask(Task task, OnTaskOperationListener listener) {
        if (tasksRef == null || task.getId() == null) {
            listener.onFailure("Invalid task or user not authenticated");
            return;
        }

        tasksRef.document(task.getId())
                .set(task)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure("Failed to update task: " + e.getMessage()));
    }

    public void deleteTask(String taskId, OnTaskOperationListener listener) {
        if (tasksRef == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        tasksRef.document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure("Failed to delete task: " + e.getMessage()));
    }

    public void clearCompletedTasks(OnTaskOperationListener listener) {
        if (tasksRef == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        tasksRef.whereEqualTo("completed", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        listener.onSuccess();
                        return;
                    }

                    int totalDocs = querySnapshot.size();
                    int[] deletedCount = {0};

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete()
                                .addOnCompleteListener(task -> {
                                    deletedCount[0]++;
                                    if (deletedCount[0] == totalDocs) {
                                        listener.onSuccess();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Failed to clear completed tasks: " + e.getMessage()));
    }

    // 🔹 New method: fetch tasks synchronously (for Worker)
    public List<Task> getAllTasksSync() throws Exception {
        if (tasksRef == null) {
            initializeTasksReference();
            if (tasksRef == null) {
                throw new Exception("User not authenticated");
            }
        }

        List<Task> tasks = new ArrayList<>();
        for (DocumentSnapshot doc : Tasks.await(tasksRef.get()).getDocuments()) {
            Task task = doc.toObject(Task.class);
            if (task != null) {
                task.setId(doc.getId());
                tasks.add(task);
            }
        }
        return tasks;
    }

    public interface OnTaskOperationListener {
        void onSuccess();
        void onFailure(String error);
    }
}
