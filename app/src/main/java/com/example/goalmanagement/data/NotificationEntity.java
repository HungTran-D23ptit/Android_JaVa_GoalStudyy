package com.example.goalmanagement.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;

    @NonNull
    public String content;

    public long timestamp; // Thời gian tạo notification

    @NonNull
    public String type; // "reminder", "warning", "report", "progress"

    public boolean isRead = false;

    public long relatedTaskId = -1; // ID của task liên quan (nếu có)

    public NotificationEntity(@NonNull String title, @NonNull String content, long timestamp, @NonNull String type) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
    }

    @Ignore
    public NotificationEntity(@NonNull String title, @NonNull String content, long timestamp, @NonNull String type, long relatedTaskId) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
        this.relatedTaskId = relatedTaskId;
    }
}
