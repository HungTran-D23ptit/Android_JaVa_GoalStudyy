package com.example.goalmanagement.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String email;

    @NonNull
    public String password;

    @NonNull
    public String name;

    public long createdAtMillis;

    public User(@NonNull String email, @NonNull String password, @NonNull String name, long createdAtMillis) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.createdAtMillis = createdAtMillis;
    }
}
