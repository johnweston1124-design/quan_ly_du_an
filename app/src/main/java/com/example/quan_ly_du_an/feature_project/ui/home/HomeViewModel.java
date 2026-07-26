package com.example.quan_ly_du_an.feature_project.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.quan_ly_du_an.model.ProjectWithRole;
import com.example.quan_ly_du_an.feature_project.repository.ProjectRepository;
import com.example.quan_ly_du_an.utils.SessionManager;
import androidx.lifecycle.Transformations;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final ProjectRepository repository;
    private final MutableLiveData<Long> _userId = new MutableLiveData<>();
    private final LiveData<List<ProjectWithRole>> _userProjects;
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new ProjectRepository(application);
        _userProjects = Transformations.switchMap(_userId, repository::getProjectsForUser);
        loadProjects();
    }

    public void loadProjects() {
        long userId = SessionManager.getCurrentUserId(getApplication());
        _userId.postValue(userId);
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
