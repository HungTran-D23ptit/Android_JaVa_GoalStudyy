package com.example.goalmanagement;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class VerifyAccountActivity extends AppCompatActivity {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4;
    private TextView tvResendCode;
    private ImageView btnBack;
    private CountDownTimer countDownTimer;
    private boolean isResendEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_account);

        // Ánh xạ views
        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        tvResendCode = findViewById(R.id.tv_resend_code);
        btnBack = findViewById(R.id.btn_verify_back);

        btnBack.setOnClickListener(v -> finish());

        // Setup OTP input auto-focus
        setupOtpInputs();

        // Start countdown timer
        startCountdownTimer();

        // Handle resend code
        tvResendCode.setOnClickListener(v -> {
            if (isResendEnabled) {
                resendVerificationEmailSimple();
            } else {
                Toast.makeText(this, "Vui lòng đợi trước khi gửi lại", Toast.LENGTH_SHORT).show();
            }
        });

        // Check if user is already verified
        checkVerificationStatusSimple();
    }

    private void setupOtpInputs() {
        etOtp1.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Implementation if needed
            }
        });

        etOtp2.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp3.requestFocus();
                } else if (s.length() == 0) {
                    etOtp1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        etOtp3.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp4.requestFocus();
                } else if (s.length() == 0) {
                    etOtp2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        etOtp4.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    // All OTP digits entered, verify
                    verifyOtpSimple();
                } else if (s.length() == 0) {
                    etOtp3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void verifyOtpSimple() {
        String otp = etOtp1.getText().toString() +
                    etOtp2.getText().toString() +
                    etOtp3.getText().toString() +
                    etOtp4.getText().toString();

        if (otp.length() == 4) {
            // Mô phỏng xác minh OTP - luôn thành công nếu có 4 ký tự
            Toast.makeText(this, "Xác minh thành công!", Toast.LENGTH_SHORT).show();

            // Check if this is for password reset or email verification
            Intent intent = getIntent();
            if (intent.hasExtra("USER_EMAIL")) {
                // This is for password reset, go to new password
                Intent newPasswordIntent = new Intent(VerifyAccountActivity.this, NewPasswordActivity.class);
                newPasswordIntent.putExtra("USER_EMAIL", intent.getStringExtra("USER_EMAIL"));
                startActivity(newPasswordIntent);
            } else {
                // This is for email verification after registration
                // User can now proceed to login
                finish();
            }
        }
    }

    private void clearOtpFields() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp1.requestFocus();
    }

    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(60000, 1000) { // 60 seconds
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                // Update UI if needed
                isResendEnabled = false;
            }

            @Override
            public void onFinish() {
                tvResendCode.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                isResendEnabled = true;
            }
        }.start();
    }

    private void resendVerificationEmailSimple() {
        // Mô phỏng gửi lại email xác minh - luôn thành công
        Toast.makeText(this, "Email xác minh đã được gửi lại", Toast.LENGTH_SHORT).show();
        startCountdownTimer();
    }

    private void checkVerificationStatusSimple() {
        // Mô phỏng kiểm tra trạng thái xác minh - luôn chưa xác minh
        // Không làm gì cả
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
