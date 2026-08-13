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

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.adapter.TaskAdapter;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.databinding.ActivityMainBinding;
import com.example.quan_ly_du_an.feature_project.ui.ProjectSharedViewModel;
import com.example.quan_ly_du_an.feature_project.ui.home.HomeFragment;
import com.example.quan_ly_du_an.feature_project.ui.home.ProjectAdapter;
import com.example.quan_ly_du_an.model.ProjectWithRole;
import com.example.quan_ly_du_an.model.Task;
import com.example.quan_ly_du_an.model.User;
import com.example.quan_ly_du_an.worker.DeadlineWorker;
import com.google.android.material.chip.Chip;
import com.example.quan_ly_du_an.utils.ThemeAndLocaleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private ActivityMainBinding binding;
    private TaskAdapter taskAdapter;
    private ProjectSharedViewModel projectSharedViewModel;
    private List<Task> currentFullList = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AppDatabase db;
    private ProjectAdapter recentProjectAdapter;
    private TaskAdapter recentTaskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeAndLocaleManager.applyThemeAndLocale(this);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        initHomeTab();
        initTasksTab();
        initProfileTab();
        initProjectTab();
        loadUserProfile();
        prepopulateSampleData();
        setupBottomNavigation();
        setupProjectNavigationSync();

        PeriodicWorkRequest deadlineWorkRequest = new PeriodicWorkRequest.Builder(
                DeadlineWorker.class,
                2,
                TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DeadlineCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                deadlineWorkRequest
        );

        showTab(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTaskData();
    }

    private void refreshTaskData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("USER_ID", -1);
        if (userId != -1 && db != null) {
            db.taskDao().getAllTasksForUser(userId).observe(this, tasks -> {
                if (tasks != null) {
                    currentFullList = tasks;
                    if (taskAdapter != null) taskAdapter.updateData(tasks);
                    if (binding != null && binding.layoutTasks != null) {
                        if (tasks.isEmpty()) {
                            binding.layoutTasks.layoutEmpty.setVisibility(View.VISIBLE);
                            binding.layoutTasks.rvTask.setVisibility(View.GONE);
                        } else {
                            binding.layoutTasks.layoutEmpty.setVisibility(View.GONE);
                            binding.layoutTasks.rvTask.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            db.taskDao().getLatestTasksForUser(userId, 3).observe(this, tasks -> {
                if (tasks != null && recentTaskAdapter != null) {
                    recentTaskAdapter.updateData(tasks);
                }
            });
        }
    }

    private void initHomeTab() {
        recentProjectAdapter = new ProjectAdapter(projectWithRole -> {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_project);
            projectSharedViewModel.selectProject(projectWithRole);
        });
        recentProjectAdapter.setOnProjectLongClickListener(projectWithRole -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xóa dự án")
                    .setMessage("Bạn có chắc chắn muốn xóa dự án '" + projectWithRole.title + "' không? Hành động này sẽ xóa toàn bộ thành viên và công việc thuộc dự án này.")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        com.example.quan_ly_du_an.feature_project.ui.home.HomeViewModel homeViewModel =
                                new androidx.lifecycle.ViewModelProvider(this).get(com.example.quan_ly_du_an.feature_project.ui.home.HomeViewModel.class);
                        homeViewModel.deleteProject(projectWithRole.projectId, () -> {
                            Toast.makeText(this, "Đã xóa dự án '" + projectWithRole.title + "'", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
        binding.rvRecentProjects.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentProjects.setAdapter(recentProjectAdapter);

        recentTaskAdapter = new TaskAdapter(new ArrayList<>(), this);
        binding.rvRecentTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentTasks.setAdapter(recentTaskAdapter);

        binding.btnQuickAddProject.setOnClickListener(v -> {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_project);
            binding.bottomNavigation.post(() -> {
                androidx.fragment.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.layoutProjects);
                if (fragment instanceof com.example.quan_ly_du_an.feature_project.ui.home.HomeFragment) {
                    ((com.example.quan_ly_du_an.feature_project.ui.home.HomeFragment) fragment).showCreateProjectBottomSheet();
                }
            });
        });
        binding.btnQuickAddTask.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddTaskActivity.class)));
        binding.btnQuickTeam.setOnClickListener(v -> binding.bottomNavigation.setSelectedItemId(R.id.nav_team));
        binding.btnQuickReport.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LichSuActivity.class)));
        binding.btnSeeAllProjects.setOnClickListener(v -> binding.bottomNavigation.setSelectedItemId(R.id.nav_project));
        binding.btnSeeAllTasks.setOnClickListener(v -> binding.bottomNavigation.setSelectedItemId(R.id.nav_task));

        binding.btnNotification.setOnClickListener(v -> {
            executorService.execute(() -> {
                List<com.example.quan_ly_du_an.model.NotificationHistory> list = db.notificationDao().getAllSync();

                runOnUiThread(() -> {
                    if (list == null || list.isEmpty()) {
                        Toast.makeText(this, "Không có thông báo nào", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] items = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault());
                        String date = sdf.format(new java.util.Date(list.get(i).getTimestamp()));
                        items[i] = "[" + date + "] " + list.get(i).getTitle() + "\n" + list.get(i).getContent();
                    }

                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("🔔 Lịch sử thông báo")
                            .setItems(items, null)
                            .setPositiveButton("Đóng", null)
                            .setNeutralButton("Xóa tất cả", (dialog, which) -> {
                                executorService.execute(() -> {
                                    db.notificationDao().deleteAll();
                                    runOnUiThread(() -> Toast.makeText(this, "Đã xóa lịch sử", Toast.LENGTH_SHORT).show());
                                });
                            })
                            .show();
                });
            });
        });

        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("USER_ID", -1);

        executorService.execute(() -> {
            User currentUser = db.userDao().getUserById(userId);
            boolean isAdmin = (userId == 999) || (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole()));

            runOnUiThread(() -> {
                if (isAdmin) {
                    binding.layoutAdminPanel.setVisibility(View.VISIBLE);
                    binding.btnAdminManageUsers.setOnClickListener(v -> {
                        executorService.execute(() -> {
                            List<User> userList = db.userDao().getAllUsers();
                            StringBuilder sb = new StringBuilder("Danh sách tài khoản hệ thống (" + userList.size() + "):\n");
                            for (User u : userList) {
                                sb.append("• ").append(u.getName()).append(" (").append(u.getEmail()).append(") - ").append(u.getRole() != null ? u.getRole() : "Member").append("\n");
                            }
                            runOnUiThread(() -> {
                                new androidx.appcompat.app.AlertDialog.Builder(this)
                                        .setTitle("👥 Quản Lý Người Dùng")
                                        .setMessage(sb.toString())
                                        .setPositiveButton("Đóng", null)
                                        .show();
                            });
                        });
                    });

                    binding.btnAdminAllProjects.setOnClickListener(v -> {
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_project);
                    });

                    executorService.execute(() -> {
                        int totalUsers = db.userDao().getTotalUsersCount();
                        int totalProjects = db.projectDao().getTotalProjectsCount();
                        int totalTasks = db.taskDao().getTotalTasksCount();

                        runOnUiThread(() -> {
                            binding.tvAdminTotalUsers.setText(String.valueOf(totalUsers));
                            binding.tvAdminTotalProjects.setText(String.valueOf(totalProjects));
                            binding.tvAdminTotalTasks.setText(String.valueOf(totalTasks));
                        });
                    });

                    db.projectDao().getAllLatestProjectsForAdmin(3).observe(this, projects -> {
                        if (projects != null) {
                            recentProjectAdapter.setProjects(projects);
                            executorService.execute(() -> {
                                int count = db.projectDao().getTotalProjectsCount();
                                runOnUiThread(() -> {
                                    binding.tvCountProjects.setText(String.valueOf(count));
                                    binding.layoutProfile.tvProfileStatProjects.setText(String.valueOf(count));
                                });
                            });
                        }
                    });

                    db.taskDao().getLatestTasks(3).observe(this, tasks -> {
                        if (tasks != null) {
                            recentTaskAdapter.updateData(tasks);
                        }
                    });

                    db.taskDao().getAllTasks().observe(this, allTasks -> {
                        if (allTasks != null) {
                            int total = allTasks.size();
                            int completed = 0;
                            int pending = 0;
                            int highPriority = 0;

                            for (Task t : allTasks) {
                                if ("Done".equalsIgnoreCase(t.getStatus()) || "Hoàn thành".equalsIgnoreCase(t.getStatus())) {
                                    completed++;
                                } else {
                                    pending++;
                                }
                                if ("Cao".equalsIgnoreCase(t.getPriority()) || "High".equalsIgnoreCase(t.getPriority())) {
                                    highPriority++;
                                }
                            }

                            binding.tvCountTasks.setText(String.valueOf(pending));
                            binding.tvCountCompletedTasks.setText(String.valueOf(completed));
                            binding.tvCountHighTasks.setText(String.valueOf(highPriority));

                            binding.layoutProfile.tvProfileStatTasks.setText(String.valueOf(completed));

                            int percent = (total > 0) ? (completed * 100 / total) : 0;
                            binding.pbOverallProgress.setProgress(percent);
                            binding.tvProgressPercent.setText(percent + "%");
                        }
                    });
                } else {
                    binding.layoutAdminPanel.setVisibility(View.GONE);

                    db.taskDao().getLatestTasksForUser(userId, 3).observe(this, tasks -> {
                        if (tasks != null) {
                            recentTaskAdapter.updateData(tasks);
                        }
                    });

                    db.taskDao().getAllTasksForUser(userId).observe(this, allTasks -> {
                        if (allTasks != null) {
                            int total = allTasks.size();
                            int completed = 0;
                            int pending = 0;
                            int highPriority = 0;

                            for (Task t : allTasks) {
                                if ("Done".equalsIgnoreCase(t.getStatus()) || "Hoàn thành".equalsIgnoreCase(t.getStatus())) {
                                    completed++;
                                } else {
                                    pending++;
                                }
                                if ("Cao".equalsIgnoreCase(t.getPriority()) || "High".equalsIgnoreCase(t.getPriority())) {
                                    highPriority++;
                                }
                            }

                            binding.tvCountTasks.setText(String.valueOf(pending));
                            binding.tvCountCompletedTasks.setText(String.valueOf(completed));
                            binding.tvCountHighTasks.setText(String.valueOf(highPriority));

                            binding.layoutProfile.tvProfileStatTasks.setText(String.valueOf(completed));

                            int percent = (total > 0) ? (completed * 100 / total) : 0;
                            binding.pbOverallProgress.setProgress(percent);
                            binding.tvProgressPercent.setText(percent + "%");
                        }
                    });
                }

                com.example.quan_ly_du_an.feature_project.ui.home.HomeViewModel homeViewModel = new androidx.lifecycle.ViewModelProvider(this).get(com.example.quan_ly_du_an.feature_project.ui.home.HomeViewModel.class);
                homeViewModel.getUserProjects().observe(this, projects -> {
                    if (projects != null) {
                        java.util.List<com.example.quan_ly_du_an.model.ProjectWithRole> recentProjects = new java.util.ArrayList<>();
                        int limit = Math.min(3, projects.size());
                        for (int i = 0; i < limit; i++) {
                            recentProjects.add(projects.get(i));
                        }
                        recentProjectAdapter.setProjects(recentProjects);

                        int count = projects.size();
                        binding.tvCountProjects.setText(String.valueOf(count));
                        binding.layoutProfile.tvProfileStatProjects.setText(String.valueOf(count));
                    }
                });
            });
        });
    }

    private void setupProjectNavigationSync() {
        projectSharedViewModel = new ViewModelProvider(this).get(ProjectSharedViewModel.class);
        projectSharedViewModel.getNavigateToTeamRequest().observe(this, shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                binding.bottomNavigation.setSelectedItemId(R.id.nav_team);

                ProjectWithRole selected = projectSharedViewModel.getSelectedProject().getValue();
                if (selected != null) {
                    com.example.quan_ly_du_an.feature_project.ui.project.ProjectDetailFragment fragment = com.example.quan_ly_du_an.feature_project.ui.project.ProjectDetailFragment
                            .newInstance(
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
                    if (userId == 999) {
                        binding.tvHomeGreeting.setText("Xin chào, Admin Quản Trị!");
                        binding.layoutProfile.tvProfileName.setText("Quản trị viên hệ thống");
                        binding.layoutProfile.tvProfileEmail.setText("admin@system.com");
                        binding.layoutProfile.tvProfileRole.setText("👑 QUẢN TRỊ VIÊN TỐI CAO");
                    } else if (user != null) {
                        String name = user.getName();
                        binding.tvHomeGreeting.setText("Xin chào, " + name + "!");
                        binding.layoutProfile.tvProfileName.setText(name);
                        binding.layoutProfile.tvProfileEmail.setText(user.getEmail());
                        binding.layoutProfile.tvProfileRole.setText("⚡ Thành viên chính thức");
                    }
                });
            });
        }
    }

    private void prepopulateSampleData() {
        executorService.execute(() -> {
            User adminUser = db.userDao().getUserById(999);
            if (adminUser == null) {
                adminUser = new User("Quản trị viên", "admin", "123456");
                adminUser.setId(999);
                adminUser.setRole("ADMIN");
                db.userDao().insertUser(adminUser);
            }

            User userB = db.userDao().getUserByEmail("b@email.com");
            if (userB == null) {
                userB = new User("Nguyen Van B", "b@email.com", "123456");
                db.userDao().insertUser(userB);
                userB = db.userDao().getUserByEmail("b@email.com");
            }

            User jerry = db.userDao().getUserByEmail("jerry@gmail.com");
            if (jerry == null) {
                jerry = new User("Jerry Member", "jerry@gmail.com", "123456");
                jerry.setRole("MEMBER");
                db.userDao().insertUser(jerry);
            }

            User tom = db.userDao().getUserByEmail("tom@gmail.com");
            if (tom == null) {
                tom = new User("Tom Leader", "tom@gmail.com", "123456");
                tom.setRole("LEADER");
                db.userDao().insertUser(tom);
            }

            User qtv = db.userDao().getUserByEmail("qtv@gmail.com");
            if (qtv == null) {
                qtv = new User("Quản trị viên (QTV)", "qtv@gmail.com", "123456");
                qtv.setRole("ADMIN");
                db.userDao().insertUser(qtv);
            }

            final User finalUserB = userB;
            try (android.database.Cursor cursor = db.query("SELECT COUNT(*) FROM tasks", null)) {
                if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                    Task t1 = new Task(0, 1, finalUserB.getId(), "Thiết kế UI cho mobile",
                            "Tạo các màn hình Dashboard và Profile trên nền trắng", "Cao", "Doing", "30/07/2026");
                    Task t2 = new Task(0, 1, finalUserB.getId(), "Kết nối Database",
                            "Cấu hình SQLite Database cho dự án", "Trung bình", "Tự do", "05/08/2026");

                    db.taskDao().insert(t1);
                    db.taskDao().insert(t2);
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

        if (userId == 999) {
            db.taskDao().getAllTasks().observe(this, tasks -> {
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
        } else if (userId != -1) {
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
                if (userId != -1) {
                    executorService.execute(() -> {
                        List<Task> searchResults = db.taskDao().searchTasksForUserSync(userId, s.toString());

                        runOnUiThread(() -> {
                            taskAdapter.updateData(searchResults);
                        });
                    });
                }
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
                if (chip != null)
                    taskAdapter.filterByStatus(chip.getText().toString());
            }
        });

        if (binding.layoutTasks.chipGroupPriorityFilter != null) {
            binding.layoutTasks.chipGroupPriorityFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    taskAdapter.filterByPriority("Tất cả ưu tiên");
                } else {
                    Chip chip = findViewById(checkedIds.get(0));
                    if (chip != null)
                        taskAdapter.filterByPriority(chip.getText().toString());
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

        binding.layoutProfile.btnDoiMatKhau.setOnClickListener(v -> {
            startActivity(new Intent(this, DoiMatKhauActivity.class));
        });

        binding.layoutProfile.btnNhatKyDangNhap.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("🛡️ Nhật Ký Đăng Nhập")
                    .setMessage("Các thiết bị đăng nhập gần đây:\n\n" +
                            "• Android Phone - 22:30 hôm nay (Thiết bị này)\n" +
                            "• Chrome Web Client - 18:45 hôm qua\n" +
                            "• Trạng thái bảo mật: Đã xác thực 🟢")
                    .setPositiveButton("Đóng", null)
                    .show();
        });

        binding.layoutProfile.btnLichSu.setOnClickListener(v -> {
            startActivity(new Intent(this, LichSuActivity.class));
        });

        binding.layoutProfile.btnCaiDatThongBao.setOnClickListener(v -> {
            Toast.makeText(this, "Thông báo ứng dụng và nhắc nhở deadline đang BẬT", Toast.LENGTH_SHORT).show();
        });

        String currentLang = ThemeAndLocaleManager.getLanguageName(this);
        binding.layoutProfile.tvCurrentLanguage.setText(currentLang);

        boolean isDark = ThemeAndLocaleManager.isDarkMode(this);
        binding.layoutProfile.tvCurrentDarkMode.setText(isDark ? "Bật" : "Tắt");

        binding.layoutProfile.btnNgonNgu.setOnClickListener(v -> {
            String[] languages = {"🇻🇳 Tiếng Việt (Vietnamese)", "🇬🇧 English (Tiếng Anh)"};
            String saved = ThemeAndLocaleManager.getLanguageName(this);
            int checkedItem = saved.contains("English") ? 1 : 0;

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("🌐 Chọn Ngôn Ngữ Ứng Dụng")
                    .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                        String selectedName = (which == 1) ? "English" : "Tiếng Việt";
                        String selectedCode = (which == 1) ? "en" : "vi";
                        ThemeAndLocaleManager.setLanguage(this, selectedName, selectedCode);
                        binding.layoutProfile.tvCurrentLanguage.setText(selectedName);
                        Toast.makeText(this, (which == 1) ? "Switched language to English 🇬🇧" : "Đã chuyển sang Tiếng Việt 🇻🇳", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        recreate();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        binding.layoutProfile.btnDarkMode.setOnClickListener(v -> {
            boolean newDarkState = !ThemeAndLocaleManager.isDarkMode(this);
            ThemeAndLocaleManager.setDarkMode(this, newDarkState);
            binding.layoutProfile.tvCurrentDarkMode.setText(newDarkState ? "Bật" : "Tắt");
            Toast.makeText(this, newDarkState ? "Đã bật Chế độ tối 🌙" : "Đã chuyển sang Chế độ sáng ☀️", Toast.LENGTH_SHORT).show();
        });

        binding.layoutProfile.btnTroGiup.setOnClickListener(v -> {
            Toast.makeText(this, "Liên hệ hỗ trợ 24/7: hotro@quanlyduan.com", Toast.LENGTH_LONG).show();
        });

        binding.layoutProfile.btnDieuKhoan.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("📜 Điều Khoản & Chính Sách")
                    .setMessage("Chính sách sử dụng và bảo mật dữ liệu:\n\n" +
                            "1. Tất cả dữ liệu dự án được mã hóa bảo mật.\n" +
                            "2. Người dùng có toàn quyền quản lý dữ liệu cá nhân.\n" +
                            "3. Ứng dụng tuân thủ tiêu chuẩn an toàn thông tin ISO/IEC 27001.")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
        });

        binding.layoutProfile.btnThongTinApp.setOnClickListener(v -> {
            Toast.makeText(this, "Quản Lý Dự Án - Phiên bản v1.0.0 (Latest)", Toast.LENGTH_SHORT).show();
        });

        binding.layoutProfile.btnLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Đăng Xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> handleLogout())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
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
            getSupportFragmentManager().popBackStack(null,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layoutTeam,
                            new com.example.quan_ly_du_an.feature_project.ui.home.TeamProjectsFragment())
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