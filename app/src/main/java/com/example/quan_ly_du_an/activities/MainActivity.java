package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.adapter.TaskAdapter;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.model.Task;
import com.example.quan_ly_du_an.model.User;
import com.example.quan_ly_du_an.databinding.ActivityMainBinding;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private ActivityMainBinding binding;
    private AppDatabase database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private TaskAdapter taskAdapter;
    private List<Task> fullTaskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getDatabase(this);

        // Load profile data if logged in
        loadUserProfile();

        // Initialize Task logic
        initTaskSection();

        binding.btnLogout.setOnClickListener(v -> handleLogout());

        binding.btnThongTinCaNhan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThongTinCaNhanActivity.class);
            startActivity(intent);
        });

        binding.btnMucTieu.setOnClickListener(v -> {
            // Có thể mở LichSuActivity hoặc một màn hình mục tiêu mới
            Intent intent = new Intent(MainActivity.this, LichSuActivity.class);
            startActivity(intent);
        });

        binding.btnCaiDatThongBao.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Mở màn hình Cài đặt thông báo", Toast.LENGTH_SHORT).show();
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                binding.layoutHome.setVisibility(View.VISIBLE);
                binding.layoutTask.setVisibility(View.GONE);
                binding.layoutProfile.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.nav_task) {
                binding.layoutHome.setVisibility(View.GONE);
                binding.layoutTask.setVisibility(View.VISIBLE);
                binding.layoutProfile.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.nav_settings) {
                binding.layoutHome.setVisibility(View.GONE);
                binding.layoutTask.setVisibility(View.GONE);
                binding.layoutProfile.setVisibility(View.VISIBLE);
                return true;
            }

            return false;
        });
    }

    private void initTaskSection() {
        binding.rvTask.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(new ArrayList<>(), this);
        binding.rvTask.setAdapter(taskAdapter);

        database.taskDao().getAllTasks().observe(this, tasks -> {
            if (tasks != null) {
                fullTaskList = tasks;
                taskAdapter.updateData(tasks);
                updateTaskEmptyState();
            }
        });

        binding.fabAddTask.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddTaskActivity.class));
        });

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                taskAdapter.filter(s.toString());
                updateTaskEmptyState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                taskAdapter.filterByStatus("Tất cả");
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) taskAdapter.filterByStatus(chip.getText().toString());
            }
            updateTaskEmptyState();
        });

        binding.chipGroupPriority.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                taskAdapter.filterByPriority("Tất cả ưu tiên");
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) taskAdapter.filterByPriority(chip.getText().toString());
            }
            updateTaskEmptyState();
        });

        binding.btnSort.setOnClickListener(v -> {
            List<Task> sortedList = new ArrayList<>(fullTaskList);
            sortedList.sort((t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
            taskAdapter.updateData(sortedList);
            Toast.makeText(this, "Đã sắp xếp theo tên", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateTaskEmptyState() {
        if (taskAdapter.getItemCount() == 0) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.rvTask.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.rvTask.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
        intent.putExtra("taskId", task.getTaskId());
        startActivity(intent);
    }

    private void handleLogout() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.clear();
        editor.apply();

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private void loadUserProfile() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("USER_ID", -1);

        if (userId != -1) {
            executorService.execute(() -> {
                User user = database.userDao().getUserById(userId);
                if (user != null) {
                    runOnUiThread(() -> {
                        binding.tvProfileName.setText(user.getName());
                        binding.tvProfileEmail.setText(user.getEmail());
                    });
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}