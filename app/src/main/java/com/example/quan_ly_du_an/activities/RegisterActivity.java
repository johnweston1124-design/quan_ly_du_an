package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.model.User;
import com.example.quan_ly_du_an.databinding.ActivityRegisterBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AppDatabase database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getDatabase(this);

        // Xử lý nút Đăng ký ngay
        binding.btnRegister.setOnClickListener(v -> handleRegister());

        // Xử lý text Đã có tài khoản -> Quay lại Login
        binding.tvLogin.setOnClickListener(v -> {
            finish(); // Đóng màn hình đăng ký, tự động quay về màn hình đăng nhập
        });

        // Xử lý các nút MXH (Mô phỏng)
        binding.btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đăng ký Google đang phát triển", Toast.LENGTH_SHORT).show();
        });

        binding.btnFacebook.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đăng ký Facebook đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleRegister() {
        String username = binding.etUsername.getText() != null ? binding.etUsername.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";

        // Reset lỗi hiển thị
        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
        binding.tilEmail.setError(null);

        // Validation cơ bản
        if (username.isEmpty() || username.length() < 3) {
            binding.tilUsername.setError("Tên đăng nhập phải từ 3 ký tự");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            binding.tilPassword.setError("Mật khẩu phải từ 6 ký tự");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            binding.tilEmail.setError("Email không hợp lệ");
            return;
        }

        // Đẩy xuống Background Thread để thao tác với Database
        executorService.execute(() -> {
            User newUser = new User(username, email, password);

            try {
                database.userDao().insertUser(newUser);

                // Lấy user vừa insert để lấy ID chính xác
                User createdUser = database.userDao().getUserByEmail(email);
                int userId = createdUser != null ? createdUser.getId() : newUser.getId();

                runOnUiThread(() -> {
                    // Lưu phiên đăng nhập tự động
                    SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("IS_LOGGED_IN", true);
                    editor.putInt("USER_ID", userId);
                    editor.apply();

                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Đang chuyển đến màn hình chính...", Toast.LENGTH_SHORT).show();

                    // Chuyển thẳng sang MainActivity và xóa stack trước đó
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại. Email/Username có thể đã tồn tại.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
