package com.example.goalmanagement.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getById(long id);

    @Query("DELETE FROM users WHERE id = :id")
    void deleteById(long id);
}
