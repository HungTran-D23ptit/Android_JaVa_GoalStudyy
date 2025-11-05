package com.example.goalmanagement;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.material.switchmaterial.SwitchMaterial; // Import Switch

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    ImageView btnBack;
    SwitchMaterial switchPomodoro, switchAutoPostpone, switchReminders, switchWeeklyReport, switchGoalWarnings, switchAchievements;
    Spinner spinnerBreakTime;
    TextView tvGoogleCalendarStatus; // Giả sử là TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ánh xạ View
        btnBack = findViewById(R.id.btn_back_settings);
        switchPomodoro = findViewById(R.id.switch_pomodoro);
        switchAutoPostpone = findViewById(R.id.switch_auto_postpone);
        spinnerBreakTime = findViewById(R.id.spinner_break_time);
        tvGoogleCalendarStatus = findViewById(R.id.tv_google_calendar_status);
        switchReminders = findViewById(R.id.switch_reminders);
        switchWeeklyReport = findViewById(R.id.switch_weekly_report);
        switchGoalWarnings = findViewById(R.id.switch_goal_warnings);
        switchAchievements = findViewById(R.id.switch_achievements);

        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Cấu hình Spinner "Nghỉ giữa task"
        setupBreakTimeSpinner();

        // Lưu/tải trạng thái của các Switch (SharedPreferences)
        android.content.SharedPreferences prefs = getSharedPreferences(ProfileActivity.PREFS_NAME, MODE_PRIVATE);
        switchPomodoro.setChecked(prefs.getBoolean("pref_pomodoro", false));
        switchAutoPostpone.setChecked(prefs.getBoolean("pref_auto_postpone", true));
        switchReminders.setChecked(prefs.getBoolean("pref_reminders", true));
        switchWeeklyReport.setChecked(prefs.getBoolean("pref_weekly_report", true));
        switchGoalWarnings.setChecked(prefs.getBoolean("pref_goal_warnings", true));
        switchAchievements.setChecked(prefs.getBoolean("pref_achievements", true));

        switchPomodoro.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("pref_pomodoro", isChecked).apply());
        switchAutoPostpone.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean("pref_auto_postpone", v).apply());
        switchReminders.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean("pref_reminders", v).apply());
        switchWeeklyReport.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean("pref_weekly_report", v).apply());
        switchGoalWarnings.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean("pref_goal_warnings", v).apply());
        switchAchievements.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean("pref_achievements", v).apply());

        tvGoogleCalendarStatus.setOnClickListener(v -> {
            // TODO: Xử lý logic kết nối Google Calendar
            Toast.makeText(this, "Kết nối Google Calendar...", Toast.LENGTH_SHORT).show();
        });
    }

    // Hàm thiết lập Spinner thời gian nghỉ
    private void setupBreakTimeSpinner() {
        List<String> breakOptions = new ArrayList<>();
        breakOptions.add("5 phút");
        breakOptions.add("10 phút");
        breakOptions.add("15 phút");
        breakOptions.add("Không nghỉ");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, breakOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreakTime.setAdapter(adapter);

        android.content.SharedPreferences prefs = getSharedPreferences(ProfileActivity.PREFS_NAME, MODE_PRIVATE);
        int saved = prefs.getInt("pref_break_minutes", 10);
        int index = 1; // mặc định 10 phút
        if (saved == 5) index = 0; else if (saved == 10) index = 1; else if (saved == 15) index = 2; else if (saved == 0) index = 3;
        spinnerBreakTime.setSelection(index);
        spinnerBreakTime.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                int minutes = pos==0?5: pos==1?10: pos==2?15:0;
                prefs.edit().putInt("pref_break_minutes", minutes).apply();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }
}