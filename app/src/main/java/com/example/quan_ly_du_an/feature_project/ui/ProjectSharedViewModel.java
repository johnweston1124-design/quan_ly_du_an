package com.example.quan_ly_du_an.feature_project.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.quan_ly_du_an.model.ProjectWithRole;

/**
 * ViewModel chia sẻ giữa Tab Dự án và Tab Đội ngũ để đồng bộ dữ liệu.
 */
public class ProjectSharedViewModel extends ViewModel {
    private final MutableLiveData<ProjectWithRole> _selectedProject = new MutableLiveData<>();
    public LiveData<ProjectWithRole> getSelectedProject() { return _selectedProject; }

    private final MutableLiveData<Boolean> _navigateToTeamRequest = new MutableLiveData<>(false);
    public LiveData<Boolean> getNavigateToTeamRequest() { return _navigateToTeamRequest; }

    public void selectProject(ProjectWithRole project) {
        _selectedProject.setValue(project);
        _navigateToTeamRequest.setValue(true);
    }

    public void completeNavigation() {
        _navigateToTeamRequest.setValue(false);
    }
}
