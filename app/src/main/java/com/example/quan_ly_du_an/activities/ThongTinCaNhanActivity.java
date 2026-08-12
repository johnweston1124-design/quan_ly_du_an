package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.databinding.ActivityThongTinCaNhanBinding;
import com.example.quan_ly_du_an.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThongTinCaNhanActivity extends AppCompatActivity {

    private ActivityThongTinCaNhanBinding binding;
    private AppDatabase database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private User currentUser;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThongTinCaNhanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getDatabase(this);

        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("USER_ID", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ThongTinCaNhanActivity.this, LoginActivity.class));
            finish();
            return;
        }

        loadUserProfile(currentUserId);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnChangeAvatar.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng thay đổi ảnh đại diện đang phát triển", Toast.LENGTH_SHORT).show();
        });

        binding.btnSaveProfile.setOnClickListener(v -> handleSaveProfile());
    }

    private void loadUserProfile(int userId) {
        executorService.execute(() -> {
            currentUser = database.userDao().getUserById(userId);

            runOnUiThread(() -> {
                if (userId == 999) {
                    binding.edtName.setText("Quản trị viên hệ thống");
                    binding.edtEmail.setText("admin@system.com");
                    binding.tvUserRoleBadge.setText("👑 Vai trò: Admin Tối Cao");
                    binding.tvUserIdDisplay.setText("Mã tài khoản: #999");
                } else if (currentUser != null) {
                    binding.edtName.setText(currentUser.getName() != null ? currentUser.getName() : "");
                    binding.edtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
                    binding.tvUserRoleBadge.setText("⚡ Vai trò: " + (currentUser.getRole() != null ? currentUser.getRole() : "Thành viên"));
                    binding.tvUserIdDisplay.setText("Mã tài khoản: #" + currentUser.getId());
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void handleSaveProfile() {
        String newName = binding.edtName.getText() != null ? binding.edtName.getText().toString().trim() : "";
        String newEmail = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(newName)) {
            binding.edtName.setError("Vui lòng nhập họ và tên");
            return;
        }

        if (TextUtils.isEmpty(newEmail)) {
            binding.edtEmail.setError("Vui lòng nhập email");
            return;
        }

        if (currentUser == null && currentUserId != 999) {
            Toast.makeText(this, "Không thể cập nhật tài khoản này!", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            if (currentUser != null) {
                currentUser.setName(newName);
                currentUser.setEmail(newEmail);
                database.userDao().updateUser(currentUser);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Cập nhật thông tin cá nhân thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
