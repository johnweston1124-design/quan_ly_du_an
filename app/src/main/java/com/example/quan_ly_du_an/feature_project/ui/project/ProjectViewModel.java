package com.example.quan_ly_du_an.feature_project.ui.project;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.quan_ly_du_an.feature_project.data.model.MemberWithRole;
import com.example.quan_ly_du_an.feature_project.repository.ProjectRepository;
import com.example.quan_ly_du_an.utils.SessionManager;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProjectViewModel extends AndroidViewModel {
    private final ProjectRepository repository;

    public ProjectViewModel(@NonNull Application application) {
        super(application);
        repository = new ProjectRepository(application);
    }

    public LiveData<List<MemberWithRole>> getProjectMembers(long projectId) {
        return repository.getMembersByProjectId(projectId);
    }

    public String getCurrentUserRole(long projectId) {
        Future<String> future = Executors.newSingleThreadExecutor().submit(() ->
                repository.getUserRoleSync(projectId, SessionManager.getCurrentUserId()));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return "MEMBER";
        }
    }

    public void addMember(long projectId, String email, String role, Runnable onSuccess) {
        repository.addMemberByEmail(projectId, email, role, onSuccess);
    }

    public void removeMember(long projectId, long userId, Runnable onSuccess) {
        repository.removeMember(projectId, userId, onSuccess);
    }
}