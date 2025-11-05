package com.example.goalmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color; // Thêm import này
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
// import androidx.core.content.ContextCompat; // Không cần
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ProfileActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    TextView btnPersonalInfo, btnShareApp, btnThemeColor, btnActivitySchedule, btnSettings, btnAuthAction;
    ImageView btnBack, ivProfileAvatar;
    TextView tvProfileName, tvProfileEmail;

    // Các "chìa khóa" (KEY) để lưu và đọc dữ liệu
    public static final String PREFS_NAME = "UserPrefs";
    public static final String IS_LOGGED_IN_KEY = "IsLoggedIn";
    public static final String USER_NAME_KEY = "UserName";
    public static final String USER_EMAIL_KEY = "UserEmail";
    public static final String THEME_COLOR_KEY = "ThemeColor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ các nút
        btnPersonalInfo = findViewById(R.id.btn_personal_info);
        btnShareApp = findViewById(R.id.btn_share_app);
        btnThemeColor = findViewById(R.id.btn_theme_color);
        btnActivitySchedule = findViewById(R.id.btn_activity_schedule);
        btnSettings = findViewById(R.id.btn_settings);
        btnBack = findViewById(R.id.btn_profile_back);
        btnAuthAction = findViewById(R.id.btn_auth_action);
        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);

        // Xử lý click
        btnBack.setOnClickListener(v -> finish());
        btnActivitySchedule.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, ScheduleSettingsActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, SettingsActivity.class)));
        btnPersonalInfo.setOnClickListener(v -> Toast.makeText(this, "Mở Thông tin cá nhân...", Toast.LENGTH_SHORT).show());
        btnShareApp.setOnClickListener(v -> Toast.makeText(this, "Mở Chia sẻ...", Toast.LENGTH_SHORT).show());
        btnThemeColor.setOnClickListener(v -> {
            showColorPickerDialog();
        });

        // Xử lý Bottom Navigation (layout hiện tại không có bottom nav)
        bottomNav = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Gọi hàm này trong onResume()
        checkLoginState();

        // --- SỬA LỖI Ở ĐÂY ---
        // Đặt viên thuốc vào "Cá nhân" MỖI KHI quay lại màn hình này
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
        }
    }

    // ... (Hàm checkLoginState, showLogoutDialog, saveLoginState,
    //      showColorPickerDialog, saveThemeColor giữ nguyên như cũ) ...

    private void checkLoginState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(IS_LOGGED_IN_KEY, false);
        if (isLoggedIn) {
            String name = prefs.getString(USER_NAME_KEY, "Người dùng");
            String email = prefs.getString(USER_EMAIL_KEY, "email@example.com");
            tvProfileName.setText(name);
            tvProfileEmail.setText(email);
            tvProfileName.setVisibility(View.VISIBLE);
            tvProfileEmail.setVisibility(View.VISIBLE);
            ivProfileAvatar.setImageResource(R.drawable.ic_profile);
            btnAuthAction.setText("Đăng xuất");
            btnAuthAction.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_logout, 0);
            btnAuthAction.setOnClickListener(v -> { showLogoutDialog(); });
        } else {
            tvProfileName.setText("Khách");
            tvProfileEmail.setVisibility(View.GONE);
            ivProfileAvatar.setImageResource(R.drawable.ic_profile_avatar);
            btnAuthAction.setText("Đăng nhập");
            btnAuthAction.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_login, 0);
            btnAuthAction.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }
    }
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    saveLoginState(false);
                    checkLoginState();
                    Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(IS_LOGGED_IN_KEY, isLoggedIn);
        if (!isLoggedIn) {
            editor.remove(USER_NAME_KEY);
            editor.remove(USER_EMAIL_KEY);
        }
        editor.commit();
    }
    private void showColorPickerDialog() {
        final String[] colorNames = {"Xanh nhạt (Mặc định)", "Hồng nhạt", "Vàng nhạt"};
        final String[] colorHexCodes = {"#E0F7FA", "#FCE4EC", "#FFF8E1"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn màu nền yêu thích");
        int checkedItem = 0;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedColor = prefs.getString(THEME_COLOR_KEY, "#E0F7FA");
        for (int i = 0; i < colorHexCodes.length; i++) {
            if (colorHexCodes[i].equalsIgnoreCase(savedColor)) {
                checkedItem = i;
                break;
            }
        }
        builder.setSingleChoiceItems(colorNames, checkedItem, (dialog, which) -> {
            String selectedColorHex = colorHexCodes[which];
            saveThemeColor(selectedColorHex);
            Toast.makeText(this, "Đã đổi màu nền!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void saveThemeColor(String colorHex) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(THEME_COLOR_KEY, colorHex);
        editor.commit();
    }


    /**
     * Hàm xử lý Bottom Navigation (SỬA LẠI)
     */
    private void setupBottomNavigation() {
        // --- SỬA LỖI Ở ĐÂY ---
        // XÓA dòng này khỏi setupBottomNavigation()
        // bottomNav.setSelectedItemId(R.id.nav_profile);

        if (bottomNav == null) return; // không có bottom nav ở layout hiện tại
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(ProfileActivity.this, MainActivity.class);
            } else if (itemId == R.id.nav_schedule) {
                intent = new Intent(ProfileActivity.this, ScheduleActivity.class);
            } else if (itemId == R.id.nav_progress) {
                intent = new Intent(ProfileActivity.this, ProgressActivity.class);
            } else if (itemId == R.id.nav_community) {
                Toast.makeText(ProfileActivity.this, "Mở Cộng đồng", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_profile) {
                return true; // Đã ở đây, không làm gì
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); // Tắt hiệu ứng nháy
            }
            return true;
        });
    }
}