package com.example.quan_ly_du_an.feature_project.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.feature_project.ui.base.BaseFragment;
import com.example.quan_ly_du_an.feature_project.ui.project.ProjectDetailFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends BaseFragment {
    private HomeViewModel viewModel;
    private ProjectAdapter adapter;
    private RecyclerView recyclerViewProjects;
    private FloatingActionButton fabAddProject;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        recyclerViewProjects = view.findViewById(R.id.recyclerViewProjects);
        fabAddProject = view.findViewById(R.id.fabAddProject);

        setupRecyclerView();
        observeViewModel();

        fabAddProject.setOnClickListener(v -> showCreateProjectDialog());
    }

    private void setupRecyclerView() {
        adapter = new ProjectAdapter(projectWithRole -> {
            ProjectDetailFragment fragment = ProjectDetailFragment.newInstance(
                    projectWithRole.projectId,
                    projectWithRole.title,
                    projectWithRole.role);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerViewProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewProjects.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getUserProjects().observe(getViewLifecycleOwner(), projects -> {
            if (projects != null) {
                adapter.setProjects(projects);
            }
        });
    }

    private void showCreateProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tạo dự án mới");

        final EditText inputTitle = new EditText(getContext());
        inputTitle.setHint("Tên dự án");
        final EditText inputDesc = new EditText(getContext());
        inputDesc.setHint("Mô tả dự án");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(inputTitle);
        layout.addView(inputDesc);
        builder.setView(layout);

        builder.setPositiveButton("Tạo Ngay", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            if (!title.isEmpty()) {
                viewModel.createProject(title, desc, () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> viewModel.loadProjects());
                    }
                });
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}