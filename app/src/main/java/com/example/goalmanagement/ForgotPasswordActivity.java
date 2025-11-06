package com.example.goalmanagement;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etForgotEmail;
    private Button btnContinue;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Ánh xạ views
        etForgotEmail = findViewById(R.id.et_forgot_email);
        btnContinue = findViewById(R.id.btn_forgot_continue);
        btnBack = findViewById(R.id.btn_forgot_back);

        btnBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            String email = etForgotEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mô phỏng gửi email reset password
            sendPasswordResetEmailSimple(email);
        });
    }

    private void sendPasswordResetEmailSimple(String email) {
        // Mô phỏng gửi email reset password - luôn thành công
        Toast.makeText(this, "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư.",
                Toast.LENGTH_LONG).show();

        // Chuyển đến màn hình xác minh
        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyAccountActivity.class);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }
}
