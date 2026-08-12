package com.example.quan_ly_du_an.feature_project.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.database.ProjectDao;
import com.example.quan_ly_du_an.database.ProjectMemberDao;
import com.example.quan_ly_du_an.database.UserDao;
import com.example.quan_ly_du_an.model.History;
import com.example.quan_ly_du_an.model.MemberWithRole;
import com.example.quan_ly_du_an.model.Project;
import com.example.quan_ly_du_an.model.ProjectMember;
import com.example.quan_ly_du_an.model.ProjectWithRole;
import com.example.quan_ly_du_an.model.User;
import com.example.quan_ly_du_an.utils.SessionManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectRepository {
    private final AppDatabase db;
    private final ProjectDao projectDao;
    private final ProjectMemberDao projectMemberDao;
    private final UserDao userDao;
    private final ExecutorService executorService;

    public ProjectRepository(Application application) {
        this.db = AppDatabase.getDatabase(application);
        this.projectDao = db.projectDao();
        this.projectMemberDao = db.projectMemberDao();
        this.userDao = db.userDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    // --- API CHO PHÂN CÔNG 2 (DỰ ÁN) ---

    public LiveData<List<ProjectWithRole>> getProjectsForUser(long userId) {
        MutableLiveData<List<ProjectWithRole>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            User user = userDao.getUserById((int) userId);
            boolean isAdmin = (userId == 999) || (user != null && "ADMIN".equalsIgnoreCase(user.getRole()));
            List<ProjectWithRole> data;
            if (isAdmin) {
                data = projectDao.getAllProjectsForAdmin();
            } else {
                data = projectDao.getProjectsForUser(userId);
            }
            liveData.postValue(data);
        });
        return liveData;
    }

    public LiveData<List<ProjectWithRole>> searchProjects(long userId, String keyword) {
        MutableLiveData<List<ProjectWithRole>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            String wildCard = "%" + keyword + "%";
            List<ProjectWithRole> data = projectDao.searchProjects(userId, wildCard);
            liveData.postValue(data);
        });
        return liveData;
    }

    public void createNewProject(String title, String desc, String startDate, String endDate, String status, int expectedMembers, long creatorUserId, Runnable onSuccess, OnError onError) {
        if (title == null || title.trim().isEmpty()) {
            if (onError != null) onError.onError("Tên dự án không được để trống");
            return;
        }
        if (startDate != null && endDate != null && !startDate.trim().isEmpty() && !endDate.trim().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date start = sdf.parse(startDate);
                java.util.Date end = sdf.parse(endDate);
                if (end.before(start)) {
                    if (onError != null) onError.onError("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
                    return;
                }
            } catch (Exception e) {
                if (onError != null) onError.onError("Định dạng ngày không hợp lệ. Vui lòng chọn lại!");
                return;
            }
        }
        executorService.execute(() -> {
            try {
                // Đảm bảo user có tồn tại trong CSDL để tránh lỗi FOREIGN KEY constraint
                if (userDao.getUserById((int) creatorUserId) == null) {
                    com.example.quan_ly_du_an.model.User dummyUser;
                    if (creatorUserId == 999) {
                        dummyUser = new com.example.quan_ly_du_an.model.User("Quản trị viên hệ thống", "admin@system.com", "123456");
                        dummyUser.setRole("Admin");
                    } else {
                        dummyUser = new com.example.quan_ly_du_an.model.User("Demo User", "demo" + creatorUserId + "@example.com", "123456");
                    }
                    dummyUser.setId((int) creatorUserId);
                    userDao.insertUser(dummyUser);
                }

                Project project = new Project(title, desc, status != null ? status : "Đang thực hiện", 
                                              startDate != null ? startDate : "", endDate != null ? endDate : "", expectedMembers);
                long id = projectDao.insertProject(project);
                if (id != -1) {
                    ProjectMember adminMember = new ProjectMember(id, creatorUserId, "ADMIN");
                    projectMemberDao.insertProjectMember(adminMember);
                    
                    // LƯU VÀO LỊCH SỬ
                    java.text.SimpleDateFormat timeSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
                    String currentTime = timeSdf.format(new java.util.Date());
                    History history = new History("Tạo dự án mới", "Đã tạo dự án: " + title, currentTime, (int) creatorUserId);
                    db.historyDao().insert(history);

                    if (onSuccess != null) onSuccess.run();
                } else if (onError != null) {
                    onError.onError("Lỗi khi tạo dự án trong Database");
                }
            } catch (Exception e) {
                if (onError != null) {
                    onError.onError("Lỗi CSDL: " + e.getMessage());
                }
                e.printStackTrace();
            }
        });
    }

    public void deleteProject(long projectId, long currentUserId, Runnable onSuccess, OnError onError) {
        executorService.execute(() -> {
            com.example.quan_ly_du_an.model.User user = userDao.getUserById((int) currentUserId);
            boolean isSystemAdmin = (currentUserId == 999) || (user != null && "ADMIN".equalsIgnoreCase(user.getRole()));

            String role = projectMemberDao.getUserRoleSync(projectId, currentUserId);
            boolean isProjectAdmin = "ADMIN".equalsIgnoreCase(role);

            if (!isSystemAdmin && !isProjectAdmin) {
                if (onError != null) onError.onError("Chỉ ADMIN mới có quyền xóa dự án!");
                return;
            }
            
            int rowsDeleted = projectDao.deleteProject(projectId);
            if (rowsDeleted > 0) {
                try {
                    db.execQuery("DELETE FROM tasks WHERE projectId = ?", new Object[]{projectId});
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (onSuccess != null) onSuccess.run();
            } else {
                if (onError != null) onError.onError("Xóa dự án thất bại");
            }
        });
    }

    // --- API CHO PHÂN CÔNG 3 (MEMBER) ---

    public LiveData<List<MemberWithRole>> getMembersByProjectId(long projectId) {
        MutableLiveData<List<MemberWithRole>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<MemberWithRole> data = projectMemberDao.getMembersByProjectId(projectId);
            if (data != null) {
                for (MemberWithRole member : data) {
                    int count = 0;
                    try (android.database.Cursor cursor = db.query(
                            "SELECT COUNT(*) FROM tasks WHERE projectId = ? AND assignedUserId = ?",
                            new Object[]{projectId, member.userId})) {
                        if (cursor != null && cursor.moveToFirst()) {
                            count = cursor.getInt(0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    member.taskCount = count;
                }
            }
            liveData.postValue(data);
        });
        return liveData;
    }

    public void addMemberByEmail(long projectId, String email, String role, Runnable onSuccess, OnError onError) {
        if (email == null || !email.contains("@")) {
            if (onError != null) onError.onError("Email không hợp lệ");
            return;
        }
        executorService.execute(() -> {
            if (projectMemberDao.isMemberAlreadyInProject(projectId, email)) {
                if (onError != null) onError.onError("Thành viên này đã có trong dự án");
                return;
            }

            User user = userDao.getUserByEmail(email);
            if (user == null) {
                if (onError != null) onError.onError("Tài khoản email này không tồn tại trong hệ thống");
                return;
            }
            
            ProjectMember member = new ProjectMember(projectId, user.getId(), role);
            long result = projectMemberDao.insertProjectMember(member);

            if (result != -1) {
                if (onSuccess != null) onSuccess.run();
            } else if (onError != null) {
                onError.onError("Lỗi khi thêm thành viên");
            }
        });
    }

    public void removeMember(long projectId, long userId, Runnable onSuccess, OnError onError) {
        executorService.execute(() -> {
            int count = 0;
            try (android.database.Cursor cursor = db.query(
                    "SELECT COUNT(*) FROM tasks WHERE projectId = ? AND assignedUserId = ? AND status != 'Hoàn thành'",
                    new Object[]{projectId, userId})) {
                if (cursor != null && cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (count > 0) {
                if (onError != null) onError.onError("Không thể xóa thành viên đang có công việc chưa hoàn thành!");
                return;
            }

            projectMemberDao.removeMember(projectId, userId);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public String getUserRoleSync(long projectId, long userId) {
        com.example.quan_ly_du_an.model.User user = userDao.getUserById((int) userId);
        if (userId == 999 || (user != null && "ADMIN".equalsIgnoreCase(user.getRole()))) {
            return "ADMIN";
        }
        String role = projectMemberDao.getUserRoleSync(projectId, userId);
        return role != null ? role : "MEMBER";
    }

    public interface OnError {
        void onError(String message);
    }
}
