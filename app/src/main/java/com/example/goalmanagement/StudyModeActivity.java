package com.example.goalmanagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent; // Thêm import này
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class StudyModeActivity extends AppCompatActivity {

    private TextView tvTimerCountdown, tvTimerLabel, tvTaskNameStudy;
    private ProgressBar progressBarTimer;
    private ImageButton btnPausePlay, btnPostpone, btnComplete;
    private CountDownTimer countDownTimer;

    private long totalTimeInMillis; // Sẽ được đặt khi onCreate
    private long timeLeftInMillis;
    private boolean isTimerRunning = false;
    private String taskName; // Biến lưu tên task
    private long taskId = -1; // id task trong DB (nếu có)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_mode);

        // Ánh xạ View
        tvTimerCountdown = findViewById(R.id.tv_timer_countdown);
        tvTimerLabel = findViewById(R.id.tv_timer_label);
        tvTaskNameStudy = findViewById(R.id.tv_task_name_study);
        progressBarTimer = findViewById(R.id.progress_bar_timer);
        btnPausePlay = findViewById(R.id.btn_pause_play);
        btnPostpone = findViewById(R.id.btn_postpone);
        btnComplete = findViewById(R.id.btn_complete);

        // --- NHẬN DỮ LIỆU TỪ INTENT (QUAN TRỌNG) ---
        taskName = getIntent().getStringExtra("TASK_NAME");
        long durationMinutes = getIntent().getLongExtra("TASK_DURATION_MINUTES", 0);
        taskId = getIntent().getLongExtra("TASK_ID", -1);

        if (taskName != null) {
            tvTaskNameStudy.setText(taskName);
        } else {
            taskName = "Học tập"; // Đặt tên mặc định nếu không có
        }

        if (durationMinutes > 0) {
            totalTimeInMillis = durationMinutes * 60 * 1000;
        } else {
            // Nếu không có thời gian từ Intent, dùng 30 giây mặc định để test
            totalTimeInMillis = 30 * 1000;
        }
        // --- KẾT THÚC NHẬN DỮ LIỆU ---

        timeLeftInMillis = totalTimeInMillis;
        progressBarTimer.setMax((int) (totalTimeInMillis / 1000)); // Max tính bằng giây

        startTimer(); // Bắt đầu đếm giờ

        // Xử lý nút Tạm dừng/Tiếp tục
        btnPausePlay.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        // Xử lý nút Hoàn thành
        btnComplete.setOnClickListener(v -> completeTask());

        // Xử lý nút Dời lịch
        btnPostpone.setOnClickListener(v -> {
            pauseTimer(); // Dừng timer
            Toast.makeText(this, "Đã dời lịch", Toast.LENGTH_SHORT).show();

            // Cập nhật trạng thái cho MainActivity
            MainActivity.isTimerCurrentlyRunning = false;

            // Cập nhật DB nếu có TASK_ID
            if (taskId > 0) {
                new Thread(() -> {
                    com.example.goalmanagement.data.AppDatabase db = com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext());
                    db.taskDao().updateStatus(taskId, "postponed");
                    com.example.goalmanagement.data.Task t = db.taskDao().getById(taskId);
                    if (t != null) {
                        long oneDay = 24L * 60L * 60L * 1000L;
                        com.example.goalmanagement.data.Task newTask = new com.example.goalmanagement.data.Task(
                                t.goalId,
                                t.title,
                                t.subject,
                                t.startAtMillis + oneDay,
                                t.endAtMillis + oneDay,
                                t.durationMinutes,
                                getNextDayKey(t.dayKey),
                                "pending"
                        );
                        db.taskDao().insert(newTask);
                    }
                }).start();
            }
            finish(); // Đóng màn hình
        });
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownUI();

                // Cập nhật biến static để MainActivity đọc
                MainActivity.currentTimerTimeLeft = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                timeLeftInMillis = 0;
                updateCountdownUI();

                // Cập nhật biến static
                MainActivity.isTimerCurrentlyRunning = false;

                showTimerFinishedDialog();
            }
        }.start();

        isTimerRunning = true;
        btnPausePlay.setImageResource(R.drawable.ic_pause);
        TextView pauseLabel = findViewById(R.id.tv_pause_label);
        if (pauseLabel != null) pauseLabel.setText("Tạm dừng");

        // --- CẬP NHẬT TRẠNG THÁI CHO MAINACTIVITY ---
        MainActivity.isTimerCurrentlyRunning = true;
        MainActivity.currentTimerTaskName = this.taskName;
        MainActivity.currentTimerTimeLeft = this.timeLeftInMillis;
        // --- KẾT THÚC CẬP NHẬT ---
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        btnPausePlay.setImageResource(R.drawable.ic_play);
        TextView pauseLabel = findViewById(R.id.tv_pause_label);
        if (pauseLabel != null) pauseLabel.setText("Tiếp tục");

        // --- CẬP NHẬT TRẠNG THÁI CHO MAINACTIVITY ---
        // Khi Tạm dừng, ta coi như timer không "chạy"
        // để MainActivity hiển thị lại card "Tạo mục tiêu"
        MainActivity.isTimerCurrentlyRunning = false;
    }

    private void updateCountdownUI() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimerCountdown.setText(timeLeftFormatted);

        int progress = (int) (timeLeftInMillis / 1000);
        progressBarTimer.setProgress(progress);
    }

    private void completeTask() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;

        // --- CẬP NHẬT TRẠNG THÁI CHO MAINACTIVITY ---
        MainActivity.isTimerCurrentlyRunning = false;

        // Đánh dấu task là hoàn thành trong database
        if (taskId > 0) {
            new Thread(() -> {
                com.example.goalmanagement.data.AppDatabase db = com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext());
                db.taskDao().updateStatus(taskId, "completed");
            }).start();
        }
        Toast.makeText(this, "Đã hoàn thành!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getNextDayKey(String dayKey) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date d = sdf.parse(dayKey);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(d);
            c.add(java.util.Calendar.DATE, 1);
            return sdf.format(c.getTime());
        } catch (Exception e) {
            return dayKey;
        }
    }

    // Hiển thị Dialog Hết giờ (Giữ nguyên)
    private void showTimerFinishedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn cần thêm thời gian để hoàn thành bài này không?");

        builder.setPositiveButton("Có", (dialog, which) -> {
            showAddTimeDialog();
        });
        builder.setNegativeButton("Đã hoàn thành", (dialog, which) -> {
            completeTask();
        });
        builder.setCancelable(false);
        builder.show();
    }

    // Hiển thị Dialog Thêm thời gian (Giữ nguyên)
    private void showAddTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_time, null);
        final EditText etAddTime = dialogView.findViewById(R.id.et_add_time_input);

        builder.setView(dialogView);
        builder.setPositiveButton("Tiếp tục", null);
        builder.setNegativeButton("Quay lại", (dialog, which) -> {
            showTimerFinishedDialog();
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String timeStr = etAddTime.getText().toString().trim();
                if (timeStr.isEmpty()) {
                    Toast.makeText(StudyModeActivity.this, "Vui lòng nhập số phút", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        int minutesToAdd = Integer.parseInt(timeStr);
                        if (minutesToAdd <= 0) {
                            Toast.makeText(StudyModeActivity.this, "Số phút phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        timeLeftInMillis = minutesToAdd * 60 * 1000;
                        totalTimeInMillis = timeLeftInMillis;
                        progressBarTimer.setMax((int) (totalTimeInMillis / 1000));
                        startTimer(); // Bắt đầu đếm giờ lại
                        dialog.dismiss();

                    } catch (NumberFormatException e) {
                        Toast.makeText(StudyModeActivity.this, "Số phút không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}