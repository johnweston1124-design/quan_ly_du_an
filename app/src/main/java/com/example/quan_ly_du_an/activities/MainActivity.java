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
        // 1. Setup RecyclerView cho Dự án gần đây
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

        // 2. Setup RecyclerView cho Công việc mới nhất
        recentTaskAdapter = new TaskAdapter(new ArrayList<>(), this);
        binding.rvRecentTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentTasks.setAdapter(recentTaskAdapter);

        // 3. Quick Action click listeners
        binding.btnQuickAddProject.setOnClickListener(v -> binding.bottomNavigation.setSelectedItemId(R.id.nav_project));
        binding.btnQuickAddTask.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddTaskActivity.class)));
        binding.btnQuickTeam.setOnClickListener(v -> binding.bottomNavigation.setSelectedItemId(R.id.nav_team));
        binding.btnQuickReport.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LichSuActivity.class)));
        binding.btnSeeAllProjects.setOnClickListener(v -> binding.bottomNavigation.setSelectedItemId(R.id.nav_project));
        binding.btnSeeAllTasks.setOnClickListener(v -> binding.bottomNavigation.setSelectedItemId(R.id.nav_task));
        binding.btnNotification.setOnClickListener(v -> Toast.makeText(this, "Không có thông báo mới nào", Toast.LENGTH_SHORT).show());

        // 4. Quan sát dữ liệu
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

                    // Tải thống kê toàn hệ thống cho Admin
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

                    // Admin: Lấy 3 dự án mới nhất của TOÀN BỘ NHÂN VIÊN
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

                    // Admin: Lấy 3 công việc mới nhất của TOÀN BỘ NHÂN VIÊN
                    db.taskDao().getLatestTasks(3).observe(this, tasks -> {
                        if (tasks != null) {
                            recentTaskAdapter.updateData(tasks);
                        }
                    });

                    // Admin: Cập nhật tổng số công việc của TOÀN BỘ NHÂN VIÊN
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
                            binding.layoutProfile.tvProfileStatEfficiency.setText(percent + "%");
                        }
                    });
                } else {
                    binding.layoutAdminPanel.setVisibility(View.GONE);

                    // User thường: Lấy 3 công việc mới nhất của chính User đó
                    db.taskDao().getLatestTasksForUser(userId, 3).observe(this, tasks -> {
                        if (tasks != null) {
                            recentTaskAdapter.updateData(tasks);
                        }
                    });

                    // User thường: Cập nhật tổng số công việc cá nhân
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
                            binding.layoutProfile.tvProfileStatEfficiency.setText(percent + "%");
                        }
                    });
                }

                // Cả Admin và User thường: Quan sát HomeViewModel để đồng bộ dữ liệu dự án giữa các tab
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
                // Chuyển sang Tab Đội ngũ (nav_team)
                binding.bottomNavigation.setSelectedItemId(R.id.nav_team);

                // Mở trực tiếp ProjectDetailFragment
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
                    if (user != null) {
                        String name = user.getName();
                        binding.tvHomeGreeting.setText("Xin chào, " + name + "!");
                        binding.layoutProfile.tvProfileName.setText(name);
                        binding.layoutProfile.tvProfileEmail.setText(user.getEmail());
                        binding.layoutProfile.tvProfileRole.setText("⚡ Thành viên chính thức");
                    } else if (userId == 999) {
                        binding.tvHomeGreeting.setText("Xin chào, Quản trị viên!");
                        binding.layoutProfile.tvProfileName.setText("Quản trị viên hệ thống");
                        binding.layoutProfile.tvProfileEmail.setText("admin@system.com");
                        binding.layoutProfile.tvProfileRole.setText("QUẢN TRỊ VIÊN TỐI CAO");
                    }
                });
            });
        }
    }

    private void prepopulateSampleData() {
        executorService.execute(() -> {
            // 0. Tạo Admin account
            User adminUser = db.userDao().getUserById(999);
            if (adminUser == null) {
                adminUser = new User("Quản trị viên", "admin", "123456");
                adminUser.setId(999);
                adminUser.setRole("ADMIN");
                db.userDao().insertUser(adminUser);
            }

            // 1. Tạo Nguyen Van B
            User userB = db.userDao().getUserByEmail("b@email.com");
            if (userB == null) {
                userB = new User("Nguyen Van B", "b@email.com", "123456");
                db.userDao().insertUser(userB);
                userB = db.userDao().getUserByEmail("b@email.com");
            }

            // 2. Tạo Member Jerry
            User jerry = db.userDao().getUserByEmail("jerry@gmail.com");
            if (jerry == null) {
                jerry = new User("Jerry Member", "jerry@gmail.com", "123456");
                jerry.setRole("MEMBER");
                db.userDao().insertUser(jerry);
            }

            // 3. Tạo Leader Tom
            User tom = db.userDao().getUserByEmail("tom@gmail.com");
            if (tom == null) {
                tom = new User("Tom Leader", "tom@gmail.com", "123456");
                tom.setRole("LEADER");
                db.userDao().insertUser(tom);
            }

            // 4. Tạo Admin QTV
            User qtv = db.userDao().getUserByEmail("qtv@gmail.com");
            if (qtv == null) {
                qtv = new User("Quản trị viên (QTV)", "qtv@gmail.com", "123456");
                qtv.setRole("ADMIN");
                db.userDao().insertUser(qtv);
            }

            final User finalUserB = userB;
            // 2. Tạo Task mẫu
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
            // Admin: Quan sát toàn bộ công việc của nhân viên trong hệ thống
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
            // User thường: Chỉ quan sát công việc được phân công cho cá nhân
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
        binding.layoutProfile.btnThanhTich.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("🏆 Bảng Thành Tích & Huy Hiệu")
                    .setMessage("Các danh hiệu bạn đã đạt được:\n\n" +
                            "• ⚡ Chuyên gia đúng hạn (Tích lũy >10 task xong)\n" +
                            "• 🏆 Thành viên tích cực (Điểm thưởng: 850 pts)\n" +
                            "• 🎯 Tỷ lệ hoàn thành xuất sắc (100%)")
                    .setPositiveButton("Tuyệt vời", null)
                    .show();
        });

        binding.layoutProfile.btnBaoCao.setOnClickListener(v -> {
            startActivity(new Intent(this, LichSuActivity.class));
        });

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

        SharedPreferences langPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String currentLang = langPref.getString("APP_LANGUAGE", "Tiếng Việt");
        binding.layoutProfile.tvCurrentLanguage.setText(currentLang);

        binding.layoutProfile.btnNgonNgu.setOnClickListener(v -> {
            String[] languages = {"🇻🇳 Tiếng Việt (Vietnamese)", "🇬🇧 English (Tiếng Anh)"};
            String saved = langPref.getString("APP_LANGUAGE", "Tiếng Việt");
            int checkedItem = saved.contains("English") ? 1 : 0;

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("🌐 Chọn Ngôn Ngữ Ứng Dụng")
                    .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                        String selected = (which == 1) ? "English" : "Tiếng Việt";
                        langPref.edit().putString("APP_LANGUAGE", selected).apply();
                        binding.layoutProfile.tvCurrentLanguage.setText(selected);
                        Toast.makeText(this, (which == 1) ? "Switched language to English 🇬🇧" : "Đã chuyển sang Tiếng Việt 🇻🇳", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        binding.layoutProfile.btnDarkMode.setOnClickListener(v -> {
            Toast.makeText(this, "Giao diện đang ở chế độ sáng (Light Mode)", Toast.LENGTH_SHORT).show();
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
