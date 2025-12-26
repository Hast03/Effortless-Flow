package com.example.effortlessflow.ui.insights;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.effortlessflow.R;
import com.example.effortlessflow.model.Task;
import com.example.effortlessflow.utils.DateUtils;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsightsFragment extends Fragment {

    private InsightsViewModel viewModel;
    private ProgressBar todayProgressBar;
    private TextView todayProgressText, completedCount, pendingCount;
    private TextView weekCompletionRate, dailyAverage, overdueCount;
    private TextView productiveTime, avgDuration, productivityTipText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_insights, container, false);

        initViews(root);
        setupViewModel();

        return root;
    }

    private void initViews(View root) {
        todayProgressBar = root.findViewById(R.id.today_progress_bar);
        todayProgressText = root.findViewById(R.id.today_progress_text);
        completedCount = root.findViewById(R.id.completed_count);
        pendingCount = root.findViewById(R.id.pending_count);
        weekCompletionRate = root.findViewById(R.id.week_completion_rate);
        dailyAverage = root.findViewById(R.id.daily_average);
        overdueCount = root.findViewById(R.id.overdue_count);
        productiveTime = root.findViewById(R.id.productive_time);
        avgDuration = root.findViewById(R.id.avg_duration);
        productivityTipText = root.findViewById(R.id.productivity_tip_text);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(InsightsViewModel.class);

        viewModel.getTasks().observe(getViewLifecycleOwner(), this::updateInsights);
        viewModel.loadTasks();
    }

    private void updateInsights(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            setDefaultValues();
            return;
        }

        calculateTodayProgress(tasks);
        calculateOverallStats(tasks);
        calculateWeeklyStats(tasks);
        setProductivityInsights(tasks);
    }

    private void calculateTodayProgress(List<Task> tasks) {
        int todayTotal = 0;
        int todayCompleted = 0;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        for (Task task : tasks) {
            if (task.getDueDate() != null &&
                    task.getDueDate().after(today.getTime()) &&
                    task.getDueDate().before(tomorrow.getTime())) {
                todayTotal++;
                if (task.isCompleted()) {
                    todayCompleted++;
                }
            }
        }
        if (todayTotal == 0) {
            todayProgressBar.setProgress(0);
            todayProgressText.setText("No tasks today");
        } else {
            int progress = (todayCompleted * 100) / todayTotal;
            todayProgressBar.setProgress(progress);
            todayProgressText.setText(todayCompleted + "/" + todayTotal + " tasks");
        }
    }

    private void calculateOverallStats(List<Task> tasks) {
        int completed = 0;
        int pending = 0;
        int overdue = 0;

        for (Task task : tasks) {
            if (task.isCompleted()) {
                completed++;
            } else {
                pending++;
                if (task.isOverdue()) {
                    overdue++;
                }
            }
        }

        completedCount.setText(String.valueOf(completed));
        pendingCount.setText(String.valueOf(pending));
        overdueCount.setText(String.valueOf(overdue));
    }

    private void calculateWeeklyStats(List<Task> tasks) {
        Calendar weekStart = Calendar.getInstance();
        weekStart.add(Calendar.DAY_OF_WEEK, -(weekStart.get(Calendar.DAY_OF_WEEK) - 1));

        int weekTotal = 0;
        int weekCompleted = 0;

        for (Task task : tasks) {
            if (task.getCreatedAt() != null && task.getCreatedAt().after(weekStart.getTime())) {
                weekTotal++;
                if (task.isCompleted()) {
                    weekCompleted++;
                }
            }
        }
        if (weekTotal == 0) {
            weekCompletionRate.setText("0%");
            dailyAverage.setText("0 tasks");
        } else {
            int completionRate = (weekCompleted * 100) / weekTotal;
            weekCompletionRate.setText(completionRate + "%");

            double avgPerDay = weekTotal / 7.0;
            dailyAverage.setText(String.format("%.1f tasks", avgPerDay));
        }
    }

    private void setProductivityInsights(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            productiveTime.setText("Not available");
            avgDuration.setText("Not available");
            productivityTipText.setText("Start adding tasks to see your productivity insights!");
            return;
        }
        // Calculate most productive time from actual task data
        Map<Integer, Integer> hourTaskCount = new HashMap<>();
        Map<Integer, Long> hourDurationSum = new HashMap<>();

        for (Task task : tasks) {
            if (task.getCreatedAt() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(task.getCreatedAt());
                int hour = calendar.get(Calendar.HOUR_OF_DAY);

                hourTaskCount.put(hour, hourTaskCount.getOrDefault(hour, 0) + 1);

                // Add estimated duration if available
                if (task.getEstimatedDuration() > 0) {
                    hourDurationSum.put(hour, hourDurationSum.getOrDefault(hour, 0L) + task.getEstimatedDuration());
                }
            }
        }
        // Find the hour with most tasks
        if (!hourTaskCount.isEmpty()) {
            int mostProductiveHour = Collections.max(hourTaskCount.entrySet(), Map.Entry.comparingByValue()).getKey();
            productiveTime.setText(formatHour(mostProductiveHour));
        } else {
            productiveTime.setText("Not available");
        }
        // Calculate average duration from actual task data
        if (!hourDurationSum.isEmpty()) {
            double totalDuration = 0;
            int taskCount = 0;

            for (Map.Entry<Integer, Long> entry : hourDurationSum.entrySet()) {
                totalDuration += entry.getValue();
                taskCount += hourTaskCount.getOrDefault(entry.getKey(), 0);
            }

            if (taskCount > 0) {
                double avgDurationMinutes = totalDuration / taskCount;
                avgDuration.setText(formatDuration(avgDurationMinutes));
            } else {
                avgDuration.setText("Not available");
            }
        }
        String[] tips = {
                "Break large tasks into smaller subtasks for better progress tracking!",
                "Set realistic due dates to avoid overdue stress.",
                "Complete high-priority tasks during your most productive hours.",
                "Review and clear completed tasks weekly to stay organized.",
                "Use the AI suggestions to plan your tasks more effectively."
        };
        int randomTip = (int) (Math.random() * tips.length);
        productivityTipText.setText(tips[randomTip]);
    }

    // Helper method to format hour to readable format
    private String formatHour(int hour) {
        if (hour == 0) return "12 AM";
        if (hour == 12) return "12 PM";
        if (hour < 12) return hour + " AM";
        else return (hour - 12) + " PM";
    }
    // Helper method to format duration to readable format
    private String formatDuration(double minutes) {
        if (minutes < 60) {
            return (int)minutes + " min";
        } else if (minutes < 1440) { // Less than a day
            int hours = (int)(minutes / 60);
            int mins = (int)(minutes % 60);
            return hours + "h " + mins + "min";
        } else { // More than a day
            int days = (int)(minutes / 1440);
            int hours = (int)((minutes % 1440) / 60);
            return days + "d " + hours + "h";
        }
    }

    private void setDefaultValues() {
        // Show welcoming message for new users
        todayProgressBar.setProgress(0);
        todayProgressText.setText("No tasks today");
        completedCount.setText("0");
        pendingCount.setText("0");
        overdueCount.setText("0");
        weekCompletionRate.setText("0%");
        dailyAverage.setText("0 tasks");

        // Encouraging message for new users
        productivityTipText.setText("Welcome to TaskManager! Start by adding your first task using the + button. Your productivity insights will appear here as you complete tasks.");
    }
}