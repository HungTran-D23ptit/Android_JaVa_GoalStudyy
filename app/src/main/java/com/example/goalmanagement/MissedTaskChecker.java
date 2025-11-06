package com.example.goalmanagement;

import android.content.Context;
import android.util.Log;

import com.example.goalmanagement.data.AppDatabase;
import com.example.goalmanagement.data.NotificationEntity;
import com.example.goalmanagement.data.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MissedTaskChecker {
    private static final String TAG = "MissedTaskChecker";
    private final Context context;
    private final AppDatabase db;

    public MissedTaskChecker(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
    }

    /**
     * Kiểm tra các task đã quá hạn và tạo notification cảnh báo
     */
    public void checkAndNotifyMissedTasks() {
        new Thread(() -> {
            try {
                long now = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String todayKey = sdf.format(new Date(now));

                // Lấy tất cả task pending hoặc inprogress
                List<Task> pendingTasks = db.taskDao().getByDay(todayKey);

                for (Task task : pendingTasks) {
                    if (task.status.equals("pending") || task.status.equals("inprogress")) {
                        // Nếu task đã bắt đầu mà chưa hoàn thành và đã quá giờ kết thúc
                        if (task.endAtMillis < now) {
                            // Đánh dấu task là missed
                            db.taskDao().updateStatus(task.id, "missed");

                            // Tạo notification cảnh báo
                            String title = "Task bị bỏ lỡ";
                            String content = "Bạn đã bỏ lỡ task: " + task.title +
                                           ". Điều này có thể ảnh hưởng đến mục tiêu của bạn.";

                            NotificationEntity notification = new NotificationEntity(
                                title,
                                content,
                                now,
                                "warning",
                                task.id
                            );

                            db.notificationDao().insert(notification);

                            Log.d(TAG, "Marked task as missed: " + task.title);
                        }
                    }
                }

                // Tạo báo cáo tiến độ hàng tuần
                generateWeeklyProgressReport();

            } catch (Exception e) {
                Log.e(TAG, "Error checking missed tasks", e);
            }
        }).start();
    }

    /**
     * Tạo báo cáo tiến độ hàng tuần
     */
    public void generateWeeklyProgressReport() {
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.add(Calendar.DAY_OF_MONTH, -7); // Tuần trước
            String weekStart = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

            cal.add(Calendar.DAY_OF_MONTH, 6); // Chủ nhật
            String weekEnd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

            // Đếm số task hoàn thành trong tuần
            int completedTasks = db.taskDao().countByDayRangeAndStatus(weekStart, weekEnd, "completed");
            int totalTasks = db.taskDao().countByDayRangeAndStatus(weekStart, weekEnd, "completed") +
                           db.taskDao().countByDayRangeAndStatus(weekStart, weekEnd, "missed");

            if (totalTasks > 0) {
                int completionRate = (completedTasks * 100) / totalTasks;
                String title = "Báo cáo tuần";
                String content = String.format("Tuần này bạn đã hoàn thành %d/%d task (%d%%). %s",
                    completedTasks, totalTasks, completionRate,
                    completionRate >= 80 ? "Tuyệt vời!" : "Cố gắng hơn nhé!");

                NotificationEntity report = new NotificationEntity(
                    title,
                    content,
                    System.currentTimeMillis(),
                    "report"
                );

                db.notificationDao().insert(report);
                Log.d(TAG, "Generated weekly progress report");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error generating weekly report", e);
        }
    }

    /**
     * Tạo notification nhắc nhở cho task sắp đến giờ
     */
    public void createReminderNotification(Task task) {
        new Thread(() -> {
            try {
                String title = "Nhắc nhở học tập";
                String content = "Đến giờ học: " + task.title + " (" +
                               new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(task.startAtMillis)) + ")";

                NotificationEntity reminder = new NotificationEntity(
                    title,
                    content,
                    System.currentTimeMillis(),
                    "reminder",
                    task.id
                );

                db.notificationDao().insert(reminder);
                Log.d(TAG, "Created reminder notification for: " + task.title);

            } catch (Exception e) {
                Log.e(TAG, "Error creating reminder", e);
            }
        }).start();
    }
}
