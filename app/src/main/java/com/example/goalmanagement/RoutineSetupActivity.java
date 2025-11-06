package com.example.goalmanagement;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.goalmanagement.data.AppDatabase;
import com.example.goalmanagement.data.Routine;

import java.util.ArrayList;
import java.util.List;

public class RoutineSetupActivity extends AppCompatActivity {

    // UI Components
    TextView tvWelcome;
    TimePicker timePickerWakeUp, timePickerSleep;
    LinearLayout layoutStudyDays;
    CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    EditText etBreakMinutes;
    Button btnGenerateRoutine;

    // Data
    private AppDatabase db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_setup);

        // Initialize database
        db = AppDatabase.getInstance(this);
        prefs = getSharedPreferences(ProfileActivity.PREFS_NAME, MODE_PRIVATE);

        // Initialize UI
        initializeViews();
        setupUI();
        setupClickListeners();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tv_routine_setup_welcome);
        timePickerWakeUp = findViewById(R.id.time_picker_wake_up);
        timePickerSleep = findViewById(R.id.time_picker_sleep);
        layoutStudyDays = findViewById(R.id.layout_study_days);
        cbMonday = findViewById(R.id.cb_monday);
        cbTuesday = findViewById(R.id.cb_tuesday);
        cbWednesday = findViewById(R.id.cb_wednesday);
        cbThursday = findViewById(R.id.cb_thursday);
        cbFriday = findViewById(R.id.cb_friday);
        cbSaturday = findViewById(R.id.cb_saturday);
        cbSunday = findViewById(R.id.cb_sunday);
        etBreakMinutes = findViewById(R.id.et_break_minutes);
        btnGenerateRoutine = findViewById(R.id.btn_generate_routine);
    }

    private void setupUI() {
        // Set default values
        timePickerWakeUp.setHour(6);
        timePickerWakeUp.setMinute(30);
        timePickerSleep.setHour(23);
        timePickerSleep.setMinute(0);

        // Default study days: Monday to Friday
        cbMonday.setChecked(true);
        cbTuesday.setChecked(true);
        cbWednesday.setChecked(true);
        cbThursday.setChecked(true);
        cbFriday.setChecked(true);

        // Default break time
        etBreakMinutes.setText("10");

        // Welcome message
        tvWelcome.setText("Thiết lập Routine cá nhân\nChúng ta sẽ thiết kế lịch học phù hợp với nhịp sống của bạn.");
    }

    private void setupClickListeners() {
        btnGenerateRoutine.setOnClickListener(v -> generateRoutine());
    }

    private void generateRoutine() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Get input values
        int wakeHour = timePickerWakeUp.getHour();
        int wakeMinute = timePickerWakeUp.getMinute();
        int sleepHour = timePickerSleep.getHour();
        int sleepMinute = timePickerSleep.getMinute();

        int wakeMinutesOfDay = wakeHour * 60 + wakeMinute;
        int sleepMinutesOfDay = sleepHour * 60 + sleepMinute;

        List<Integer> studyDays = getSelectedStudyDays();
        int breakMinutes = Integer.parseInt(etBreakMinutes.getText().toString().trim());

        // Generate default routine
        String studyDaysCsv = buildStudyDaysCsv(studyDays);
        Routine routine = new Routine(wakeMinutesOfDay, sleepMinutesOfDay, 19*60, 22*60, breakMinutes, studyDaysCsv);

        // Save to database
        new Thread(() -> {
            long routineId = db.routineDao().insert(routine);

            // Mark routine as set up
            runOnUiThread(() -> {
                prefs.edit().putBoolean("ROUTINE_SETUP_COMPLETED", true).apply();

                Toast.makeText(this, "Routine đã được tạo thành công!", Toast.LENGTH_SHORT).show();

                // Navigate to main activity
                Intent intent = new Intent(RoutineSetupActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }).start();
    }

    private boolean validateInput() {
        // Check wake up time
        int wakeHour = timePickerWakeUp.getHour();
        int sleepHour = timePickerSleep.getHour();

        if (wakeHour >= sleepHour) {
            Toast.makeText(this, "Giờ dậy phải sớm hơn giờ ngủ", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check study days
        if (getSelectedStudyDays().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một ngày học", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check break minutes
        String breakStr = etBreakMinutes.getText().toString().trim();
        if (breakStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập thời gian nghỉ", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int breakMinutes = Integer.parseInt(breakStr);
            if (breakMinutes < 5 || breakMinutes > 60) {
                Toast.makeText(this, "Thời gian nghỉ phải từ 5-60 phút", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời gian nghỉ phải là số", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private List<Integer> getSelectedStudyDays() {
        List<Integer> days = new ArrayList<>();
        if (cbMonday.isChecked()) days.add(2); // Calendar.MONDAY
        if (cbTuesday.isChecked()) days.add(3); // Calendar.TUESDAY
        if (cbWednesday.isChecked()) days.add(4); // Calendar.WEDNESDAY
        if (cbThursday.isChecked()) days.add(5); // Calendar.THURSDAY
        if (cbFriday.isChecked()) days.add(6); // Calendar.FRIDAY
        if (cbSaturday.isChecked()) days.add(7); // Calendar.SATURDAY
        if (cbSunday.isChecked()) days.add(1); // Calendar.SUNDAY
        return days;
    }

    private String buildStudyDaysCsv(List<Integer> studyDays) {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < studyDays.size(); i++) {
            if (i > 0) csv.append(",");
            csv.append(studyDays.get(i));
        }
        return csv.toString();
    }
}
