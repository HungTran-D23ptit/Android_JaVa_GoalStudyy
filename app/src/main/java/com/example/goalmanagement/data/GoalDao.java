package com.example.goalmanagement.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GoalDao {
    @Insert
    long insert(Goal goal);

    @Update
    void update(Goal goal);

    @Query("SELECT * FROM goals ORDER BY createdAtMillis DESC")
    List<Goal> getAll();

    @Query("SELECT * FROM goals WHERE id=:id LIMIT 1")
    Goal getById(long id);
}


