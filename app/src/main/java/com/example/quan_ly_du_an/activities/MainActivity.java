package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. KIỂM TRA BẢO MẬT: Bắt buộc đăng nhập trước khi vào Trang chủ
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false);
        int currentUserId = sharedPref.getInt("USER_ID", -1);

        // Nếu chưa đăng nhập hoặc không tìm thấy USER_ID, ép chuyển về LoginActivity ngay lập tức
        if (!isLoggedIn || currentUserId == -1) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng MainActivity lại
            return;
        }

        // 2. Nếu đã đăng nhập thành công thì mới bơm Giao diện (Layout)
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Đăng xuất
        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleLogout());
        }

        // Mở Thông tin cá nhân
        View btnThongTin = findViewById(R.id.btnThongTinCaNhan);
        if (btnThongTin != null) {
            btnThongTin.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ThongTinCaNhanActivity.class);
                startActivity(intent);
            });
        }

        // Mở Lịch sử
        View btnLichSu = findViewById(R.id.btnLichSu);
        if (btnLichSu != null) {
            btnLichSu.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LichSuActivity.class);
                startActivity(intent);
            });
        }

        // Mở Cài đặt thông báo
        View btnThongBao = findViewById(R.id.btnCaiDatThongBao);
        if (btnThongBao != null) {
            btnThongBao.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Mở màn hình Cài đặt thông báo", Toast.LENGTH_SHORT).show();
            });
        }

        // Điều hướng Bottom Navigation (Tổng quan & Cá nhân)
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                binding.layoutHome.setVisibility(View.VISIBLE);
                binding.layoutProfile.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.nav_settings) {
                binding.layoutHome.setVisibility(View.GONE);
                binding.layoutProfile.setVisibility(View.VISIBLE);
                return true;
            }

            return false;
        });
    }

    private void handleLogout() {
        // Xóa thông tin lưu trong SharedPreferences khi đăng xuất
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.clear();
        editor.apply();

        // Chuyển về màn hình Login
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}