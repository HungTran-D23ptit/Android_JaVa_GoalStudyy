package com.example.goalmanagement.data;

import androidx.room.Entity;
import androidx.room.Ignore;
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

    // Ngày học trong tuần (dạng CSV: "2,3,4,5,6" cho Thứ 2-Thứ 6)
    public String studyDaysCsv;

    @Ignore
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
        this.studyDaysCsv = "2,3,4,5,6"; // Mặc định Thứ 2-Thứ 6
    }

    public Routine(int wakeMinutesOfDay,
                   int sleepMinutesOfDay,
                   int studyStartMinutesOfDay,
                   int studyEndMinutesOfDay,
                   int breakMinutes,
                   String studyDaysCsv) {
        this.wakeMinutesOfDay = wakeMinutesOfDay;
        this.sleepMinutesOfDay = sleepMinutesOfDay;
        this.studyStartMinutesOfDay = studyStartMinutesOfDay;
        this.studyEndMinutesOfDay = studyEndMinutesOfDay;
        this.breakMinutes = breakMinutes;
        this.studyDaysCsv = studyDaysCsv;
    }
}


