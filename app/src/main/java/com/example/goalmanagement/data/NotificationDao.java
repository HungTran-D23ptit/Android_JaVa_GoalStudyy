package com.example.goalmanagement.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    long insert(NotificationEntity notification);

    @Update
    void update(NotificationEntity notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    List<NotificationEntity> getAll();

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY timestamp DESC")
    List<NotificationEntity> getUnread();

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    int getUnreadCount();

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markAsRead(long id);

    @Query("UPDATE notifications SET isRead = 1")
    void markAllAsRead();

    @Query("DELETE FROM notifications WHERE timestamp < :beforeTimestamp")
    void deleteOldNotifications(long beforeTimestamp);

    @Query("SELECT * FROM notifications WHERE type = :type ORDER BY timestamp DESC LIMIT 10")
    List<NotificationEntity> getByType(String type);
}
