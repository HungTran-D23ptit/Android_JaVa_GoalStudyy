package com.example.goalmanagement;

import androidx.annotation.NonNull; // Thêm import
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem; // Thêm import
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView; // Thêm import
import com.google.android.material.navigation.NavigationBarView; // Thêm import
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;

public class ScheduleActivity extends AppCompatActivity implements
        DateSelectorAdapter.OnDateSelectedListener,
        ScheduleAdapter.OnScheduleInteractionListener {

    RecyclerView rvScheduleItems, rvDateSelector;
    ScheduleAdapter scheduleAdapter;
    DateSelectorAdapter dateAdapter;
    List<ScheduleItem> scheduleDataList;
    List<DateItem> dateDataList;
    Button btnAddSchedule, btnStartLearning;
    ImageView btnPrevDay, btnNextDay, btnNotification;
    Calendar selectedDate = Calendar.getInstance();

    BottomNavigationView bottomNav; // THÊM BottomNav

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Ánh xạ View
        rvScheduleItems = findViewById(R.id.rv_schedule_items);
        btnAddSchedule = findViewById(R.id.btn_add_schedule);
        btnStartLearning = findViewById(R.id.btn_start_learning);
        rvDateSelector = findViewById(R.id.rv_date_selector);
        btnPrevDay = findViewById(R.id.btn_prev_day);
        btnNextDay = findViewById(R.id.btn_next_day);
        btnNotification = findViewById(R.id.img_notification_schedule);
        bottomNav = findViewById(R.id.bottom_navigation_schedule); // THÊM Ánh xạ

        setupDateSelector();
        setupScheduleList(selectedDate);
        setupNotificationButton();
        setupBottomNavigation(); // THÊM HÀM NÀY

        // Xử lý nút bấm
        btnAddSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, CreateTaskActivity.class);
            startActivity(intent);
        });
        btnStartLearning.setOnClickListener(v -> {
            if (scheduleDataList != null && !scheduleDataList.isEmpty()) {
                ScheduleItem currentTask = scheduleDataList.get(0);
                long durationMinutes = 45;
                try {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date startTime = timeFormat.parse(currentTask.startTime);
                    Date endTime = timeFormat.parse(currentTask.endTime);
                    long diffInMillis = endTime.getTime() - startTime.getTime();
                    if (diffInMillis < 0) diffInMillis += 24 * 60 * 60 * 1000;
                    durationMinutes = diffInMillis / (60 * 1000);
                } catch (Exception e) { e.printStackTrace(); }

                Intent intent = new Intent(ScheduleActivity.this, StudyModeActivity.class);
                intent.putExtra("TASK_NAME", currentTask.title);
                intent.putExtra("TASK_DURATION_MINUTES", durationMinutes);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không có lịch trình để bắt đầu", Toast.LENGTH_SHORT).show();
            }
        });
        btnPrevDay.setOnClickListener(v -> scrollDays(-1));
        btnNextDay.setOnClickListener(v -> scrollDays(1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_schedule); // Đặt lại tab khi quay về
        }
        // Refresh schedule data when returning to this activity
        setupScheduleList(selectedDate);
    }

    private void setupNotificationButton() {
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(ScheduleActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * HÀM MỚI: Thiết lập xử lý sự kiện cho BottomNavigationView
     */
    private void setupBottomNavigation() {
        // Xóa dòng setSelectedItemId(R.id.nav_schedule) khỏi đây
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(ScheduleActivity.this, MainActivity.class);
            } else if (itemId == R.id.nav_schedule) {
                return true; // Đã ở đây
            } else if (itemId == R.id.nav_progress) {
                intent = new Intent(ScheduleActivity.this, ProgressActivity.class);
            } else if (itemId == R.id.nav_community) {
                Toast.makeText(this, "Mở Cộng đồng", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(ScheduleActivity.this, ProfileActivity.class);
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    // --- (Các hàm setupDateSelector, setupScheduleList, calculateEndTime,
    //      onDateSelected, scrollDays, isSameDay, onEditClick, onDeleteClick
    //      GIỮ NGUYÊN NHƯ CODE CŨ CỦA BẠN) ---

    private void setupDateSelector() {
        dateDataList = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        selectedDate.setTime(today.getTime());
        int daysBefore = 10;
        int daysAfter = 10;
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTime(today.getTime());
        tempCal.add(Calendar.DAY_OF_YEAR, -daysBefore);
        for (int i = 0; i < daysBefore + 1 + daysAfter; i++) {
            boolean isSelected = isSameDay(tempCal, today);
            dateDataList.add(new DateItem((Calendar) tempCal.clone(), isSelected));
            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }
        dateAdapter = new DateSelectorAdapter(this, dateDataList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvDateSelector.setLayoutManager(layoutManager);
        rvDateSelector.setAdapter(dateAdapter);
        rvDateSelector.scrollToPosition(daysBefore);
    }
    private void setupScheduleList(Calendar date) {
        scheduleDataList = new ArrayList<>();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("NEW_TASK_NAME") && isSameDay(date, Calendar.getInstance())) {
            String newName = intent.getStringExtra("NEW_TASK_NAME");
            String newContent = intent.getStringExtra("NEW_TASK_CONTENT");
            String newTime = intent.getStringExtra("NEW_TASK_TIME");
            String startTime = newTime != null ? newTime : "00:00";
            String endTime = calculateEndTime(startTime, 1);
            scheduleDataList.add(new ScheduleItem(startTime, endTime, newName, newContent, "study"));
            intent.removeExtra("NEW_TASK_NAME");
        }

        // Load real tasks from database for the selected date
        new Thread(() -> {
            try {
                com.example.goalmanagement.data.AppDatabase db = com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext());
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                String dayKey = sdf.format(date.getTime());
                java.util.List<com.example.goalmanagement.data.Task> tasks = db.taskDao().getByDay(dayKey);
                java.text.SimpleDateFormat hm = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                for (com.example.goalmanagement.data.Task t : tasks) {
                    String type = "Break".equalsIgnoreCase(t.subject) ? "rest" : "study";
                    String start = hm.format(new java.util.Date(t.startAtMillis));
                    String end = hm.format(new java.util.Date(t.endAtMillis));
                    String title = type.equals("rest") ? "Nghỉ ngơi" : t.title;
                    String desc = type.equals("rest") ? "" : t.subject;
                    scheduleDataList.add(new ScheduleItem(start, end, title, desc, type, t.id));
                }
            } catch (Exception ignored) { }
            runOnUiThread(() -> {
                if (scheduleAdapter == null) {
                    scheduleAdapter = new ScheduleAdapter(this, scheduleDataList, this);
                    rvScheduleItems.setLayoutManager(new LinearLayoutManager(this));
                    rvScheduleItems.setAdapter(scheduleAdapter);
                } else {
                    scheduleAdapter.updateData(scheduleDataList);
                }
            });
        }).start();
    }
    private String calculateEndTime(String startTime, int durationHours) {
        try {
            String[] parts = startTime.split(":");
            int startHour = Integer.parseInt(parts[0]);
            int startMinute = Integer.parseInt(parts[1]);
            int endHour = (startHour + durationHours) % 24;
            return String.format(Locale.getDefault(), "%02d:%02d", endHour, startMinute);
        } catch (Exception e) { return startTime; }
    }
    @Override
    public void onDateSelected(Calendar selectedDate) {
        this.selectedDate = selectedDate;
        setupScheduleList(selectedDate);
    }
    private void scrollDays(int amount) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) rvDateSelector.getLayoutManager();
        if (layoutManager != null && dateAdapter != null) {
            int currentSelectedPos = -1;
            for(int i=0; i<dateDataList.size(); i++){
                if(dateDataList.get(i).isSelected){
                    currentSelectedPos = i;
                    break;
                }
            }
            int targetPosition = currentSelectedPos + amount;
            if (targetPosition >= 0 && targetPosition < dateAdapter.getItemCount()) {
                Calendar targetDate = dateDataList.get(targetPosition).calendar;
                dateAdapter.setSelectedPosition(targetPosition);
                onDateSelected(targetDate);
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (targetPosition < firstVisible || targetPosition > lastVisible) {
                    rvScheduleItems.smoothScrollToPosition(targetPosition);
                }
            }
        }
    }
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) return false;
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    @Override
    public void onEditClick(int position) {
        if (position >= 0 && position < scheduleDataList.size()) {
            ScheduleItem itemToEdit = scheduleDataList.get(position);
            Toast.makeText(this, "Sửa: " + itemToEdit.title + " (Chưa implement)", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < scheduleDataList.size()) {
            ScheduleItem itemToDelete = scheduleDataList.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa lịch trình '" + itemToDelete.title + "'?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        scheduleDataList.remove(position);
                        scheduleAdapter.notifyItemRemoved(position);
                        scheduleAdapter.notifyItemRangeChanged(position, scheduleDataList.size());
                        Toast.makeText(this, "Đã xóa: " + itemToDelete.title, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }
}