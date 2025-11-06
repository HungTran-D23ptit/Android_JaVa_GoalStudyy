package com.example.goalmanagement;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NewPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmNewPassword;
    private Button btnContinue;
    private ImageView btnBack;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        // Get email from intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        // Ánh xạ views
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        btnContinue = findViewById(R.id.btn_new_pass_continue);
        btnBack = findViewById(R.id.btn_new_pass_back);

        btnBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmNewPassword.getText().toString().trim();

            // Validation
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mô phỏng cập nhật mật khẩu
            updatePasswordSimple(newPassword);
        });
    }

    private void updatePasswordSimple(String newPassword) {
        // Mô phỏng cập nhật mật khẩu - luôn thành công
        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();

        // Navigate back to login
        Intent intent = new Intent(NewPasswordActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
