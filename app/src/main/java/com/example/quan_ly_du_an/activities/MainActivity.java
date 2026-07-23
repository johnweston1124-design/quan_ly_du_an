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

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleLogout());
        }

        View btnThongTin = findViewById(R.id.btnThongTinCaNhan);
        if (btnThongTin != null) {
            btnThongTin.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ThongTinCaNhanActivity.class);
                startActivity(intent);
            });
        }

        View btnMucTieu = findViewById(R.id.btnLichSu);
        if (btnMucTieu != null) {
            btnMucTieu.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Mở màn hình Chỉnh sửa mục tiêu", Toast.LENGTH_SHORT).show();
            });
        }

        View btnThongBao = findViewById(R.id.btnCaiDatThongBao);
        if (btnThongBao != null) {
            btnThongBao.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Mở màn hình Cài đặt thông báo", Toast.LENGTH_SHORT).show();
            });
        }
        View btnLichSu = findViewById(R.id.btnLichSu); // Kiểm tra lại đúng ID nút Lịch sử trong XML của bạn
        if (btnLichSu != null) {
            btnLichSu.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LichSuActivity.class);
                startActivity(intent);
            });
        }

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
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.clear();
        editor.apply();

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}