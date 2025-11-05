package com.example.goalmanagement.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks", indices = {@Index(value = {"goalId", "dayKey"})})
public class Task {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long goalId;

    @NonNull
    public String title; // ví dụ: "Nghe TOEIC"

    @NonNull
    public String subject; // để sắp xếp xen kẽ

    // Khoảng thời gian theo millis epoch
    public long startAtMillis;
    public long endAtMillis;

    public int durationMinutes;

    // dayKey dạng yyyy-MM-dd để truy vấn nhanh theo ngày
    @NonNull
    public String dayKey;

    // status: pending | inprogress | completed | postponed | missed
    @NonNull
    public String status;

    public Task(long goalId,
                @NonNull String title,
                @NonNull String subject,
                long startAtMillis,
                long endAtMillis,
                int durationMinutes,
                @NonNull String dayKey,
                @NonNull String status) {
        this.goalId = goalId;
        this.title = title;
        this.subject = subject;
        this.startAtMillis = startAtMillis;
        this.endAtMillis = endAtMillis;
        this.durationMinutes = durationMinutes;
        this.dayKey = dayKey;
        this.status = status;
    }
}


