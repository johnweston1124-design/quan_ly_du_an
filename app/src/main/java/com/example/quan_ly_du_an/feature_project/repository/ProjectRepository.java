package com.example.quan_ly_du_an.feature_project.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.feature_project.data.model.MemberWithRole;
import com.example.quan_ly_du_an.feature_project.data.model.ProjectWithRole;
import com.example.quan_ly_du_an.feature_project.data.sqlite.AppDbHelper;
import com.example.quan_ly_du_an.feature_project.data.sqlite.MemberDaoSqlite;
import com.example.quan_ly_du_an.model.Task;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectRepository {
    private final MemberDaoSqlite memberDao;
    private final AppDatabase roomDb;
    private final ExecutorService executorService;

    public ProjectRepository(Application application) {
        AppDbHelper dbHelper = AppDbHelper.getInstance(application);
        this.memberDao = new MemberDaoSqlite(dbHelper);
        this.roomDb = AppDatabase.getDatabase(application);
        this.executorService = Executors.newFixedThreadPool(4);
    }

    // --- API CHO PHÂN CÔNG 2 (DỰ ÁN) ---

    public LiveData<List<ProjectWithRole>> getProjectsForUser(long userId) {
        MutableLiveData<List<ProjectWithRole>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<ProjectWithRole> data = memberDao.getProjectsForUser(userId);
            liveData.postValue(data);
        });
        return liveData;
    }

    // Cung cấp cho Phân công 4 (Search)
    public LiveData<List<ProjectWithRole>> searchProjects(long userId, String keyword) {
        MutableLiveData<List<ProjectWithRole>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<ProjectWithRole> data = memberDao.searchProjects(userId, keyword);
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
            long id = memberDao.createProject(title, desc, status, startDate, endDate, expectedMembers, creatorUserId);
            if (id != -1) {
                onSuccess.run();
            } else if (onError != null) {
                onError.onError("Lỗi khi tạo dự án trong Database");
            }
        });
    }

    public void deleteProject(long projectId, long currentUserId, Runnable onSuccess, OnError onError) {
        executorService.execute(() -> {
            String role = memberDao.getUserRoleSync(projectId, currentUserId);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                if (onError != null) onError.onError("Chỉ ADMIN mới có quyền xóa dự án!");
                return;
            }
            
            boolean success = memberDao.deleteProject(projectId);
            if (success) {
                try {
                    roomDb.query("DELETE FROM tasks WHERE projectId = ?", new Object[]{projectId});
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
            List<MemberWithRole> data = memberDao.getMembersByProjectId(projectId);
            if (data != null) {
                for (MemberWithRole member : data) {
                    int count = 0;
                    try (android.database.Cursor cursor = roomDb.query(
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
            if (memberDao.isMemberAlreadyInProject(projectId, email)) {
                if (onError != null) onError.onError("Thành viên này đã có trong dự án");
                return;
            }

            long result = memberDao.addMemberByEmail(projectId, email, role);
            if (result == -2) {
                if (onError != null) onError.onError("Tài khoản email này không tồn tại trong hệ thống");
            } else if (result != -1) {
                if (onSuccess != null) onSuccess.run();
            } else if (onError != null) {
                onError.onError("Lỗi khi thêm thành viên");
            }
        });
    }

    public void removeMember(long projectId, long userId, Runnable onSuccess, OnError onError) {
        executorService.execute(() -> {
            // Nghiệp vụ: Kiểm tra Task chưa hoàn thành của Member (Kết nối Phân công 3)
            int count = 0;
            try (android.database.Cursor cursor = roomDb.query(
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

            memberDao.removeMember(projectId, userId);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public String getUserRoleSync(long projectId, long userId) {
        return memberDao.getUserRoleSync(projectId, userId);
    }

    public interface OnError {
        void onError(String message);
    }
}
