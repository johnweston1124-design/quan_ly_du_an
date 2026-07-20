package com.example.quan_ly_du_an.feature_project.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.quan_ly_du_an.feature_project.data.model.ProjectWithRole;
import com.example.quan_ly_du_an.feature_project.repository.ProjectRepository;
import com.example.quan_ly_du_an.utils.SessionManager;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final ProjectRepository repository;
    private LiveData<List<ProjectWithRole>> userProjects;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new ProjectRepository(application);
        loadProjects();
    }

    public void loadProjects() {
        userProjects = repository.getProjectsForUser(SessionManager.getCurrentUserId());
    }

    public LiveData<List<ProjectWithRole>> getUserProjects() {
        if (userProjects == null) loadProjects();
        return userProjects;
    }

    public void createProject(String title, String description, Runnable onSuccess) {
        repository.createNewProject(title, description, SessionManager.getCurrentUserId(), () -> {
            loadProjects();
            if (onSuccess != null) onSuccess.run();
        });
    }
}