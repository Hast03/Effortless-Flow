package com.example.effortlessflow.ui.tasklist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.effortlessflow.MainActivity;
import com.example.effortlessflow.R;
import com.example.effortlessflow.ui.TaskDetailsActivity;
import com.example.effortlessflow.model.Task;
import com.example.effortlessflow.model.SubTask;
import com.example.effortlessflow.utils.DateUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private TaskListViewModel viewModel;
    private TaskAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout noTasksLayout;
    private FloatingActionButton fabAddTask;
    private MaterialToolbar toolbar;
    private List<Task> allTasks = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_task_list, container, false);

        initViews(root);
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();

        return root;
    }

    private void initViews(View root) {
        recyclerView = root.findViewById(R.id.tasks_recycler_view);
        noTasksLayout = root.findViewById(R.id.no_tasks_layout);
        fabAddTask = root.findViewById(R.id.FloatActBtn_add_task);
        toolbar = root.findViewById(R.id.toolbar_task_list);

        // Setup toolbar - Fixed to avoid action bar conflict
        if (getActivity() != null && toolbar != null) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            // Only set support action bar if there isn't one already
            if (activity.getSupportActionBar() == null) {
                activity.setSupportActionBar(toolbar);
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new TaskAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TaskListViewModel.class);

        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks != null ? new ArrayList<>(tasks) : new ArrayList<>();
            updateUI(allTasks);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadTasks();
    }

    private void setupClickListeners() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
            startActivity(intent);
        });
    }

    private void updateUI(List<Task> tasks) {
        if (tasks.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noTasksLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
            adapter.updateTasks(tasks);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.task_list_menu, menu);

        // Setup search
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setQueryHint("Search tasks...");
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterTasks(query);
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterTasks(newText);
                    return true;
                }
            });

            // Handle search view expand/collapse
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    showAllTasks(); // Reset filter when search is collapsed
                    return true;
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.filter_all) {
            showAllTasks();
            return true;
        } else if (id == R.id.filter_pending) {
            filterByStatus(false);
            return true;
        } else if (id == R.id.filter_completed) {
            filterByStatus(true);
            return true;
        } else if (id == R.id.sort_by_priority) {
            sortByPriority();
            return true;
        } else if (id == R.id.sort_by_due_date) {
            sortByDueDate();
            return true;
        } else if (id == R.id.action_clear_completed) {
            clearCompletedTasks();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void filterTasks(String query) {
        if (query.isEmpty()) {
            updateUI(allTasks);
            return;
        }

        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    (task.getDescription() != null && task.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                filteredTasks.add(task);
            }
        }
        updateUI(filteredTasks);
    }

    private void showAllTasks() {
        updateUI(allTasks);
    }

    private void filterByStatus(boolean completed) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.isCompleted() == completed) {
                filteredTasks.add(task);
            }
        }
        updateUI(filteredTasks);
    }

    private void sortByPriority() {
        List<Task> sortedTasks = new ArrayList<>(allTasks);
        sortedTasks.sort((t1, t2) -> Integer.compare(t2.getEffortPriority(), t1.getEffortPriority()));
        updateUI(sortedTasks);
    }

    private void sortByDueDate() {
        List<Task> sortedTasks = new ArrayList<>(allTasks);
        sortedTasks.sort((t1, t2) -> {
            if (t1.getDueDate() == null) return 1;
            if (t2.getDueDate() == null) return -1;
            return t1.getDueDate().compareTo(t2.getDueDate());
        });
        updateUI(sortedTasks);
    }

    private void clearCompletedTasks() {
        viewModel.clearCompletedTasks();
        Toast.makeText(getContext(), "Completed tasks cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskClick(Task task) {
        // Changed to show view dialog instead of opening edit activity
        showTaskViewDialog(task);
    }

    @Override
    public void onTaskView(Task task) {
        showTaskViewDialog(task);
    }

    @Override
    public void onTaskCheckChanged(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        viewModel.updateTask(task);

        if (isChecked) {
            Toast.makeText(getContext(), "Task completed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskDelete(Task task) {
        // Added confirmation dialog before deleting
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteTask(task.getId());
                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showTaskViewDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_view_task, null);

        // Initialize dialog views
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_task_title_tv);
        TextView dialogDescription = dialogView.findViewById(R.id.dialog_task_description_tv);
        TextView dialogDueDate = dialogView.findViewById(R.id.dialog_task_due_date_tv);
        TextView dialogEffortText = dialogView.findViewById(R.id.dialog_task_effort_text);
        View dialogEffortDot = dialogView.findViewById(R.id.dialog_task_effort_dot);
        LinearLayout dialogSubtasksContainer = dialogView.findViewById(R.id.dialog_subtasks_container);
        ImageView dialogCloseBtn = dialogView.findViewById(R.id.dialog_close_btn);
        ImageView dialogOptionsIcon = dialogView.findViewById(R.id.dialog_task_options_icon);
        Button completeTaskBtn = dialogView.findViewById(R.id.btn_complete_task);

        // Populate dialog with task data
        dialogTitle.setText(task.getTitle());

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            dialogDescription.setText(task.getDescription());
        } else {
            dialogDescription.setText("No description provided");
        }

        if (task.getDueDate() != null) {
            dialogDueDate.setText(DateUtils.formatDate(task.getDueDate()));
        } else {
            dialogDueDate.setText("No due date");
        }

        dialogEffortText.setText(task.getEffort());
        setEffortColor(dialogEffortDot, task.getEffort());

        // Load subtasks
        List<SubTask> subtasks = task.getSubTasks();
        if (subtasks != null) {
            android.util.Log.d("TaskDialog", "Loading " + subtasks.size() + " subtasks");
        } else {
            android.util.Log.d("TaskDialog", "Subtasks list is NULL");
        }
        loadSubtasksInDialog(dialogSubtasksContainer, subtasks, task);

        // Handle complete button
        if (task.isCompleted()) {
            completeTaskBtn.setText("Mark as Incomplete");
            completeTaskBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.medium_gray_text));
        } else {
            completeTaskBtn.setText("Complete Task");
            completeTaskBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.aqua_accent_dark));
        }

        AlertDialog dialog = builder.setView(dialogView).create();

        // Set click listeners
        dialogCloseBtn.setOnClickListener(v -> dialog.dismiss());

        dialogOptionsIcon.setOnClickListener(v -> {
            showTaskOptionsInDialog(v, task, dialog);
        });

        completeTaskBtn.setOnClickListener(v -> {
            task.setCompleted(!task.isCompleted());
            viewModel.updateTask(task);
            dialog.dismiss();
            Toast.makeText(getContext(),
                    task.isCompleted() ? "Task completed!" : "Task marked incomplete",
                    Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void setEffortColor(View dot, String effort) {
        int color;
        switch (effort) {
            case "High":
                color = R.color.red_alert;
                break;
            case "Medium":
                color = R.color.orange_medium;
                break;
            case "Low":
            default:
                color = R.color.aqua_accent;
                break;
        }
        dot.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), color));
    }

    private void loadSubtasksInDialog(LinearLayout container, List<SubTask> subtasks, Task parentTask) {
        container.removeAllViews();

        if (subtasks == null || subtasks.isEmpty()) {
            TextView noSubtasks = new TextView(getContext());
            noSubtasks.setText("No subtasks");
            noSubtasks.setTextColor(ContextCompat.getColor(getContext(), R.color.medium_gray_text));
            noSubtasks.setPadding(16, 16, 16, 16);
            container.addView(noSubtasks);
            return;
        }

        for (SubTask subtask : subtasks) {
            View subtaskView = LayoutInflater.from(getContext()).inflate(R.layout.item_subtask, container, false);

            CheckBox checkbox = subtaskView.findViewById(R.id.subtask_checkbox);
            ImageView completedIcon = subtaskView.findViewById(R.id.subtask_completed_icon);
            TextView title = subtaskView.findViewById(R.id.subtask_title_tv);
            TextView description = subtaskView.findViewById(R.id.subtask_description_tv);
            Button completeBtn = subtaskView.findViewById(R.id.btn_mark_subtask_complete);

            title.setText(subtask.getTitle());

            if (subtask.getDescription() != null && !subtask.getDescription().isEmpty()) {
                description.setVisibility(View.VISIBLE);
                description.setText(subtask.getDescription());
            }else {
                description.setVisibility(View.GONE);
            }

            if (subtask.isCompleted()) {
                checkbox.setVisibility(View.GONE);
                completedIcon.setVisibility(View.VISIBLE);
                completeBtn.setVisibility(View.GONE);
            } else {
                checkbox.setVisibility(View.VISIBLE);
                completedIcon.setVisibility(View.GONE);
                checkbox.setChecked(false);
                completeBtn.setVisibility(View.GONE);

                // When checkbox is clicked, show the complete button
                checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        completeBtn.setVisibility(View.VISIBLE);
                    } else {
                        completeBtn.setVisibility(View.GONE);
                    }
                });

                // When complete button is clicked, mark subtask as complete
                completeBtn.setOnClickListener(v -> {
                    subtask.setCompleted(true);
                    viewModel.updateSubTask(parentTask);

                    // Update UI immediately
                    checkbox.setVisibility(View.GONE);
                    completedIcon.setVisibility(View.VISIBLE);
                    completeBtn.setVisibility(View.GONE);

                    Toast.makeText(getContext(), "Subtask completed!", Toast.LENGTH_SHORT).show();
                });
            }
            container.addView(subtaskView);
        }
    }

    private void showTaskOptionsInDialog(View view, Task task, AlertDialog dialog) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.task_item_options);

        // Remove "View Details" from dialog menu since we're already viewing
        // popup.getMenu().removeItem(R.id.view_task);

        // Remove "Duplicate Task" option as requested
        // popup.getMenu().removeItem(R.id.duplicate_task);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.edit_task) {
                dialog.dismiss();
                Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
                intent.putExtra("TASK_ID", task.getId());
                intent.putExtra("EDIT_MODE", true);
                startActivity(intent);
                return true;
            } else if (id == R.id.delete_task) {
                dialog.dismiss();
                onTaskDelete(task);
                return true;
            }
            return false;
        });

        popup.show();
    }
}