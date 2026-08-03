package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.quan_ly_du_an.adapter.HistoryAdapter;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.databinding.ActivityLichSuBinding;
import java.util.ArrayList;

public class LichSuActivity extends AppCompatActivity {

    private ActivityLichSuBinding binding;
    private HistoryAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLichSuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getDatabase(this);
        setupRecyclerView();
        loadHistory();

        binding.btnBack.setOnClickListener(v -> finish());

        binding.edtSearchHistory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }

    private void loadHistory() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("USER_ID", -1);

        if (userId != -1) {
            db.historyDao().getAllHistoryForUser(userId).observe(this, historyList -> {
                if (historyList != null && !historyList.isEmpty()) {
                    adapter.setHistoryList(historyList);
                    binding.tvEmpty.setVisibility(View.GONE);
                    binding.rvHistory.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    binding.rvHistory.setVisibility(View.GONE);
                }
            });
        }
    }
}
