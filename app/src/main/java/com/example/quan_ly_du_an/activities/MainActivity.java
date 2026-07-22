package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private ActivityMainBinding binding;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                User user = AppDatabase.getDatabase(this).userDao().getUserById(userId);
                runOnUiThread(() -> {
                    if (user != null) {
                        binding.layoutProfile.tvProfileName.setText(user.getName());
                        binding.layoutProfile.tvProfileEmail.setText(user.getEmail());
                    } else if (userId == 999) {
                        // Trường hợp đăng nhập bằng tài khoản admin mặc định
                        binding.layoutProfile.tvProfileName.setText("Quản trị viên");
                        binding.layoutProfile.tvProfileEmail.setText("admin@system.com");
                    }
                });
            });
        }
    }

    private void initTasksTab() {
        binding.layoutTasks.rvTask.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        // Load demo data
        taskList.add(new Task(1, 1, 2, "Thiết kế Login", "Thiết kế giao diện đăng nhập", "Cao", "Đang làm", "20/07/2026"));
        taskList.add(new Task(2, 1, 3, "Thiết kế Database", "Tạo bảng Task", "Trung bình", "To Do", "22/07/2026"));
        
        taskAdapter = new TaskAdapter(taskList, this);
        binding.layoutTasks.rvTask.setAdapter(taskAdapter);

        binding.layoutTasks.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTaskActivity.class));
        });
    }

    private void initProfileTab() {
        // Các ID này nằm trong layout_ca_nhan.xml được include với id layoutProfile
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
        } else {
            binding.layoutHome.setVisibility(View.VISIBLE);
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
