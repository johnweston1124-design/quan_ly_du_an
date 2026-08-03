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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.adapter.TaskAdapter;
import com.example.quan_ly_du_an.api.MongoApiService;
import com.example.quan_ly_du_an.api.RetrofitClient;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.databinding.ActivityMainBinding;
import com.example.quan_ly_du_an.feature_project.ui.ProjectSharedViewModel;
import com.example.quan_ly_du_an.feature_project.ui.home.HomeFragment;
import com.example.quan_ly_du_an.feature_project.ui.home.ProjectAdapter;
import com.example.quan_ly_du_an.model.ProjectWithRole;
import com.example.quan_ly_du_an.model.Task;
import com.example.quan_ly_du_an.model.User;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private ActivityMainBinding binding;
    private TaskAdapter taskAdapter;
    private ProjectSharedViewModel projectSharedViewModel;
    private List<Task> currentFullList = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AppDatabase db;

    // Adapters cho trang chủ
    private ProjectAdapter recentProjectAdapter;
    private TaskAdapter recentTaskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getDatabase(getApplicationContext());

        initHomeTab();
        initTasksTab();
        initProfileTab();
        initProjectTab();
        loadUserProfile();
        prepopulateSampleData();
        setupBottomNavigation();
        setupProjectNavigationSync();

        // Mặc định hiện tab Home
        showTab(R.id.nav_home);
    }

    private void initHomeTab() {
        // 1. Setup RecyclerView cho Dự án gần đây
        recentProjectAdapter = new ProjectAdapter(projectWithRole -> {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_project);
            projectSharedViewModel.selectProject(projectWithRole);
        });
        binding.rvRecentProjects.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentProjects.setAdapter(recentProjectAdapter);

        // 2. Setup RecyclerView cho Công việc mới nhất
        recentTaskAdapter = new TaskAdapter(new ArrayList<>(), this);
        binding.rvRecentTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentTasks.setAdapter(recentTaskAdapter);

        // 3. Quan sát dữ liệu
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("USER_ID", -1);

        if (userId != -1) {
            // Lấy 3 dự án mới nhất
            db.projectDao().getLatestProjectsForUser((long) userId, 3).observe(this, projects -> {
                if (projects != null) {
                    recentProjectAdapter.setProjects(projects);
                    // Cập nhật con số thống kê dự án
                    executorService.execute(() -> {
                        int count = db.projectDao().getProjectsForUser((long) userId).size();
                        runOnUiThread(() -> binding.tvCountProjects.setText("Dự án đang làm: " + count));
                    });
                }
            });

            // Lấy 3 công việc mới nhất của User này
            db.taskDao().getLatestTasksForUser(userId, 3).observe(this, tasks -> {
                if (tasks != null) {
                    recentTaskAdapter.updateData(tasks);
                }
            });

            // Cập nhật tổng số công việc của User này
            db.taskDao().getAllTasksForUser(userId).observe(this, allTasks -> {
                if (allTasks != null) {
                    binding.tvCountTasks.setText("Công việc hôm nay: " + allTasks.size());
                }
            });
        }
    }

    private void setupProjectNavigationSync() {
        projectSharedViewModel = new ViewModelProvider(this).get(ProjectSharedViewModel.class);
        projectSharedViewModel.getNavigateToTeamRequest().observe(this, shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                // Chuyển sang Tab Đội ngũ (nav_team)
                binding.bottomNavigation.setSelectedItemId(R.id.nav_team);

                // Mở trực tiếp ProjectDetailFragment
                ProjectWithRole selected = projectSharedViewModel.getSelectedProject().getValue();
                if (selected != null) {
                    com.example.quan_ly_du_an.feature_project.ui.project.ProjectDetailFragment fragment =
                            com.example.quan_ly_du_an.feature_project.ui.project.ProjectDetailFragment.newInstance(
                                    selected.projectId,
                                    selected.title,
                                    selected.role);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.layoutTeam, fragment)
                            .addToBackStack(null)
                            .commit();
                }

                projectSharedViewModel.completeNavigation();
            }
        });
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

    private void prepopulateSampleData() {
        executorService.execute(() -> {
            // 1. Tạo Nguyen Van B
            User userB = db.userDao().getUserByEmail("b@email.com");
            if (userB == null) {
                userB = new User("Nguyen Van B", "b@email.com", "123456");
                db.userDao().insertUser(userB);
                userB = db.userDao().getUserByEmail("b@email.com");

                // PUSH TO BACKEND NODEJS
                RetrofitClient.getMongoService().registerUser(userB).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            android.util.Log.d("BACKEND", "User B pushed successfully!");
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        android.util.Log.e("BACKEND", "Network error pushing user: " + t.getMessage());
                    }
                });
            }

            final User finalUserB = userB;
            // 2. Tạo Task mẫu
            try (android.database.Cursor cursor = db.query("SELECT COUNT(*) FROM tasks", null)) {
                if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                    Task t1 = new Task(0, 1, finalUserB.getId(), "Thiết kế UI cho mobile", "Tạo các màn hình Dashboard và Profile trên nền trắng", "Cao", "Doing", "30/07/2026");
                    Task t2 = new Task(0, 1, finalUserB.getId(), "Kết nối MongoDB", "Cấu hình Retrofit và Data API cho dự án", "Trung bình", "Tự do", "05/08/2026");

                    db.taskDao().insert(t1);
                    db.taskDao().insert(t2);

                    // PUSH TASKS TO BACKEND NODEJS
                    MongoApiService service = RetrofitClient.getMongoService();
                    service.createTask(t1).enqueue(new Callback<Task>() {
                        @Override
                        public void onResponse(Call<Task> call, Response<Task> response) {
                            android.util.Log.d("BACKEND", "Task 1 status: " + response.code());
                        }

                        @Override
                        public void onFailure(Call<Task> call, Throwable t) {
                        }
                    });
                    service.createTask(t2).enqueue(new Callback<Task>() {
                        @Override
                        public void onResponse(Call<Task> call, Response<Task> response) {
                            android.util.Log.d("BACKEND", "Task 2 status: " + response.code());
                        }

                        @Override
                        public void onFailure(Call<Task> call, Throwable t) {
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initTasksTab() {
        binding.layoutTasks.rvTask.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(new ArrayList<>(), this);
        binding.layoutTasks.rvTask.setAdapter(taskAdapter);

        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("USER_ID", -1);

        if (userId != -1) {
            db.taskDao().getAllTasksForUser(userId).observe(this, tasks -> {
                if (tasks != null) {
                    currentFullList = tasks;
                    taskAdapter.updateData(tasks);

                    if (tasks.isEmpty()) {
                        binding.layoutTasks.layoutEmpty.setVisibility(View.VISIBLE);
                        binding.layoutTasks.rvTask.setVisibility(View.GONE);
                    } else {
                        binding.layoutTasks.layoutEmpty.setVisibility(View.GONE);
                        binding.layoutTasks.rvTask.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        binding.layoutTasks.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                taskAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.layoutTasks.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                taskAdapter.filterByStatus("Tất cả");
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) taskAdapter.filterByStatus(chip.getText().toString());
            }
        });

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

        binding.layoutTasks.btnSort.setOnClickListener(v -> {
            List<Task> sortedList = new ArrayList<>(currentFullList);
            sortedList.sort((t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
            taskAdapter.updateData(sortedList);
            Toast.makeText(this, "Đã sắp xếp theo tên", Toast.LENGTH_SHORT).show();
        });

        binding.layoutTasks.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutProjects, new HomeFragment())
                .commit();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
    }

    private void showTab(int itemId) {
        binding.layoutHome.setVisibility(View.GONE);
        binding.layoutProjects.setVisibility(View.GONE);
        binding.layoutTasks.getRoot().setVisibility(View.GONE);
        binding.layoutProfile.getRoot().setVisibility(View.GONE);
        binding.layoutTeam.setVisibility(View.GONE);

        if (itemId == R.id.nav_home) {
            binding.layoutHome.setVisibility(View.VISIBLE);
        } else if (itemId == R.id.nav_project) {
            binding.layoutProjects.setVisibility(View.VISIBLE);
        } else if (itemId == R.id.nav_task) {
            binding.layoutTasks.getRoot().setVisibility(View.VISIBLE);
        } else if (itemId == R.id.nav_team) {
            binding.layoutTeam.setVisibility(View.VISIBLE);
            getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layoutTeam, new com.example.quan_ly_du_an.feature_project.ui.home.TeamProjectsFragment())
                    .commit();
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
