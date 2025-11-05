package com.example.goalmanagement;

import androidx.appcompat.app.AppCompatActivity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter; // Thêm import
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner; // Thêm import
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleSettingsActivity extends AppCompatActivity {

    Spinner spinnerFreeDays;
    EditText etFreeTimeStart, etFreeTimeEnd, etStudyDuration, etBreakDuration;
    Button btnAddFreeTime, btnCreateFixedSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_settings);

        // Ánh xạ View
        spinnerFreeDays = findViewById(R.id.spinner_free_days);
        etFreeTimeStart = findViewById(R.id.et_free_time_start);
        etFreeTimeEnd = findViewById(R.id.et_free_time_end);
        etStudyDuration = findViewById(R.id.et_study_duration);
        etBreakDuration = findViewById(R.id.et_break_duration);
        btnAddFreeTime = findViewById(R.id.btn_add_free_time);
        btnCreateFixedSchedule = findViewById(R.id.btn_create_fixed_schedule);

        // 1. Cấu hình Spinner
        setupDaysSpinner();

        // 2. Cấu hình các EditText chọn giờ
        setupTimePicker(etFreeTimeStart);
        setupTimePicker(etFreeTimeEnd);
        setupTimePicker(etStudyDuration);
        setupTimePicker(etBreakDuration);

        // 3. Xử lý nút bấm
        btnAddFreeTime.setOnClickListener(v -> {
            // TODO: Thêm logic lưu trữ nhiều khoảng thời gian rảnh
            Toast.makeText(this, "Đã thêm thời gian (chưa implement)", Toast.LENGTH_SHORT).show();
        });

        btnCreateFixedSchedule.setOnClickListener(v -> {
            try {
                int studyStart = parseHmToMinutes(etFreeTimeStart.getText().toString());
                int studyEnd = parseHmToMinutes(etFreeTimeEnd.getText().toString());
                int breakMinutes = parseHmToMinutes(etBreakDuration.getText().toString());
                if (studyEnd <= studyStart) {
                    Toast.makeText(this, "Giờ kết thúc phải sau giờ bắt đầu", Toast.LENGTH_SHORT).show();
                    return;
                }
                com.example.goalmanagement.data.AppDatabase db = com.example.goalmanagement.data.AppDatabase.getInstance(getApplicationContext());
                com.example.goalmanagement.data.Routine routine = db.routineDao().getSingle();
                if (routine == null) {
                    routine = new com.example.goalmanagement.data.Routine(420, 1380, studyStart, studyEnd, Math.max(5, breakMinutes));
                    db.routineDao().insert(routine);
                } else {
                    routine.studyStartMinutesOfDay = studyStart;
                    routine.studyEndMinutesOfDay = studyEnd;
                    routine.breakMinutes = Math.max(5, breakMinutes);
                    db.routineDao().update(routine);
                }
                Toast.makeText(this, "Đã lưu routine", Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception e) {
                Toast.makeText(this, "Vui lòng nhập thời gian hợp lệ (HH:mm)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm thiết lập Spinner chọn ngày
    private void setupDaysSpinner() {
        List<String> daysOptions = new ArrayList<>();
        daysOptions.add("Thứ 2 - Thứ 6");
        daysOptions.add("Thứ 7 & Chủ nhật");
        daysOptions.add("Tất cả các ngày");
        // Thêm các tùy chọn khác nếu muốn

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFreeDays.setAdapter(adapter);
    }

    // Hàm gán TimePickerDialog cho một EditText
    private void setupTimePicker(final EditText editText) {
        editText.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mHour = c.get(Calendar.HOUR_OF_DAY);
            int mMinute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        // Format giờ: HH:mm
                        editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    }, mHour, mMinute, true); // true = 24 giờ
            timePickerDialog.show();
        });
    }

    private int parseHmToMinutes(String hm) {
        String[] parts = hm.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        return h * 60 + m;
    }
}