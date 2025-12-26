package com.example.effortlessflow.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.example.effortlessflow.R;
import com.example.effortlessflow.model.SubTask;
import com.example.effortlessflow.model.Task;
import com.example.effortlessflow.repository.TaskRepository;
import com.example.effortlessflow.services.FlaskAIService;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskDetailsActivity extends AppCompatActivity {

    private TextInputEditText titleEditText, descriptionEditText;
    private AutoCompleteTextView dueDateEditText, effortSpinner;
    private Button aiSuggestionsBtn, addSubtasksBtn, regenerateBtn;
    private LinearLayout aiSuggestionContainer, subtasksContainer, aiActionLayout;
    private LinearLayout addedSubtasksContainer; // Container for added subtasks
    private CheckBox selectAllCheckBox;
    private ProgressBar aiLoadingPb;
    private TextView aiLoadingText;
    private Toolbar toolbar;

    private List<CheckBox> subtaskCheckboxes = new ArrayList<>();
    private List<TextView> subtaskTimeViews = new ArrayList<>();
    private List<SubTask> addedSubtasks = new ArrayList<>(); // Track added subtasks
    private List<Integer> selectedSubtaskIndices = new ArrayList<>(); // Track selection order
    private List<Integer> removedSubtaskIndices = new ArrayList<>(); // Track removed subtasks
    private List<View> addedSubtaskViews = new ArrayList<>(); // Track subtask views in container
    private Date selectedDate;
    private String taskId;
    private boolean editMode;
    private Task currentTask;

    // Flask AI Service instance
    private FlaskAIService flaskAIService;
    private List<SubTask> currentGeneratedSubtasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        initViews();
        setupToolbar();
        setupSpinners();
        setupClickListeners();
        checkIntentData();

        // Initialize Flask AI Service
        flaskAIService = new FlaskAIService();
    }

    private void initViews() {
        titleEditText = findViewById(R.id.task_title_et);
        descriptionEditText = findViewById(R.id.task_description_et);
        dueDateEditText = findViewById(R.id.task_due_date_et);
        effortSpinner = findViewById(R.id.task_effort_spinner);
        aiSuggestionsBtn = findViewById(R.id.ai_suggestions_btn);
        aiSuggestionContainer = findViewById(R.id.ai_suggestion_container);
        subtasksContainer = findViewById(R.id.subtasks_list_container);
        addedSubtasksContainer = findViewById(R.id.added_subtasks_container);
        selectAllCheckBox = findViewById(R.id.select_all_subtasks_cb);
        addSubtasksBtn = findViewById(R.id.add_subtasks_btn);
        regenerateBtn = findViewById(R.id.regenerate_subtasks_btn);
        aiActionLayout = findViewById(R.id.ai_suggestion_actions_layout);
        aiLoadingPb = findViewById(R.id.ai_loading_pb);
        aiLoadingText = findViewById(R.id.ai_loading_text_tv);
        toolbar = findViewById(R.id.task_details_toolbar);

        // Initialize subtask views
        initSubtaskViews();
    }

    private void initSubtaskViews() {
        subtaskCheckboxes.add(findViewById(R.id.subtask_1_cb));
        subtaskCheckboxes.add(findViewById(R.id.subtask_2_cb));
        subtaskCheckboxes.add(findViewById(R.id.subtask_3_cb));
        subtaskCheckboxes.add(findViewById(R.id.subtask_4_cb));
        subtaskCheckboxes.add(findViewById(R.id.subtask_5_cb));

        subtaskTimeViews.add(findViewById(R.id.subtask_1_time_tv));
        subtaskTimeViews.add(findViewById(R.id.subtask_2_time_tv));
        subtaskTimeViews.add(findViewById(R.id.subtask_3_time_tv));
        subtaskTimeViews.add(findViewById(R.id.subtask_4_time_tv));
        subtaskTimeViews.add(findViewById(R.id.subtask_5_time_tv));
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupSpinners() {
        // Effort spinner
        String[] efforts = {"Low", "Medium", "High"};
        ArrayAdapter<String> effortAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, efforts);
        effortSpinner.setAdapter(effortAdapter);
        effortSpinner.setText("Medium", false);
    }

    private void setupClickListeners() {
        dueDateEditText.setOnClickListener(v -> showDatePicker());

        aiSuggestionsBtn.setOnClickListener(v -> generateAISuggestions());

        addSubtasksBtn.setOnClickListener(v -> addSelectedSubtasksToContainer());

        regenerateBtn.setOnClickListener(v -> regenerateSubtasks());

        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CheckBox cb : subtaskCheckboxes) {
                cb.setChecked(isChecked);
            }
            updateSelectAllState();
        });

        // Setup individual checkbox listeners to update select all state
        for (CheckBox cb : subtaskCheckboxes) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> updateSelectAllState());
        }
    }

    // Method to update select all checkbox state based on individual checkboxes
    private void updateSelectAllState() {
        boolean allChecked = true;
        boolean anyChecked = false;

        for (int i = 0; i < subtaskCheckboxes.size(); i++) {
            CheckBox cb = subtaskCheckboxes.get(i);
            // Skip removed subtasks
            if (removedSubtaskIndices.contains(i)) {
                continue;
            }

            if (cb.isChecked()) {
                anyChecked = true;
            } else {
                allChecked = false;
            }
        }

        selectAllCheckBox.setChecked(allChecked);
    }

    private void checkIntentData() {
        taskId = getIntent().getStringExtra("TASK_ID");
        editMode = getIntent().getBooleanExtra("EDIT_MODE", false);

        if (editMode && taskId != null) {
            toolbar.setTitle("Edit Task");
        } else {
            toolbar.setTitle("New Task");
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDate = selectedCalendar.getTime();

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    dueDateEditText.setText(sdf.format(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // Real AI generation using Flask API
    private void generateAISuggestions() {
        String title = titleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a task title first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get other fields
        String description = descriptionEditText.getText().toString().trim();
        String effort = effortSpinner.getText().toString();

        // Show loading state
        showLoadingState(true);
        // Toast.makeText(this, "Generating AI subtasks...", Toast.LENGTH_SHORT).show();

        // Call Flask API
        flaskAIService.generateSubtasks(
                title,
                description,
                selectedDate,
                effort,
                new FlaskAIService.OnSubtasksGeneratedListener() {
                    @Override
                    public void onSubtasksGenerated(List<SubTask> subtasks) {
                        // Success! Display the AI-generated subtasks
                        currentGeneratedSubtasks = subtasks;
                        displayAISubtasks(subtasks);
                        showLoadingState(false);
                        Toast.makeText(TaskDetailsActivity.this,
                                "AI generated " + subtasks.size() + " subtasks!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        // Error occurred, but fallback subtasks are already provided
                        showLoadingState(false);
                        Toast.makeText(TaskDetailsActivity.this,
                                "Using default subtasks: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // Display AI-generated subtasks
    private void displayAISubtasks(List<SubTask> subtasks) {
        // Show the suggestion container
        aiSuggestionContainer.setVisibility(View.VISIBLE);
        selectAllCheckBox.setVisibility(View.VISIBLE);
        aiActionLayout.setVisibility(View.VISIBLE);

        // Hide all subtask containers first
        hideAllSubtasks();

        // Clear removed subtasks list for new generation
        removedSubtaskIndices.clear();

        // Display each subtask
        for (int i = 0; i < subtasks.size() && i < subtaskCheckboxes.size(); i++) {
            SubTask subtask = subtasks.get(i);

            // Set subtask title and description
            subtaskCheckboxes.get(i).setText(subtask.getTitle());

            // Set estimated time
            String timeText = "Estimated time: " + subtask.getEstimatedTime();
            subtaskTimeViews.get(i).setText(timeText);

            // Show the container
            findViewById(getSubtaskContainerId(i)).setVisibility(View.VISIBLE);
        }
    }

    private int getSubtaskContainerId(int index) {
        switch (index) {
            case 0: return R.id.subtask_1_container;
            case 1: return R.id.subtask_2_container;
            case 2: return R.id.subtask_3_container;
            case 3: return R.id.subtask_4_container;
            case 4: return R.id.subtask_5_container;
            default: return R.id.subtask_1_container;
        }
    }

    private void showLoadingState(boolean loading) {
        aiLoadingPb.setVisibility(loading ? View.VISIBLE : View.GONE);
        aiLoadingText.setVisibility(loading ? View.VISIBLE : View.GONE);
        aiSuggestionsBtn.setEnabled(!loading);
        regenerateBtn.setEnabled(!loading);

        if (loading) {
            aiLoadingText.setText("AI is generating subtasks...");
        }
    }

    // Add selected subtasks to container in the order they were selected
    private void addSelectedSubtasksToContainer() {
        List<SubTask> selectedSubtasks = new ArrayList<>();
        List<Integer> selectedIndices = new ArrayList<>();

        // Find all selected subtasks and their indices
        for (int i = 0; i < subtaskCheckboxes.size(); i++) {
            CheckBox cb = subtaskCheckboxes.get(i);
            if (cb.isChecked() && cb.getText().length() > 0) {
                if (i < currentGeneratedSubtasks.size()) {
                    // Use the actual SubTask object from API
                    selectedSubtasks.add(currentGeneratedSubtasks.get(i));
                    selectedIndices.add(i);

                    // Mark this subtask as removed
                    removedSubtaskIndices.add(i);

                    // Hide the container for this subtask
                    findViewById(getSubtaskContainerId(i)).setVisibility(View.GONE);
                } else {
                    // Fallback: extract just the time value
                    String timeText = subtaskTimeViews.get(i).getText().toString();
                    // Remove "Estimated time: " prefix if present
                    timeText = timeText.replace("Estimated time: ", "").trim();

                    SubTask subTask = new SubTask(
                            cb.getText().toString(),
                            "",
                            timeText
                    );
                    selectedSubtasks.add(subTask);
                    selectedIndices.add(i);

                    // Mark this subtask as removed
                    removedSubtaskIndices.add(i);

                    // Hide the container for this subtask
                    findViewById(getSubtaskContainerId(i)).setVisibility(View.GONE);
                }
            }
        }

        if (selectedSubtasks.isEmpty()) {
            Toast.makeText(this, "Please select at least one subtask", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to the list of added subtasks in the order they were selected
        addedSubtasks.addAll(selectedSubtasks);
        selectedSubtaskIndices.addAll(selectedIndices);

        // Display the added subtasks
        displayAddedSubtasks();

        // Check if all subtasks have been removed
        boolean allRemoved = true;
        for (int i = 0; i < currentGeneratedSubtasks.size(); i++) {
            if (!removedSubtaskIndices.contains(i)) {
                allRemoved = false;
                break;
            }
        }

        // If all subtasks have been removed, hide the AI suggestion container
        if (allRemoved) {
            aiSuggestionContainer.setVisibility(View.GONE);
            selectAllCheckBox.setVisibility(View.GONE);
            aiActionLayout.setVisibility(View.GONE);
        }

        // Clear selections
        for (CheckBox cb : subtaskCheckboxes) {
            cb.setChecked(false);
        }

        Toast.makeText(this, "Subtasks added to task", Toast.LENGTH_SHORT).show();
    }

    // Display added subtasks in the container with minus buttons
    private void displayAddedSubtasks() {
        if (addedSubtasks.isEmpty()) {
            addedSubtasksContainer.setVisibility(View.GONE);
            return;
        }

        addedSubtasksContainer.setVisibility(View.VISIBLE);

        // Clear previous views
        addedSubtaskViews.clear();

        // Find the container for subtasks with buttons
        LinearLayout subtasksWithButtonsContainer = findViewById(R.id.stask_btn_cont);

        // If the container doesn't exist, we need to create it or use the existing one
        if (subtasksWithButtonsContainer == null) {
            // We need to add this container to your XML layout
            // For now, let's use the addedSubtasksContainer directly
            subtasksWithButtonsContainer = addedSubtasksContainer;
        } else {
            // Clear existing views
            subtasksWithButtonsContainer.removeAllViews();
        }

        // Add each subtask with a minus button
        for (int i = 0; i < addedSubtasks.size(); i++) {
            // Create a horizontal layout for each subtask
            LinearLayout subtaskLayout = new LinearLayout(this);
            subtaskLayout.setOrientation(LinearLayout.HORIZONTAL);
            subtaskLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            // Create minus button
            ImageButton minusButton = new ImageButton(this);
            minusButton.setImageResource(R.drawable.ic_remove);
            minusButton.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_remove_background));
//            minusButton.setLayoutParams(new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));
            // Set smaller size for the button
            int buttonSize = (int) (24 * getResources().getDisplayMetrics().density); // 24dp
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    buttonSize, buttonSize);
            buttonParams.setMargins(0, 8, 8, 8); // Add margins for proper spacing
            minusButton.setLayoutParams(buttonParams);
            minusButton.setPadding(4, 4, 4, 4);

            // Create text view for subtask
            TextView subtaskTextView = new TextView(this);
            SubTask subtask = addedSubtasks.get(i);
            String subtaskText = subtask.getTitle();

            if (subtask.getEstimatedTime() != null && !subtask.getEstimatedTime().isEmpty()) {
                subtaskText += " (" + subtask.getEstimatedTime() + ")";
            }

            subtaskTextView.setText(subtaskText);
            subtaskTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            subtaskTextView.setPadding(0, 8, 0, 8);
            subtaskTextView.setTextSize(16);
            subtaskTextView.setTextColor(ContextCompat.getColor(this, R.color.dark_charcoal_text));

            // Add views to the layout
            subtaskLayout.addView(minusButton);
            subtaskLayout.addView(subtaskTextView);

            // Add the layout to the container
            subtasksWithButtonsContainer.addView(subtaskLayout);

            // Store the view for later reference
            addedSubtaskViews.add(subtaskLayout);

            // Set click listener for the minus button
            final int index = i;
            minusButton.setOnClickListener(v -> removeSubtaskFromContainer(index));
        }
    }

    // Remove a subtask from the container and return it to the generated area
    private void removeSubtaskFromContainer(int index) {
        if (index < 0 || index >= addedSubtasks.size()) {
            return;
        }

        // Get the subtask to remove
        SubTask subtask = addedSubtasks.get(index);
        int originalIndex = selectedSubtaskIndices.get(index);

        // Remove from the lists
        addedSubtasks.remove(index);
        selectedSubtaskIndices.remove(index);

        // Remove from the removed subtasks list
        removedSubtaskIndices.remove(Integer.valueOf(originalIndex));

        // Show the container for this subtask again
        findViewById(getSubtaskContainerId(originalIndex)).setVisibility(View.VISIBLE);

        // Show the AI suggestion container if it was hidden
        if (aiSuggestionContainer.getVisibility() == View.GONE) {
            aiSuggestionContainer.setVisibility(View.VISIBLE);
            selectAllCheckBox.setVisibility(View.VISIBLE);
            aiActionLayout.setVisibility(View.VISIBLE);
        }

        // Redisplay the added subtasks
        displayAddedSubtasks();

        Toast.makeText(this, "Subtask removed from task", Toast.LENGTH_SHORT).show();
    }

    // Regenerate with real API call (same as generateAISuggestions)
    private void regenerateSubtasks() {
        // Hide current suggestions and regenerate
        hideAllSubtasks();
        selectAllCheckBox.setChecked(false);
        // Hide the AI suggestion container during loading
        aiSuggestionContainer.setVisibility(View.GONE);
        selectAllCheckBox.setVisibility(View.GONE);
        aiActionLayout.setVisibility(View.GONE);

        // Show loading state
        String title = titleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a task title first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get other fields
        String description = descriptionEditText.getText().toString().trim();
        String effort = effortSpinner.getText().toString();

        // Show loading state
        showLoadingState(true);
        Toast.makeText(this, "Regenerating AI subtasks...", Toast.LENGTH_SHORT).show();

        // Call Flask API
        flaskAIService.generateSubtasks(
                title,
                description,
                selectedDate,
                effort,
                new FlaskAIService.OnSubtasksGeneratedListener() {
                    @Override
                    public void onSubtasksGenerated(List<SubTask> subtasks) {
                        // Success! Display the AI-generated subtasks
                        currentGeneratedSubtasks = subtasks;
                        displayAISubtasks(subtasks);
                        showLoadingState(false);
                        Toast.makeText(TaskDetailsActivity.this,
                                "AI regenerated " + subtasks.size() + " subtasks!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        // Error occurred, but fallback subtasks are already provided
                        showLoadingState(false);
                        Toast.makeText(TaskDetailsActivity.this,
                                "Using default subtasks: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void hideAllSubtasks() {
        for (int i = 0; i < 5; i++) {
            findViewById(getSubtaskContainerId(i)).setVisibility(View.GONE);
        }
    }

    private void saveTaskWithSubtasks(List<SubTask> subtasks) {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String effort = effortSpinner.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = editMode && currentTask != null ? currentTask : new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(selectedDate);
        task.setEffort(effort);
        task.setSubTasks(subtasks);

        TaskRepository repository = TaskRepository.getInstance();
        TaskRepository.OnTaskOperationListener listener = new TaskRepository.OnTaskOperationListener() {
            @Override
            public void onSuccess() {
                if (editMode) {
                    Toast.makeText(TaskDetailsActivity.this, "Task updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TaskDetailsActivity.this, "Task created successfully!", Toast.LENGTH_SHORT).show();
                }
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(TaskDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        };

        if (editMode) {
            repository.updateTask(task, listener);
        } else {
            repository.addTask(task, listener);
        }
    }

    // Check if task has unsaved changes
    private boolean hasUnsavedChanges() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String effort = effortSpinner.getText().toString();

        // For both new and edit tasks, check if any field is filled
        if (!title.isEmpty() || !description.isEmpty() ||
                selectedDate != null || !addedSubtasks.isEmpty()) {
            return true;
        }

        return false;
    }

    // Save task method for menu
    private void saveTask() {
        saveTaskWithSubtasks(addedSubtasks);
    }

    // Handle exit with confirmation
    private void handleExit() {
        if (hasUnsavedChanges()) {
            if (editMode) {
                // For existing task, show discard changes dialog
                new AlertDialog.Builder(this)
                        .setTitle("Discard Changes")
                        .setMessage("Are you sure you want to discard your changes?")
                        .setPositiveButton("Discard", (dialog, which) -> finish())
                        .setNegativeButton("Keep Editing", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                // For new task, show save before exit dialog
                new AlertDialog.Builder(this)
                        .setTitle("Save Task")
                        .setMessage("Do you want to save this task before exiting?")
                        .setPositiveButton("Save", (dialog, which) -> saveTask())
                        .setNegativeButton("Don't Save", (dialog, which) -> finish())
                        .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveTask();
            return true;
        } else if (id == R.id.action_exit) {
            handleExit();
            return true;
        } else if (id == android.R.id.home) {
            handleExit(); // Use handleExit instead of onBackPressed to check for unsaved changes
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}