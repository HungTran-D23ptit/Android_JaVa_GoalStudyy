package com.example.goalmanagement;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.goalmanagement.data.AppDatabase;
import com.example.goalmanagement.data.User;

public class RegisterActivity extends AppCompatActivity {

    EditText etEmail, etName, etPassword, etConfirmPassword;
    Button btnRegisterSubmit;
    TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ View
        etEmail = findViewById(R.id.et_register_email);
        etName = findViewById(R.id.et_register_name);
        etPassword = findViewById(R.id.et_register_password);
        etConfirmPassword = findViewById(R.id.et_register_confirm_password);
        btnRegisterSubmit = findViewById(R.id.btn_register_submit);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);

        // Xử lý nút "Tiếp tục" (Đăng ký)
        btnRegisterSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validation
            if (email.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đăng ký đơn giản (mô phỏng)
            registerSimple(email, password, name);
        });

        // Xử lý nút "Đăng nhập"
        tvGoToLogin.setOnClickListener(v -> {
            finish(); // Quay lại LoginActivity
        });
    }

    private void registerSimple(String email, String password, String name) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User existingUser = db.userDao().getByEmail(email);
            if (existingUser != null) {
                runOnUiThread(() -> Toast.makeText(this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show());
                return;
            }
            User newUser = new User(email, password, name, System.currentTimeMillis());
            db.userDao().insert(newUser);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                // Đóng màn hình đăng ký
                finish();
            });
        }).start();
    }
}
