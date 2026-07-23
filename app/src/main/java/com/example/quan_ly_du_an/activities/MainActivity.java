package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.adapter.TaskAdapter;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.databinding.ActivityMainBinding;
import com.example.quan_ly_du_an.model.Task;
import com.example.quan_ly_du_an.model.User;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private ActivityMainBinding binding;
    private TaskAdapter taskAdapter;
    private List<Task> currentFullList = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        db = AppDatabase.getDatabase(this);

        initTasksTab();
        initProfileTab();
        initProjectTab();
        loadUserProfile();
        setupBottomNavigation();
        
        // Mặc định hiện tab Home
        showTab(R.id.nav_home);
    }

    private void loadUserProfile() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("USER_ID", -1);

        if (userId != -1) {
            executorService.execute(() -> {
                User user = db.userDao().getUserById(userId);
                runOnUiThread(() -> {
                    if (user != null) {
                        binding.layoutProfile.tvProfileName.setText(user.getName());
                        binding.layoutProfile.tvProfileEmail.setText(user.getEmail());
                    } else if (userId == 999) {
                        binding.layoutProfile.tvProfileName.setText("Quản trị viên");
                        binding.layoutProfile.tvProfileEmail.setText("admin@system.com");
                    }
                });
            });
        }
    }

    private void initTasksTab() {
        binding.layoutTasks.rvTask.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(new ArrayList<>(), this);
        binding.layoutTasks.rvTask.setAdapter(taskAdapter);

        // Lấy dữ liệu thật từ Database
        db.taskDao().getAllTasks().observe(this, tasks -> {
            if (tasks != null) {
                currentFullList = tasks;
                taskAdapter.updateData(tasks);
                
                // Hiển thị màn hình trống nếu không có dữ liệu
                if (tasks.isEmpty()) {
                    binding.layoutTasks.layoutEmpty.setVisibility(View.VISIBLE);
                    binding.layoutTasks.rvTask.setVisibility(View.GONE);
                } else {
                    binding.layoutTasks.layoutEmpty.setVisibility(View.GONE);
                    binding.layoutTasks.rvTask.setVisibility(View.VISIBLE);
                }
            }
        });

        // Xử lý Tìm kiếm
        binding.layoutTasks.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                taskAdapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý Lọc Trạng thái
        binding.layoutTasks.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                taskAdapter.filterByStatus("Tất cả");
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) taskAdapter.filterByStatus(chip.getText().toString());
            }
        });

        // Xử lý Lọc Độ ưu tiên
        if (binding.layoutTasks.chipGroupPriorityFilter != null) {
            binding.layoutTasks.chipGroupPriorityFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    taskAdapter.filterByPriority("Tất cả ưu tiên");
                } else {
                    Chip chip = findViewById(checkedIds.get(0));
                    if (chip != null) taskAdapter.filterByPriority(chip.getText().toString());
                }
            });
        }

        // Nút Sắp xếp
        binding.layoutTasks.btnSort.setOnClickListener(v -> {
            List<Task> sortedList = new ArrayList<>(currentFullList);
            Collections.sort(sortedList, (t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
            taskAdapter.updateData(sortedList);
            Toast.makeText(this, "Đã sắp xếp theo tên", Toast.LENGTH_SHORT).show();
        });

        // Nút Thêm công việc
        binding.layoutTasks.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTaskActivity.class));
        });
    }

    private void initProfileTab() {
        binding.layoutProfile.btnThongTinCaNhan.setOnClickListener(v -> {
            startActivity(new Intent(this, ThongTinCaNhanActivity.class));
        });

        binding.layoutProfile.btnLichSu.setOnClickListener(v -> {
            startActivity(new Intent(this, LichSuActivity.class));
        });

        binding.layoutProfile.btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void initProjectTab() {
        binding.layoutProjects.fabAddProject.setOnClickListener(v -> {
            Toast.makeText(this, "Thêm dự án mới", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
    }

    private void showTab(int itemId) {
        binding.layoutHome.setVisibility(View.GONE);
        binding.layoutProjects.getRoot().setVisibility(View.GONE);
        binding.layoutTasks.getRoot().setVisibility(View.GONE);
        binding.layoutProfile.getRoot().setVisibility(View.GONE);

        if (itemId == R.id.nav_home) {
            binding.layoutHome.setVisibility(View.VISIBLE);
        } else if (itemId == R.id.nav_project) {
            binding.layoutProjects.getRoot().setVisibility(View.VISIBLE);
        } else if (itemId == R.id.nav_task) {
            binding.layoutTasks.getRoot().setVisibility(View.VISIBLE);
        } else if (itemId == R.id.nav_settings) {
            binding.layoutProfile.getRoot().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra("taskId", task.getTaskId());
        startActivity(intent);
    }

    private void handleLogout() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
