package com.example.goalmanagement.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "goals")
public class Goal {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;

    // Deadline tính bằng millis epoch
    public long deadlineAtMillis;

    // Thời lượng học mục tiêu mỗi ngày (phút)
    public int minutesPerDay;

    // Danh sách môn/vấn đề sẽ học, phân tách bởi dấu phẩy để xen kẽ
    @NonNull
    public String subjectsCsv;

    // Điểm tiến độ/penalty đơn giản
    public int progressScore;

    public long createdAtMillis;

    public Goal(@NonNull String title,
                long deadlineAtMillis,
                int minutesPerDay,
                @NonNull String subjectsCsv,
                int progressScore,
                long createdAtMillis) {
        this.title = title;
        this.deadlineAtMillis = deadlineAtMillis;
        this.minutesPerDay = minutesPerDay;
        this.subjectsCsv = subjectsCsv;
        this.progressScore = progressScore;
        this.createdAtMillis = createdAtMillis;
    }
}


