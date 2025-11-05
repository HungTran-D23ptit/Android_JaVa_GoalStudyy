package com.example.goalmanagement.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface RoutineDao {
    @Insert
    long insert(Routine routine);

    @Update
    void update(Routine routine);

    @Query("SELECT * FROM routine LIMIT 1")
    Routine getSingle();
}


