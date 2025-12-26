package com.example.effortlessflow.ui.insights;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.effortlessflow.model.Task;
import com.example.effortlessflow.repository.TaskRepository;
import java.util.List;

public class InsightsViewModel extends ViewModel {
    private TaskRepository repository;

    public InsightsViewModel() {
        repository = TaskRepository.getInstance();
    }

    public LiveData<List<Task>> getTasks() {
        return repository.getTasksLiveData();
    }

    public void loadTasks() {
        repository.loadTasks();
    }
}