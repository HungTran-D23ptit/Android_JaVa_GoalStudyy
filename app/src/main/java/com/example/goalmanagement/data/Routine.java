package com.example.goalmanagement.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routine")
public class Routine {
    @PrimaryKey(autoGenerate = true)
    public long id;

    // Thời điểm dậy/ngủ (phút kể từ 00:00)
    public int wakeMinutesOfDay;   // ví dụ 7:00 => 420
    public int sleepMinutesOfDay;  // ví dụ 23:00 => 1380

    // Khung học mặc định trong ngày (phút kể từ 00:00)
    public int studyStartMinutesOfDay; // ví dụ 19:00 => 1140
    public int studyEndMinutesOfDay;   // ví dụ 22:30 => 1350

    // Thời gian nghỉ giữa môn (phút)
    public int breakMinutes;

    public Routine(int wakeMinutesOfDay,
                   int sleepMinutesOfDay,
                   int studyStartMinutesOfDay,
                   int studyEndMinutesOfDay,
                   int breakMinutes) {
        this.wakeMinutesOfDay = wakeMinutesOfDay;
        this.sleepMinutesOfDay = sleepMinutesOfDay;
        this.studyStartMinutesOfDay = studyStartMinutesOfDay;
        this.studyEndMinutesOfDay = studyEndMinutesOfDay;
        this.breakMinutes = breakMinutes;
    }
}


