//package com.example.effortlessflow.ui.tasklist;
//
//import android.graphics.Paint;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CheckBox;
//import android.widget.ImageView;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.effortlessflow.R;
//import com.example.effortlessflow.model.Task;
//import com.example.effortlessflow.utils.DateUtils;
//import java.util.List;
//
//public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
//    private List<Task> tasks;
//    private OnTaskClickListener listener;
//
//    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
//        this.tasks = tasks;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_task, parent, false);
//        return new TaskViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
//        Task task = tasks.get(position);
//        holder.bind(task);
//    }
//
//    @Override
//    public int getItemCount() {
//        return tasks.size();
//    }
//
//    public void updateTasks(List<Task> newTasks) {
//        this.tasks = newTasks;
//        notifyDataSetChanged();
//    }
//
//    public class TaskViewHolder extends RecyclerView.ViewHolder {
//        private CheckBox taskCheckBox;
//        private TextView taskTitle;
//        private TextView taskDescription;
//        private TextView taskSubtaskSummary;
//        private TextView taskDueDate;
//        private View taskEffortDot;
//        private TextView taskEffortText;
//        private ImageView taskOptionsIcon;
//
//        public TaskViewHolder(@NonNull View itemView) {
//            super(itemView);
//            taskCheckBox = itemView.findViewById(R.id.task_checkBox);
//            taskTitle = itemView.findViewById(R.id.task_title_tv);
//            taskDescription = itemView.findViewById(R.id.task_description_tv);
//            taskSubtaskSummary = itemView.findViewById(R.id.task_subtask_summary_tv);
//            taskDueDate = itemView.findViewById(R.id.task_due_date_tv);
//            taskEffortDot = itemView.findViewById(R.id.task_effort_dot);
//            taskEffortText = itemView.findViewById(R.id.task_effort_text);
//            taskOptionsIcon = itemView.findViewById(R.id.task_options_icon);
//        }
//
//        public void bind(Task task) {
//            // Set basic info
//            taskTitle.setText(task.getTitle());
//            taskCheckBox.setChecked(task.isCompleted());
//
//            // Handle description
//            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
//                taskDescription.setVisibility(View.VISIBLE);
//                taskDescription.setText(task.getDescription());
//            } else {
//                taskDescription.setVisibility(View.GONE);
//            }
//
//            // Handle subtask summary
//            if (task.getTotalSubTaskCount() > 0) {
//                taskSubtaskSummary.setVisibility(View.VISIBLE);
//                taskSubtaskSummary.setText(String.format("Subtasks: %d/%d completed",
//                        task.getCompletedSubTaskCount(), task.getTotalSubTaskCount()));
//            } else {
//                taskSubtaskSummary.setVisibility(View.GONE);
//            }
//
//            // Handle due date
//            if (task.getDueDate() != null) {
//                taskDueDate.setVisibility(View.VISIBLE);
//                String dueDateText = DateUtils.getRelativeDateString(task.getDueDate());
//                taskDueDate.setText(dueDateText);
//
//                // Color coding for overdue tasks
//                if (task.isOverdue()) {
//                    taskDueDate.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red_alert));
//                } else {
//                    taskDueDate.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.aqua_accent));
//                }
//            } else {
//                taskDueDate.setVisibility(View.GONE);
//            }
//
//            // Handle effort indicator
//            setEffortIndicator(task.getEffort());
//
//            // Handle completed state styling
//            updateCompletedState(task.isCompleted());
//
//            // Set click listeners
//            taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                if (listener != null) {
//                    listener.onTaskCheckChanged(task, isChecked);
//                }
//                updateCompletedState(isChecked);
//            });
//
//            itemView.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onTaskClick(task);
//                }
//            });
//
//            taskOptionsIcon.setOnClickListener(v -> showTaskOptions(v, task));
//        }
//
//        private void setEffortIndicator(String effort) {
//            taskEffortText.setText(effort);
//            int color;
//            switch (effort) {
//                case "High":
//                    color = R.color.red_alert;
//                    break;
//                case "Medium":
//                    color = R.color.orange_medium;
//                    break;
//                case "Low":
//                default:
//                    color = R.color.aqua_accent;
//                    break;
//            }
//            taskEffortDot.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), color));
//        }
//
//        private void updateCompletedState(boolean completed) {
//            if (completed) {
//                taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                taskTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.medium_gray_text));
//                taskDescription.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.light_gray_hint));
//            } else {
//                taskTitle.setPaintFlags(taskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
//                taskTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.dark_charcoal_text));
//                taskDescription.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.medium_gray_text));
//            }
//        }
//
//        private void showTaskOptions(View view, Task task) {
//            PopupMenu popup = new PopupMenu(view.getContext(), view);
//            popup.inflate(R.menu.task_item_options);
//
//            popup.setOnMenuItemClickListener(item -> {
//                int id = item.getItemId();
//                if (id == R.id.view_task) {
//                    listener.onTaskView(task);
//                    return true;
//                } else if (id == R.id.edit_task) {
//                    listener.onTaskClick(task);
//                    return true;
//                } else if (id == R.id.delete_task) {
//                    listener.onTaskDelete(task);
//                    return true;
//                } else if (id == R.id.duplicate_task) {
//                    listener.onTaskDuplicate(task);
//                    return true;
//                }
//                return false;
//            });
//
//            popup.show();
//        }
//    }
//
//    public interface OnTaskClickListener {
//        void onTaskClick(Task task);
//        void onTaskView(Task task);
//        void onTaskCheckChanged(Task task, boolean isChecked);
//        void onTaskDelete(Task task);
//        default void onTaskDuplicate(Task task) {}
//    }
//}






package com.example.effortlessflow.ui.tasklist;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.effortlessflow.R;
import com.example.effortlessflow.model.Task;
import com.example.effortlessflow.utils.DateUtils;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private OnTaskClickListener listener;

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox taskCheckBox;
        private TextView taskTitle;
        private TextView taskDescription;
        private TextView taskSubtaskSummary;
        private TextView taskDueDate;
        private View taskEffortDot;
        private TextView taskEffortText;
        private ImageView taskOptionsIcon;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckBox = itemView.findViewById(R.id.task_checkBox);
            taskTitle = itemView.findViewById(R.id.task_title_tv);
            taskDescription = itemView.findViewById(R.id.task_description_tv);
            taskSubtaskSummary = itemView.findViewById(R.id.task_subtask_summary_tv);
            taskDueDate = itemView.findViewById(R.id.task_due_date_tv);
            taskEffortDot = itemView.findViewById(R.id.task_effort_dot);
            taskEffortText = itemView.findViewById(R.id.task_effort_text);
            taskOptionsIcon = itemView.findViewById(R.id.task_options_icon);
        }

        public void bind(Task task) {
            taskCheckBox.setOnCheckedChangeListener(null);
            // Set basic info
            taskTitle.setText(task.getTitle());
            taskCheckBox.setChecked(task.isCompleted());

            // Handle description
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                taskDescription.setVisibility(View.VISIBLE);
                taskDescription.setText(task.getDescription());
            } else {
                taskDescription.setVisibility(View.GONE);
            }

            // Handle subtask summary
            if (task.getTotalSubTaskCount() > 0) {
                taskSubtaskSummary.setVisibility(View.VISIBLE);
                taskSubtaskSummary.setText(String.format("Subtasks: %d/%d completed",
                        task.getCompletedSubTaskCount(), task.getTotalSubTaskCount()));
            } else {
                taskSubtaskSummary.setVisibility(View.GONE);
            }

            // Handle due date
            if (task.getDueDate() != null) {
                taskDueDate.setVisibility(View.VISIBLE);
                String dueDateText = DateUtils.getRelativeDateString(task.getDueDate());
                taskDueDate.setText(dueDateText);

                // Color coding for overdue tasks
                if (task.isOverdue()) {
                    taskDueDate.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red_alert));
                } else {
                    taskDueDate.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.aqua_accent));
                }
            } else {
                taskDueDate.setVisibility(View.GONE);
            }

            // Handle effort indicator
            setEffortIndicator(task.getEffort());

            // Handle completed state styling
            updateCompletedState(task.isCompleted());

            // Set click listeners
            taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskCheckChanged(task, isChecked);
                }
                updateCompletedState(isChecked);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskView(task); // Changed to onTaskView instead of onTaskClick
                }
            });

            taskOptionsIcon.setOnClickListener(v -> showTaskOptions(v, task));
        }

        private void setEffortIndicator(String effort) {
            taskEffortText.setText(effort);
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
            taskEffortDot.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), color));
        }

        private void updateCompletedState(boolean completed) {
            if (completed) {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                taskTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.medium_gray_text));
                taskDescription.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.light_gray_hint));
            } else {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                taskTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.dark_charcoal_text));
                taskDescription.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.medium_gray_text));
            }
        }

        private void showTaskOptions(View view, Task task) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.inflate(R.menu.task_item_options);

            // Remove "View Details" from menu since tapping on the task already shows details
            // popup.getMenu().removeItem(R.id.view_task);

            // Remove "Duplicate Task" option as requested
            // popup.getMenu().removeItem(R.id.duplicate_task);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.edit_task) {
                    listener.onTaskClick(task);
                    return true;
                } else if (id == R.id.delete_task) {
                    listener.onTaskDelete(task);
                    return true;
                }
                return false;
            });

            popup.show();
        }
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskView(Task task);
        void onTaskCheckChanged(Task task, boolean isChecked);
        void onTaskDelete(Task task);
    }
}