package com.example.goalmanagement;

// --- CÁC IMPORT CẦN THIẾT (ĐÃ THÊM ĐẦY ĐỦ) ---
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper; // Thêm import
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Thêm import
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager; // Thêm import
import androidx.recyclerview.widget.RecyclerView; // Thêm import
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
// --- KẾT THÚC IMPORT ---


// Implement thêm OnScheduleInteractionListener để xử lý click Sửa/Xóa
public class MainActivity extends AppCompatActivity implements ScheduleAdapter.OnScheduleInteractionListener {

    BottomNavigationView bottomNav;
    TextView tvWelcomeMessage, tvUserName;
    ImageView imgAvatar, imgNotification;
    SharedPreferences sharedPrefs;

    CardView findFriendsCard, createTaskCard;

    // Các biến cho logic mới
    ViewFlipper cardFlipper;
    View cardCreateGoalButton, miniTimerCardView;
    RecyclerView rvHomeSchedule;
    CardView cardNoData;
    ScheduleAdapter homeScheduleAdapter;
    List<ScheduleItem> todayScheduleList = new ArrayList<>();

    // Biến static để GIẢ LẬP trạng thái timer
    public static boolean isTimerCurrentlyRunning = false;
    public static String currentTimerTaskName = "Task đang chạy";
    public static long currentTimerTimeLeft = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = getSharedPreferences(ProfileActivity.PREFS_NAME, MODE_PRIVATE);

        // Ánh xạ View
        bottomNav = findViewById(R.id.bottom_navigation);
        tvWelcomeMessage = findViewById(R.id.tv_welcome_message);
        tvUserName = findViewById(R.id.tv_user_name);
        imgAvatar = findViewById(R.id.img_avatar);
        imgNotification = findViewById(R.id.img_notification);

        cardFlipper = findViewById(R.id.card_flipper);
        rvHomeSchedule = findViewById(R.id.rv_home_schedule);
        cardNoData = findViewById(R.id.card_no_data);

        View cardCreateGoalView = cardFlipper.findViewById(R.id.card_create_goal);
        miniTimerCardView = cardFlipper.findViewById(R.id.mini_timer_card);

        if (cardCreateGoalView != null) {
            cardCreateGoalButton = cardCreateGoalView.findViewById(R.id.btn_create_goal);
        }

        findFriendsCard = findViewById(R.id.card_find_friends);
        createTaskCard = findViewById(R.id.card_create_task);

        setupPopularGoals();
        setupCardButtons();
        setupBottomNavigation();
        setupNotificationButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataAndUpdateUI(); // Hàm tổng hợp
    }

    /**
     * HÀM QUAN TRỌNG NHẤT: Kiểm tra dữ liệu và cập nhật giao diện Trang chủ
     */
    private void loadDataAndUpdateUI() {
        // 1. Cập nhật Header (Đăng nhập / Tên)
        updateHeaderUI();
        // 2. Cập nhật Màu nền
        loadThemeColor();
        // 3. Đặt lại tab BottomNav
        if(bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // 4. Lấy dữ liệu lịch học (Giả lập)
        todayScheduleList = getTodaySchedule();

        // 5. Kiểm tra timer đang chạy
        boolean isTimerRunning = isTimerCurrentlyRunning;

        // 6. Cập nhật Card Flipper (Tạo mục tiêu / Mini Timer)
        if (isTimerRunning && miniTimerCardView != null) {
            if (cardFlipper.getDisplayedChild() != 1) {
                cardFlipper.setDisplayedChild(1); // Lật sang Card 1 (Mini Timer)
            }
            TextView tvMiniTaskName = miniTimerCardView.findViewById(R.id.tv_mini_timer_task_name);
            TextView tvMiniTime = miniTimerCardView.findViewById(R.id.tv_mini_timer_time);
            if (tvMiniTaskName != null) tvMiniTaskName.setText(currentTimerTaskName);
            if (tvMiniTime != null) {
                int minutes = (int) (currentTimerTimeLeft / 1000) / 60;
                int seconds = (int) (currentTimerTimeLeft / 1000) % 60;
                tvMiniTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }
        } else {
            if (cardFlipper.getDisplayedChild() != 0) {
                cardFlipper.setDisplayedChild(0); // Lật sang Card 0 (Tạo mục tiêu)
            }
        }

        // 7. Cập nhật Lịch học (RecyclerView / "Chưa có dữ liệu")
        if (todayScheduleList.isEmpty()) {
            rvHomeSchedule.setVisibility(View.GONE);
            cardNoData.setVisibility(View.VISIBLE);
        } else {
            rvHomeSchedule.setVisibility(View.VISIBLE);
            cardNoData.setVisibility(View.GONE);
            if (homeScheduleAdapter == null) {
                homeScheduleAdapter = new ScheduleAdapter(this, todayScheduleList, this);
                rvHomeSchedule.setLayoutManager(new LinearLayoutManager(this));
                rvHomeSchedule.setAdapter(homeScheduleAdapter);
            } else {
                homeScheduleAdapter.updateData(todayScheduleList);
            }
        }
    }

    /**
     * Hàm giả lập lấy lịch học.
     */
    private List<ScheduleItem> getTodaySchedule() {
        List<ScheduleItem> items = new ArrayList<>();
        try {
            com.example.goalmanagement.data.AppDatabase db = com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext());
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            String dayKey = sdf.format(new java.util.Date());
            java.util.List<com.example.goalmanagement.data.Task> tasks = db.taskDao().getByDay(dayKey);
            java.text.SimpleDateFormat hm = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            long now = System.currentTimeMillis();
            for (com.example.goalmanagement.data.Task t : tasks) {
                String type = "Break".equalsIgnoreCase(t.subject) ? "rest" : "study";
                String start = hm.format(new java.util.Date(t.startAtMillis));
                String end = hm.format(new java.util.Date(t.endAtMillis));
                String title = type.equals("rest") ? "Nghỉ ngơi" : t.title;
                String desc = type.equals("rest") ? "" : t.subject;
                items.add(new ScheduleItem(start, end, title, desc, type, t.id));

                // Lên lịch nhắc nhở 10 phút trước khi bắt đầu cho task học sắp tới
                if (type.equals("study") && t.startAtMillis > now && "pending".equalsIgnoreCase(t.status)) {
                    com.example.goalmanagement.notifications.ReminderScheduler.scheduleReminder(
                            getApplicationContext(),
                            t.startAtMillis,
                            "Sắp đến giờ học: " + t.title,
                            "Bắt đầu lúc " + start
                    );
                }
            }
        } catch (Exception ignored) { }
        return items;
    }


    /**
     * HÀM CẬP NHẬT HEADER (ĐÃ SỬA LỖI ICON)
     */
    private void updateHeaderUI() {
        boolean isLoggedIn = sharedPrefs.getBoolean(ProfileActivity.IS_LOGGED_IN_KEY, false);
        if (isLoggedIn) {
            String userName = sharedPrefs.getString(ProfileActivity.USER_NAME_KEY, "Bạn");
            tvWelcomeMessage.setText("Chúc bạn ngày mới vui vẻ");
            tvUserName.setText(userName);
            tvUserName.setVisibility(View.VISIBLE);
            imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        } else {
            tvWelcomeMessage.setText("Hãy đăng nhập tài khoản");
            tvUserName.setVisibility(View.GONE);
            imgAvatar.setImageResource(R.drawable.ic_profile_avatar); // Dùng icon login
        }
    }

    /**
     * HÀM LOAD MÀU NỀN (HÀM BỊ THIẾU)
     */
    private void loadThemeColor() {
        SharedPreferences prefs = getSharedPreferences(ProfileActivity.PREFS_NAME, MODE_PRIVATE);
        String defaultColor = "#E0F7FA";
        String savedColorHex = prefs.getString(ProfileActivity.THEME_COLOR_KEY, defaultColor);

        int colorInt = Color.parseColor(savedColorHex);

        String colorHexPink = "#FCE4EC";
        String colorHexYellow = "#FFF8E1";

        View cardCreateGoalView = cardFlipper.findViewById(R.id.card_create_goal);
        if (cardCreateGoalView instanceof CardView) {
            ((CardView) cardCreateGoalView).setCardBackgroundColor(colorInt);
        }
        if (miniTimerCardView instanceof CardView) {
            ((CardView) miniTimerCardView).setCardBackgroundColor(colorInt);
        }

        if (savedColorHex.equals(defaultColor)) {
            if (findFriendsCard != null) findFriendsCard.setCardBackgroundColor(Color.parseColor(colorHexPink));
            if (createTaskCard != null) createTaskCard.setCardBackgroundColor(Color.parseColor(colorHexYellow));
        } else if (savedColorHex.equals(colorHexPink)) {
            if (findFriendsCard != null) findFriendsCard.setCardBackgroundColor(Color.parseColor(defaultColor));
            if (createTaskCard != null) createTaskCard.setCardBackgroundColor(Color.parseColor(colorHexYellow));
        } else if (savedColorHex.equals(colorHexYellow)) {
            if (findFriendsCard != null) findFriendsCard.setCardBackgroundColor(Color.parseColor(colorHexPink));
            if (createTaskCard != null) createTaskCard.setCardBackgroundColor(Color.parseColor(defaultColor));
        }
    }


    /**
     * setupPopularGoals (Giữ nguyên)
     */
    private void setupPopularGoals() {
        View goalIelts = findViewById(R.id.goal_ielts);
        if (goalIelts != null) {
            TextView tvIelts = goalIelts.findViewById(R.id.tv_goal_name);
            ImageView ivIelts = goalIelts.findViewById(R.id.img_goal_icon);
            if (tvIelts != null) tvIelts.setText("Ielts");
            if (ivIelts != null) ivIelts.setImageResource(R.drawable.ic_ielts_icon);
        }
        View goalReading = findViewById(R.id.goal_reading);
        if (goalReading != null) {
            TextView tvReading = goalReading.findViewById(R.id.tv_goal_name);
            ImageView ivReading = goalReading.findViewById(R.id.img_goal_icon);
            if (tvReading != null) tvReading.setText("Đọc sách");
            if (ivReading != null) ivReading.setImageResource(R.drawable.ic_reading_icon);
        }
        View goalExercise = findViewById(R.id.goal_exercise);
        if (goalExercise != null) {
            TextView tvExercise = goalExercise.findViewById(R.id.tv_goal_name);
            ImageView ivExercise = goalExercise.findViewById(R.id.img_goal_icon);
            if (tvExercise != null) tvExercise.setText("Tập thể dục");
            if (ivExercise != null) ivExercise.setImageResource(R.drawable.ic_exercise_icon);
        }
    }

    /**
     * Thiết lập sự kiện click cho các card trong Flipper
     */
    private void setupCardButtons() {
        if (cardCreateGoalButton != null) {
            cardCreateGoalButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CreateGoalActivity.class);
                startActivity(intent);
            });
        }
        if (miniTimerCardView != null) {
            miniTimerCardView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, StudyModeActivity.class);
                intent.putExtra("TASK_NAME", currentTimerTaskName);
                intent.putExtra("TASK_DURATION_MINUTES", (currentTimerTimeLeft / 60000));
                startActivity(intent);
            });
        }

        if (createTaskCard != null) {
            createTaskCard.setOnClickListener(v -> showQuickAddTaskDialog());
        }
    }

    private void showQuickAddTaskDialog() {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View view = inflater.inflate(R.layout.dialog_quick_add_task, null);
        final android.widget.EditText etTitle = view.findViewById(R.id.et_quick_task_title);
        final android.widget.EditText etSubject = view.findViewById(R.id.et_quick_task_subject);
        final android.widget.EditText etStart = view.findViewById(R.id.et_quick_task_start);
        final android.widget.EditText etDuration = view.findViewById(R.id.et_quick_task_duration);

        new AlertDialog.Builder(this)
                .setTitle("Thêm task hôm nay")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String subject = etSubject.getText().toString().trim();
                    String startHm = etStart.getText().toString().trim();
                    String durStr = etDuration.getText().toString().trim();
                    if (title.isEmpty() || startHm.isEmpty() || durStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int duration = Integer.parseInt(durStr);
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                        String dayKey = sdf.format(new java.util.Date());
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        String[] hm = startHm.split(":");
                        cal.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(hm[0]));
                        cal.set(java.util.Calendar.MINUTE, Integer.parseInt(hm[1]));
                        cal.set(java.util.Calendar.SECOND, 0);
                        cal.set(java.util.Calendar.MILLISECOND, 0);
                        long start = cal.getTimeInMillis();
                        long end = start + duration * 60L * 1000L;
                        com.example.goalmanagement.data.Task t = new com.example.goalmanagement.data.Task(0, title, subject.isEmpty()?title:subject, start, end, duration, dayKey, "pending");
                        new Thread(() -> {
                            com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext()).taskDao().insert(t);
                            runOnUiThread(this::loadDataAndUpdateUI);
                        }).start();
                    } catch (Exception e) {
                        Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * setupNotificationButton (Giữ nguyên)
     */
    private void setupNotificationButton() {
        if (imgNotification != null) {
            imgNotification.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * setupBottomNavigation (ĐÃ SỬA LỖI LOGIC RETURN)
     */
    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Intent intent = null;
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_schedule) {
                    intent = new Intent(MainActivity.this, ScheduleActivity.class);
                } else if (itemId == R.id.nav_progress) {
                    intent = new Intent(MainActivity.this, ProgressActivity.class);
                } else if (itemId == R.id.nav_community) {
                    Toast.makeText(MainActivity.this, "Mở Cộng đồng", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_profile) {
                    intent = new Intent(MainActivity.this, ProfileActivity.class);
                }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true; // Trả về true VÌ ĐÃ XỬ LÝ
                }

                // Nếu là "Cộng đồng" (chỉ hiện Toast)
                if (itemId == R.id.nav_community) {
                    return true; // Vẫn trả về true vì đã xử lý
                }

                return false; // Trả về false nếu không item nào khớp
            }
        });
    }

    // --- CÁC HÀM XỬ LÝ SỬA/XÓA TỪ ADAPTER ---

    @Override
    public void onEditClick(int position) {
        if (position >= 0 && position < todayScheduleList.size()) {
            ScheduleItem item = todayScheduleList.get(position);
            String[] options = new String[]{"Đánh dấu hoàn thành", "Đánh dấu chưa hoàn thành", "Dời sang ngày mai", "Bắt đầu học"};
            new AlertDialog.Builder(this)
                    .setTitle(item.title)
                    .setItems(options, (d, which) -> {
                        long taskId = item.taskId;
                        if (taskId <= 0) {
                            Toast.makeText(this, "Task mẫu, không chỉnh sửa được", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (which == 0) { // complete
                            new Thread(() -> {
                                com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext()).taskDao().updateStatus(taskId, "completed");
                                runOnUiThread(this::loadDataAndUpdateUI);
                            }).start();
                        } else if (which == 1) { // pending
                            new Thread(() -> {
                                com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext()).taskDao().updateStatus(taskId, "pending");
                                runOnUiThread(this::loadDataAndUpdateUI);
                            }).start();
                        } else if (which == 2) { // postpone
                            new Thread(() -> {
                                com.example.goalmanagement.data.AppDatabase db = com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext());
                                com.example.goalmanagement.data.Task t = db.taskDao().getById(taskId);
                                if (t != null) {
                                    db.taskDao().updateStatus(taskId, "postponed");
                                    long oneDay = 24L*60L*60L*1000L;
                                    com.example.goalmanagement.data.Task nt = new com.example.goalmanagement.data.Task(t.goalId, t.title, t.subject, t.startAtMillis+oneDay, t.endAtMillis+oneDay, t.durationMinutes, getNextDayKey(t.dayKey), "pending");
                                    db.taskDao().insert(nt);
                                }
                                runOnUiThread(this::loadDataAndUpdateUI);
                            }).start();
                        } else if (which == 3) { // start study
                            Intent intent = new Intent(MainActivity.this, StudyModeActivity.class);
                            intent.putExtra("TASK_NAME", item.title);
                            intent.putExtra("TASK_ID", taskId);
                            // Estimate duration from DB
                            new Thread(() -> {
                                com.example.goalmanagement.data.Task t = com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext()).taskDao().getById(taskId);
                                long minutes = (t != null) ? t.durationMinutes : 0;
                                runOnUiThread(() -> {
                                    intent.putExtra("TASK_DURATION_MINUTES", minutes);
                                    startActivity(intent);
                                });
                            }).start();
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < todayScheduleList.size()) {
            ScheduleItem item = todayScheduleList.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa '" + item.title + "'?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        long taskId = item.taskId;
                        if (taskId > 0) {
                            new Thread(() -> {
                                com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext()).taskDao().deleteById(taskId);
                                runOnUiThread(this::loadDataAndUpdateUI);
                            }).start();
                        } else {
                            todayScheduleList.remove(position);
                            homeScheduleAdapter.notifyItemRemoved(position);
                            homeScheduleAdapter.notifyItemRangeChanged(position, todayScheduleList.size());
                            if (todayScheduleList.isEmpty()) loadDataAndUpdateUI();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }

    private String getNextDayKey(String dayKey) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date d = sdf.parse(dayKey);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(d);
            c.add(java.util.Calendar.DATE, 1);
            return sdf.format(c.getTime());
        } catch (Exception e) { return dayKey; }
    }
}