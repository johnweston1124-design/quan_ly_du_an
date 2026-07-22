package com.example.quan_ly_du_an.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quan_ly_du_an.databinding.ActivityLichSuBinding;

public class LichSuActivity extends AppCompatActivity {

    private ActivityLichSuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLichSuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Bạn có thể thiết lập Toolbar nếu muốn
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }
}
