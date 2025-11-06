package com.example.goalmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.goalmanagement.data.AppDatabase;
import com.example.goalmanagement.data.NotificationEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.graphics.Color;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnItemClickListener {

    RecyclerView rvNotifications;
    NotificationAdapter adapter;
    List<Notification> notificationList;
    TextView btnMarkAllRead;
    ImageView btnBack;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = AppDatabase.getInstance(this);

        rvNotifications = findViewById(R.id.rv_notifications);
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read);
        btnBack = findViewById(R.id.btn_back_notification);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Thiết lập RecyclerView
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Load dữ liệu từ database
        loadNotifications();

        // Xử lý nút "Đánh dấu đã đọc"
        btnMarkAllRead.setOnClickListener(v -> {
            new Thread(() -> {
                db.notificationDao().markAllAsRead();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();
                    btnMarkAllRead.setTextColor(Color.GRAY);
                    btnMarkAllRead.setEnabled(false);
                    loadNotifications(); // Reload để cập nhật UI
                });
            }).start();
        });
    }

    private void loadNotifications() {
        new Thread(() -> {
            List<NotificationEntity> entities = db.notificationDao().getAll();
            notificationList = new ArrayList<>();

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            for (NotificationEntity entity : entities) {
                String timeAgo = getTimeAgo(entity.timestamp);
                String detailDateTime = getDetailDateTime(entity.timestamp);

                String iconType = getIconTypeForNotification(entity.type);

                Notification notification = new Notification(
                    entity.title,
                    entity.content,
                    timeAgo,
                    iconType,
                    entity.isRead,
                    detailDateTime
                );
                notificationList.add(notification);
            }

            runOnUiThread(() -> {
                adapter = new NotificationAdapter(this, notificationList);
                adapter.setOnItemClickListener(this);
                rvNotifications.setAdapter(adapter);

                // Cập nhật trạng thái nút "Đánh dấu tất cả đã đọc"
                boolean hasUnread = notificationList.stream().anyMatch(n -> !n.isRead);
                btnMarkAllRead.setEnabled(hasUnread);
                btnMarkAllRead.setTextColor(hasUnread ? Color.BLACK : Color.GRAY);
            });
        }).start();
    }

    private String getIconTypeForNotification(String type) {
        switch (type) {
            case "reminder": return "bell";
            case "warning": return "warning";
            case "report": return "chart";
            case "progress": return "progress";
            default: return "bell";
        }
    }

    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }

    private String getDetailDateTime(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE 'ngày' dd/MM/yyyy '•' HH:mm", new Locale("vi", "VN"));
        return dateFormat.format(new Date(timestamp));
    }

    @Override
    public void onItemClick(Notification notification) {
        // Đánh dấu là đã đọc trong database
        int index = notificationList.indexOf(notification);
        if (index >= 0) {
            new Thread(() -> {
                // Tìm entity tương ứng trong database
                List<NotificationEntity> entities = db.notificationDao().getAll();
                if (index < entities.size()) {
                    NotificationEntity entity = entities.get(index);
                    db.notificationDao().markAsRead(entity.id);

                    runOnUiThread(() -> {
                        notification.isRead = true;
                        adapter.notifyItemChanged(index);
                        Toast.makeText(this, "Đã đánh dấu là đã đọc", Toast.LENGTH_SHORT).show();

                        // Cập nhật trạng thái nút "Đánh dấu tất cả đã đọc"
                        boolean hasUnread = notificationList.stream().anyMatch(n -> !n.isRead);
                        btnMarkAllRead.setEnabled(hasUnread);
                        btnMarkAllRead.setTextColor(hasUnread ? Color.BLACK : Color.GRAY);
                    });
                }
            }).start();
        }
    }
}
