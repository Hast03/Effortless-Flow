package com.example.effortlessflow.ui.tasklist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.effortlessflow.model.Task;
import com.example.effortlessflow.repository.TaskRepository;

import java.util.List;

public class TaskListViewModel extends ViewModel {
    private TaskRepository repository;
    private final MutableLiveData<String> operationMessage;

    public TaskListViewModel() {
        repository = TaskRepository.getInstance();
        operationMessage = new MutableLiveData<>();
    }
    public LiveData<List<Task>> getTasks() {
        return repository.getTasksLiveData();
    }
    public LiveData<String> getError() {
        return repository.getErrorLiveData();
    }

    public LiveData<String> getOperationMessage() {
        return operationMessage;
    }

    public void loadTasks() {
        repository.loadTasks();
    }

    public void updateTask(Task task) {
        repository.updateTask(task, new TaskRepository.OnTaskOperationListener() {
            @Override
            public void onSuccess() {
                operationMessage.setValue("Task updated successfully");
            }

            @Override
            public void onFailure(String error) {
                operationMessage.setValue(error);
            }
        });
    }

    public void updateSubTask(Task parentTask) {
        // Update the entire task (which contains the updated subtask)
        repository.updateTask(parentTask, new TaskRepository.OnTaskOperationListener() {
            @Override
            public void onSuccess() {
                operationMessage.setValue("Subtask updated successfully");
            }

            @Override
            public void onFailure(String error) {
                operationMessage.setValue(error);
            }
        });
    }

    public void deleteTask(String taskId) {
        repository.deleteTask(taskId, new TaskRepository.OnTaskOperationListener() {
            @Override
            public void onSuccess() {
                operationMessage.setValue("Task deleted successfully");
            }

            @Override
            public void onFailure(String error) {
                operationMessage.setValue(error);
            }
        });
    }

    public void clearCompletedTasks() {
        repository.clearCompletedTasks(new TaskRepository.OnTaskOperationListener() {
            @Override
            public void onSuccess() {
                operationMessage.setValue("Completed tasks cleared");
            }

            @Override
            public void onFailure(String error) {
                operationMessage.setValue(error);
            }
        });
    }
}