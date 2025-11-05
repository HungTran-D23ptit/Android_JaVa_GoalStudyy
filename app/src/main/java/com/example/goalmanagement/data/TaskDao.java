package com.example.goalmanagement.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    long insert(Task task);

    @Insert
    void insertAll(List<Task> tasks);

    @Update
    void update(Task task);

    @Query("SELECT * FROM tasks WHERE dayKey=:dayKey ORDER BY startAtMillis ASC")
    List<Task> getByDay(String dayKey);

    @Query("SELECT * FROM tasks WHERE id=:id LIMIT 1")
    Task getById(long id);

    @Query("UPDATE tasks SET status=:status WHERE id=:id")
    void updateStatus(long id, String status);

    @Query("DELETE FROM tasks WHERE id=:id")
    void deleteById(long id);

    @Query("SELECT COUNT(*) FROM tasks WHERE goalId=:goalId")
    int countByGoal(long goalId);

    @Query("SELECT COUNT(*) FROM tasks WHERE goalId=:goalId AND status=:status")
    int countByGoalAndStatus(long goalId, String status);

    @Query("SELECT COUNT(*) FROM tasks WHERE dayKey BETWEEN :fromDay AND :toDay AND status=:status")
    int countByDayRangeAndStatus(String fromDay, String toDay, String status);
}


