package com.example.quan_ly_du_an.feature_project.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.quan_ly_du_an.feature_project.data.model.MemberWithRole;
import com.example.quan_ly_du_an.feature_project.data.model.ProjectWithRole;
import com.example.quan_ly_du_an.feature_project.data.sqlite.AppDbHelper;
import com.example.quan_ly_du_an.feature_project.data.sqlite.MemberDaoSqlite;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectRepository {
    private final MemberDaoSqlite memberDao;
    private final ExecutorService executorService;

    public ProjectRepository(Application application) {
        AppDbHelper dbHelper = AppDbHelper.getInstance(application);
        this.memberDao = new MemberDaoSqlite(dbHelper);
        this.executorService = Executors.newFixedThreadPool(4); // Tạo luồng ngầm
    }


    public LiveData<List<ProjectWithRole>> getProjectsForUser(long userId) {
        MutableLiveData<List<ProjectWithRole>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<ProjectWithRole> data = memberDao.getProjectsForUser(userId);
            liveData.postValue(data); // Đẩy dữ liệu lên UI thread
        });
        return liveData;
    }


    public LiveData<List<MemberWithRole>> getMembersByProjectId(long projectId) {
        MutableLiveData<List<MemberWithRole>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<MemberWithRole> data = memberDao.getMembersByProjectId(projectId);
            liveData.postValue(data);
        });
        return liveData;
    }

    public void createNewProject(String title, String desc, long creatorUserId, Runnable onSuccess) {
        executorService.execute(() -> {
            memberDao.createProject(title, desc, creatorUserId);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void addMemberByEmail(long projectId, String email, String role, Runnable onSuccess) {
        executorService.execute(() -> {
            memberDao.addMemberByEmail(projectId, email, role);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void removeMember(long projectId, long userId, Runnable onSuccess) {
        executorService.execute(() -> {
            memberDao.removeMember(projectId, userId);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public String getUserRoleSync(long projectId, long userId) {
        return memberDao.getUserRoleSync(projectId, userId);
    }
}