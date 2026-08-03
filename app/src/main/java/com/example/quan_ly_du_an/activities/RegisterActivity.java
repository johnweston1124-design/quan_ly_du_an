package com.example.quan_ly_du_an.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.model.User;
import com.example.quan_ly_du_an.api.MongoApiService;
import com.example.quan_ly_du_an.api.RetrofitClient;
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

        // Đẩy xuống Background Thread để thao tác với Room Database
        executorService.execute(() -> {
            // Đã khởi tạo User đầy đủ tham số
            User newUser = new User(username, email, password);

            try {
                // SỬA LỖI: Gọi đúng hàm insertUser
                database.userDao().insertUser(newUser);

                // ĐẨY LÊN BACKEND NODEJS
                RetrofitClient.getMongoService().registerUser(newUser).enqueue(new retrofit2.Callback<User>() {
                    @Override
                    public void onResponse(retrofit2.Call<User> call, retrofit2.Response<User> response) {
                        if (response.isSuccessful()) {
                            android.util.Log.d("BACKEND", "User saved to MongoDB via NodeJS!");
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<User> call, Throwable t) {
                        android.util.Log.e("BACKEND", "Failed to connect to NodeJS: " + t.getMessage());
                    }
                });

                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại màn hình Login sau khi đăng ký thành công
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
