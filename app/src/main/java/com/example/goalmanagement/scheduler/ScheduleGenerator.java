package com.example.goalmanagement.scheduler;

import android.content.Context;

import com.example.goalmanagement.data.AppDatabase;
import com.example.goalmanagement.data.Goal;
import com.example.goalmanagement.data.Routine;
import com.example.goalmanagement.data.Task;
import com.example.goalmanagement.data.TaskDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleGenerator {

    private static String dayKey(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    private static long millisOfDay(Calendar base, int minutesOfDay) {
        Calendar c = (Calendar) base.clone();
        c.set(Calendar.HOUR_OF_DAY, minutesOfDay / 60);
        c.set(Calendar.MINUTE, minutesOfDay % 60);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static List<Task> generateDailyTasks(long goalId, String title, String[] subjects, int minutesPerDay, Routine routine, Calendar day) {
        List<Task> result = new ArrayList<>();
        if (routine == null) {
            routine = new Routine(420, 1380, 1140, 1350, 10, "2,3,4,5,6");
        }

        int studyWindow = Math.max(0, routine.studyEndMinutesOfDay - routine.studyStartMinutesOfDay);
        if (studyWindow <= 0) return result;

        int remaining = Math.min(minutesPerDay, studyWindow);
        int sessionLen = Math.min(45, remaining); // một phiên mặc định 45′ để có nhiều phiên hơn
        int breakLen = Math.max(5, routine.breakMinutes);

        int cursor = routine.studyStartMinutesOfDay;
        int idx = 0;
        String dayKey = dayKey(day);

        while (remaining > 0 && cursor < routine.studyEndMinutesOfDay) {
            String subject = subjects[idx % subjects.length].trim();
            int thisSession = Math.min(sessionLen, Math.min(remaining, routine.studyEndMinutesOfDay - cursor));
            long start = millisOfDay(day, cursor);
            long end = millisOfDay(day, cursor + thisSession);
            Task t = new Task(goalId, title, subject, start, end, thisSession, dayKey, "pending");
            result.add(t);

            remaining -= thisSession;
            cursor += thisSession;

            if (remaining <= 0) break;

            // chèn nghỉ nếu còn thời gian và chưa hết khung học
            int actualBreak = Math.min(breakLen, Math.max(0, routine.studyEndMinutesOfDay - cursor));
            if (actualBreak > 0 && remaining > 0) {
                Task rest = new Task(goalId, "Nghỉ ngơi", "Break", millisOfDay(day, cursor), millisOfDay(day, cursor + actualBreak), actualBreak, dayKey, "pending");
                result.add(rest);
                cursor += actualBreak;
            }

            idx++;
        }
        return result;
    }

    public static void generateAndSave(Context context, Goal goal, Routine routine) {
        AppDatabase db = AppDatabase.getInstance(context);
        TaskDao taskDao = db.taskDao();

        String[] subjects = goal.subjectsCsv.split(",");
        String[] studyDaysStr = routine.studyDaysCsv.split(",");
        List<Integer> studyDays = new ArrayList<>();
        for (String day : studyDaysStr) {
            try {
                studyDays.add(Integer.parseInt(day.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid days
            }
        }

        Calendar today = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(goal.deadlineAtMillis);

        Calendar cursor = (Calendar) today.clone();
        cursor.set(Calendar.HOUR_OF_DAY, 0);
        cursor.set(Calendar.MINUTE, 0);
        cursor.set(Calendar.SECOND, 0);
        cursor.set(Calendar.MILLISECOND, 0);

        while (!cursor.after(end)) {
            // Check if this day is a study day
            int dayOfWeek = cursor.get(Calendar.DAY_OF_WEEK);
            if (studyDays.contains(dayOfWeek)) {
                List<Task> tasks = generateDailyTasks(goal.id, goal.title, subjects, goal.minutesPerDay, routine, cursor);
                if (!tasks.isEmpty()) {
                    taskDao.insertAll(tasks);
                }
            }
            cursor.add(Calendar.DATE, 1);
        }
    }
}


