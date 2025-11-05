package com.example.goalmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView rvNotifications;
    NotificationAdapter adapter;
    List<Notification> notificationList;
    TextView btnMarkAllRead;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rvNotifications = findViewById(R.id.rv_notifications);
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read);
        btnBack = findViewById(R.id.btn_back_notification);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Tải dữ liệu mẫu
        loadDummyData();

        // Thiết lập Adapter
        adapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        // Xử lý nút "Đánh dấu đã đọc"
        btnMarkAllRead.setOnClickListener(v -> {
            adapter.markAllAsRead();
            btnMarkAllRead.setTextColor(Color.GRAY); // Đổi màu nút sang xám
            btnMarkAllRead.setEnabled(false); // Vô hiệu hóa nút
        });
    }

    // Tạo dữ liệu mẫu (Sau này sẽ lấy từ database)
    private void loadDummyData() {
        notificationList = new ArrayList<>();
        notificationList.add(new Notification("Nhắc nhở học tập",
                "Đã đến giờ học TOEIC Reading Practice (19:00)",
                "5 phút trước",
                "reminder",
                false,
                "Thứ 2 ngày 13/09/2025 • 11:50 AM"));
        notificationList.add(new Notification("Báo cáo tuần",
                "Tuần này bạn đã học 10.5 giờ, giảm 2 giờ so với tuần trước.",
                "5 phút trước",
                "report",
                true,
                "Chủ nhật ngày 12/09/2025 • 9:00 AM"));
        notificationList.add(new Notification("Cảnh báo tiến độ",
                "Bạn đã bỏ qua 2 task hôm nay. Điều này có thể ảnh hưởng đến mục tiêu.",
                "5 phút trước",
                "progress",
                false,
                "Thứ 2 ngày 13/09/2025 • 7:30 AM"));
    }
}