package com.example.effortlessflow.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.effortlessflow.MainActivity;
import com.example.effortlessflow.R;
import com.example.effortlessflow.model.Task;
import com.example.effortlessflow.repository.TaskRepository;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TaskNotificationManager {
    private static final String CHANNEL_ID = "task_notifications";
    private static final String CHANNEL_NAME = "Task Reminders";
    private static final int NOTIFICATION_ID = 1001;

    private Context context;
    private NotificationManager notificationManager;

    public TaskNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for overdue and upcoming tasks");
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void scheduleOverdueCheck() {
        // Schedule daily check at 9 AM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();
        if (delay < 0) {
            delay += 24 * 60 * 60 * 1000; // Add 24 hours if time has passed
        }

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(OverdueCheckWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }

    public void showOverdueNotification(int overdueCount) {
        PreferencesManager prefsManager = PreferencesManager.getInstance(context);
        if (!prefsManager.areNotificationsEnabled() || overdueCount == 0) {
            return;
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = overdueCount == 1 ? "1 task is overdue" : overdueCount + " tasks are overdue";
        String content = "Tap to view your overdue tasks";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static class OverdueCheckWorker extends Worker {
        public OverdueCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            try {
                TaskRepository repository = TaskRepository.getInstance();

                // 🔹 Fetch tasks synchronously instead of LiveData.observeForever
                List<Task> tasks = repository.getAllTasksSync();

                int overdueCount = 0;
                for (Task task : tasks) {
                    if (task.isOverdue()) {
                        overdueCount++;
                    }
                }

                if (overdueCount > 0) {
                    TaskNotificationManager notificationManager =
                            new TaskNotificationManager(getApplicationContext());
                    notificationManager.showOverdueNotification(overdueCount);
                }

                // Schedule next check
                new TaskNotificationManager(getApplicationContext()).scheduleOverdueCheck();

                return Result.success();
            } catch (Exception e) {
                e.printStackTrace();
                return Result.failure();
            }
        }
    }
}
