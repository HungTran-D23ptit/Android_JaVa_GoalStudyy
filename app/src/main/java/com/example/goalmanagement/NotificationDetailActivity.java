package com.example.goalmanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        ImageView close = findViewById(R.id.closeButton);
        ImageView action = findViewById(R.id.actionButton);
        TextView tvTitle = findViewById(R.id.detailTitle);
        TextView tvContent = findViewById(R.id.detailContent);
        TextView tvDateTime = findViewById(R.id.detailDateTime);
        Button btnView = findViewById(R.id.viewScheduleButton);
        Button btnStart = findViewById(R.id.startButton);

        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String dateTime = getIntent().getStringExtra("dateTime");

        if (title != null) tvTitle.setText(title);
        if (content != null) tvContent.setText(content);
        if (dateTime != null) tvDateTime.setText(dateTime);

        close.setOnClickListener(v -> finish());
        action.setOnClickListener(v -> finish());
        btnView.setOnClickListener(v -> finish());
        btnStart.setOnClickListener(v -> finish());
    }
}