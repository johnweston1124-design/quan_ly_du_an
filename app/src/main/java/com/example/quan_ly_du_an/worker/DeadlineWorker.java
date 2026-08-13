package com.example.quan_ly_du_an.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.model.NotificationHistory;
import com.example.quan_ly_du_an.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeadlineWorker extends Worker {

    public DeadlineWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();

            SharedPreferences sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            int userId = sharedPref.getInt("USER_ID", -1);

            if (userId == -1) {
                return Result.success();
            }

            AppDatabase db = AppDatabase.getDatabase(context);
            List<Task> userTasks = db.taskDao().getAllTasksForUserSync(userId);

            for (Task task : userTasks) {
                if ("Hoàn thành".equalsIgnoreCase(task.getStatus()) || "Done".equalsIgnoreCase(task.getStatus())) {
                    continue;
                }

                String deadlineString = task.getDeadline();
                if (deadlineString != null && !deadlineString.trim().isEmpty()) {
                    if (isOneDayLeft(deadlineString)) {
                        sendNotification(task.getTitle(), "1 ngày");
                    }
                }
            }

            return Result.success();
        } catch (Exception e) {
            Log.e("DeadlineWorker", "Lỗi: ", e);
            return Result.failure();
        }
    }

    private boolean isOneDayLeft(String deadlineStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date deadlineDate = sdf.parse(deadlineStr);
            if (deadlineDate != null) {
                Date today = new Date();
                long diff = deadlineDate.getTime() - today.getTime();
                long daysLeft = diff / (24 * 60 * 60 * 1000);

                return daysLeft == 1 || daysLeft == 0;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private void sendNotification(String taskName, String timeLeft) {
        try {
            Context context = getApplicationContext();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        "DEADLINE_CHANNEL", "Nhắc nhở Deadline", NotificationManager.IMPORTANCE_HIGH);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            int notificationId = (int) System.currentTimeMillis() + taskName.hashCode();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "DEADLINE_CHANNEL")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("⏰ Hạn chót đang đến gần!")
                    .setContentText("Công việc '" + taskName + "' chỉ còn " + timeLeft + " nữa!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            if (notificationManager != null) {
                notificationManager.notify(notificationId, builder.build());
            }

            AppDatabase db = AppDatabase.getDatabase(context);
            NotificationHistory history = new NotificationHistory(
                    "⏰ Hạn chót: " + taskName,
                    "Còn " + timeLeft + " nữa!",
                    System.currentTimeMillis()
            );
            db.notificationDao().insert(history);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
