package com.example.quan_ly_du_an.feature_project.ui.project;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.quan_ly_du_an.model.MemberWithRole;
import com.example.quan_ly_du_an.feature_project.repository.ProjectRepository;
import com.example.quan_ly_du_an.utils.RoleUtils;
import com.example.quan_ly_du_an.utils.SessionManager;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProjectViewModel extends AndroidViewModel {
    private final ProjectRepository repository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProjectViewModel(@NonNull Application application) {
        super(application);
        repository = new ProjectRepository(application);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public LiveData<List<MemberWithRole>> getProjectMembers(long projectId) {
        return repository.getMembersByProjectId(projectId);
    }

    public void addMember(long projectId, String email, String role, Runnable onSuccess) {
        String myRole = getCurrentUserRole(projectId);
        if (!RoleUtils.canManageMembers(myRole)) {
            errorMessage.setValue("Bạn không có quyền thêm thành viên!");
            return;
        }

        repository.addMemberByEmail(projectId, email, role, onSuccess, msg -> errorMessage.postValue(msg));
    }

    public void removeMember(long projectId, long userId, Runnable onSuccess) {
        String myRole = getCurrentUserRole(projectId);
        if (!RoleUtils.canManageMembers(myRole)) {
            errorMessage.setValue("Bạn không có quyền xóa thành viên!");
            return;
        }

        if (userId == SessionManager.getCurrentUserId(getApplication())) {
            errorMessage.setValue("Bạn không thể tự xóa mình khỏi dự án!");
            return;
        }

        repository.removeMember(projectId, userId, onSuccess, msg -> errorMessage.postValue(msg));
    }

    public String getCurrentUserRole(long projectId) {
        Future<String> future = Executors.newSingleThreadExecutor().submit(() ->
                repository.getUserRoleSync(projectId, SessionManager.getCurrentUserId(getApplication())));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return "MEMBER";
        }
    }
}
