package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.model.User;
import com.example.quan_ly_du_an.databinding.ActivityThongTinCaNhanBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThongTinCaNhanActivity extends AppCompatActivity {

    private ActivityThongTinCaNhanBinding binding;
    private AppDatabase database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThongTinCaNhanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getDatabase(this);

        // 1. Lấy USER_ID của tài khoản đã đăng nhập từ SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int currentUserId = sharedPref.getInt("USER_ID", -1);

        // Kiểm tra bảo mật: Nếu chưa đăng nhập (ID = -1) thì ép văng ra màn hình Login
        if (currentUserId == -1) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ThongTinCaNhanActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // 2. Load thông tin tài khoản từ Database dựa trên USER_ID
        loadUserProfile(currentUserId);

        // 3. Xử lý nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserProfile(int userId) {
        executorService.execute(() -> {
            // Truy vấn lấy User từ Database ngầm
            User user = database.userDao().getUserById(userId);

            runOnUiThread(() -> {
                if (user != null) {
                    // Đổ dữ liệu của chính tài khoản đó lên giao diện
                    binding.tvName.setText(user.getName() != null ? user.getName() : "Người dùng");
                    binding.tvEmail.setText(user.getEmail());
                    binding.tvRole.setText("Vai trò: " + user.getRole());
                } else if (userId == 999) {
                    // Xử lý tài khoản admin mặc định
                    binding.tvName.setText("Quản trị viên");
                    binding.tvEmail.setText("admin@system.com");
                    binding.tvRole.setText("Vai trò: Admin");
                } else {
                    Toast.makeText(this, "Lỗi tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
