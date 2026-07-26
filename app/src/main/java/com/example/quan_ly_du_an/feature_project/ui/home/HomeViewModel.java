package com.example.quan_ly_du_an.feature_project.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.quan_ly_du_an.model.ProjectWithRole;
import com.example.quan_ly_du_an.feature_project.repository.ProjectRepository;
import com.example.quan_ly_du_an.utils.SessionManager;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final ProjectRepository repository;
    private final MutableLiveData<List<ProjectWithRole>> _userProjects = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new ProjectRepository(application);
        loadProjects();
    }

    public void loadProjects() {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            long userId = SessionManager.getCurrentUserId(getApplication());
            repository.getProjectsForUser(userId).observeForever(projects -> {
                _userProjects.postValue(projects);
            });
        });
    }

    public LiveData<List<ProjectWithRole>> getUserProjects() {
        return _userProjects;
    }

    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public void createProject(String title, String description, String startDate, String endDate, String status, int expectedMembers, Runnable onSuccess) {
        long userId = SessionManager.getCurrentUserId(getApplication());
        repository.createNewProject(title, description, startDate, endDate, status, expectedMembers, userId, () -> {
            loadProjects(); // Cập nhật lại danh sách ngay sau khi tạo
            if (onSuccess != null) onSuccess.run();
        }, msg -> _errorMessage.postValue(msg));
    }
    
    public void deleteProject(long projectId, Runnable onSuccess) {
        long userId = SessionManager.getCurrentUserId(getApplication());
        repository.deleteProject(projectId, userId, () -> {
            loadProjects();
            if (onSuccess != null) onSuccess.run();
        }, msg -> _errorMessage.postValue(msg));
    }
    
    public LiveData<List<ProjectWithRole>> search(String keyword) {
        long userId = SessionManager.getCurrentUserId(getApplication());
        return repository.searchProjects(userId, keyword);
    }
}
