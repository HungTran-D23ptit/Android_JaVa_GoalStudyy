package com.example.goalmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.goalmanagement.data.AppDatabase;
import com.example.goalmanagement.data.Goal;
import com.example.goalmanagement.data.Routine;
import com.example.goalmanagement.scheduler.ScheduleGenerator;
import java.util.Calendar;

public class CreateGoalActivity extends AppCompatActivity {
    EditText etCurrentScore, etTargetScore, etGoalTime, etHoursPerDay;
    Button btnCreateGoalFinal;
    CardView cardGoalTypeToeic, cardGoalTypeIelts, cardGoalTypeReading, cardGoalTypeExercise;
    private CardView selectedGoalTypeCard = null;
    private String selectedGoalType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goal);
        etCurrentScore = findViewById(R.id.et_current_score);
        etTargetScore = findViewById(R.id.et_target_score);
        etGoalTime = findViewById(R.id.et_goal_time);
        etHoursPerDay = findViewById(R.id.et_hours_per_day);
        btnCreateGoalFinal = findViewById(R.id.btn_create_goal_final);
        cardGoalTypeToeic = findViewById(R.id.card_goal_type_toeic);
        cardGoalTypeIelts = findViewById(R.id.card_goal_type_ielts);
        cardGoalTypeReading = findViewById(R.id.card_goal_type_reading);
        cardGoalTypeExercise = findViewById(R.id.card_goal_type_exercise);
        etGoalTime.setOnClickListener(v -> showDatePickerDialog(etGoalTime));
        etHoursPerDay.setOnClickListener(v -> showTimePickerDialog(etHoursPerDay));
        setupGoalTypeCards();

        btnCreateGoalFinal.setOnClickListener(v -> {
            if (selectedGoalTypeCard == null) {
                Toast.makeText(this, "Vui lòng chọn loại mục tiêu", Toast.LENGTH_SHORT).show();
                return;
            }
            String goalTime = etGoalTime.getText().toString().trim();
            if (goalTime.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày hoàn thành (dd.MM.yyyy)", Toast.LENGTH_SHORT).show();
                etGoalTime.requestFocus();
                return;
            }
            String hoursPerDay = etHoursPerDay.getText().toString().trim();
            if (hoursPerDay.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn thời lượng học mỗi ngày (HH:mm)", Toast.LENGTH_SHORT).show();
                etHoursPerDay.requestFocus();
                return;
            }
            String currentScore = etCurrentScore.getText().toString().trim();
            String targetScore = etTargetScore.getText().toString().trim();

            try {
                String[] parts;
                if (goalTime.contains(".")) parts = goalTime.split("\\.");
                else if (goalTime.contains("/")) parts = goalTime.split("/");
                else {
                    Toast.makeText(this, "Định dạng ngày không hợp lệ. Dùng dd.MM.yyyy", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (parts.length != 3) {
                    Toast.makeText(this, "Định dạng ngày không hợp lệ. Dùng dd.MM.yyyy", Toast.LENGTH_SHORT).show();
                    return;
                }
                int d = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]) - 1;
                int y = Integer.parseInt(parts[2]);
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, y);
                cal.set(Calendar.MONTH, m);
                cal.set(Calendar.DAY_OF_MONTH, d);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long deadlineMillis = cal.getTimeInMillis();
                long todayMillis = System.currentTimeMillis();
                if (deadlineMillis <= todayMillis) {
                    Toast.makeText(this, "Ngày hoàn thành phải sau ngày hôm nay", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] hm = hoursPerDay.split(":");
                if (hm.length != 2) {
                    Toast.makeText(this, "Định dạng thời lượng không hợp lệ. Dùng HH:mm", Toast.LENGTH_SHORT).show();
                    return;
                }
                int hh = Integer.parseInt(hm[0]);
                int mm = Integer.parseInt(hm[1]);
                int minutesPerDay = hh * 60 + mm;
                if (minutesPerDay <= 0) {
                    Toast.makeText(this, "Thời lượng học mỗi ngày phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (minutesPerDay > 600) {
                    Toast.makeText(this, "Thời lượng học mỗi ngày quá lớn (> 10 giờ)", Toast.LENGTH_SHORT).show();
                    return;
                }

                String subjectsCsv;
                if ("Toeic".equalsIgnoreCase(selectedGoalType)) subjectsCsv = "Listening,Reading,Vocabulary,Grammar";
                else if ("Ielts".equalsIgnoreCase(selectedGoalType)) subjectsCsv = "Listening,Reading,Writing,Speaking";
                else if ("Đọc sách".equalsIgnoreCase(selectedGoalType)) subjectsCsv = "Reading";
                else if ("Tập thể dục".equalsIgnoreCase(selectedGoalType)) subjectsCsv = "Exercise";
                else subjectsCsv = selectedGoalType;

                new Thread(() -> {
                    try {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        Goal goal = new Goal(selectedGoalType + (targetScore.isEmpty() ? "" : " " + targetScore), deadlineMillis, minutesPerDay, subjectsCsv, 0, System.currentTimeMillis());
                        long goalId = db.goalDao().insert(goal);
                        goal.id = goalId;
                        Routine routine = db.routineDao().getSingle();
                        if (routine == null) {
                            routine = new Routine(420, 1380, 1140, 1350, 10);
                            db.routineDao().insert(routine);
                        }
                        ScheduleGenerator.generateAndSave(getApplicationContext(), goal, routine);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã tạo mục tiêu và sinh lịch học", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, "Không thể tạo mục tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).start();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Sai định dạng số. Vui lòng kiểm tra lại ngày/thời lượng", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Không thể tạo mục tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> editText.setText(String.format("%02d.%02d.%d", dayOfMonth, (monthOfYear + 1), year)), mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> editText.setText(String.format("%02d:%02d", hourOfDay, minute)), mHour, mMinute, true);
        timePickerDialog.show();
    }

    private void setupGoalTypeCards() {
        View.OnClickListener cardClickListener = v -> {
            if (selectedGoalTypeCard != null) resetCardBackground(selectedGoalTypeCard);
            selectedGoalTypeCard = (CardView) v;
            highlightCardBackground(selectedGoalTypeCard);
            int id = v.getId();
            if (id == R.id.card_goal_type_toeic) selectedGoalType = "Toeic";
            else if (id == R.id.card_goal_type_ielts) selectedGoalType = "Ielts";
            else if (id == R.id.card_goal_type_reading) selectedGoalType = "Đọc sách";
            else if (id == R.id.card_goal_type_exercise) selectedGoalType = "Tập thể dục";
        };
        cardGoalTypeToeic.setOnClickListener(cardClickListener);
        cardGoalTypeIelts.setOnClickListener(cardClickListener);
        cardGoalTypeReading.setOnClickListener(cardClickListener);
        cardGoalTypeExercise.setOnClickListener(cardClickListener);
    }

    private void resetCardBackground(CardView cardView) {
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
    }

    private void highlightCardBackground(CardView cardView) {
        int highlightColor = getResources().getColor(R.color.colorCardCreateGoal);
        cardView.setCardBackgroundColor(highlightColor);
    }
}
