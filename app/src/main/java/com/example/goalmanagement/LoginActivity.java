package com.example.goalmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;
import com.example.goalmanagement.data.AppDatabase;
import com.example.goalmanagement.data.User;

public class LoginActivity extends AppCompatActivity {

    Button btnLoginSubmit;
    TextView tvGoToRegister, tvForgotPassword;
    EditText etLoginEmail, etLoginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ các nút
        btnLoginSubmit = findViewById(R.id.btn_login_submit);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        etLoginEmail = findViewById(R.id.et_login_email);
        etLoginPassword = findViewById(R.id.et_login_password);

        // Xử lý sự kiện click
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Xử lý khi nhấn nút ĐĂNG NHẬP
        btnLoginSubmit.setOnClickListener(v -> {
            // Lấy dữ liệu người dùng nhập
            String email = etLoginEmail.getText().toString().trim();
            String password = etLoginPassword.getText().toString().trim();

            // Kiểm tra dữ liệu
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đăng nhập đơn giản (mô phỏng)
            loginSimple(email, password);
        });
    }

    private void loginSimple(String email, String password) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User user = db.userDao().getByEmail(email);
            runOnUiThread(() -> {
                if (user != null && user.password.equals(password)) {
                    // Đăng nhập thành công
                    String displayName = user.name;

                    // Lưu trạng thái đăng nhập
                    saveLoginState(true, displayName, email);

                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Đóng màn hình Login
                    finish();
                } else {
                    // Đăng nhập thất bại
                    Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Hàm tiện ích để lưu trạng thái đăng nhập
     */
    private void saveLoginState(boolean isLoggedIn, String name, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(ProfileActivity.PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(ProfileActivity.IS_LOGGED_IN_KEY, isLoggedIn);
        editor.putString(ProfileActivity.USER_NAME_KEY, name);
        editor.putString(ProfileActivity.USER_EMAIL_KEY, email);
        editor.apply();
    }
}
