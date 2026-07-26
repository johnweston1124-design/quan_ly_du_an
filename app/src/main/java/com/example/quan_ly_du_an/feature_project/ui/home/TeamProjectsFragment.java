package com.example.quan_ly_du_an.feature_project.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.feature_project.ui.base.BaseFragment;
import com.example.quan_ly_du_an.feature_project.ui.project.ProjectDetailFragment;
import com.example.quan_ly_du_an.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TeamProjectsFragment extends BaseFragment {
    private HomeViewModel viewModel;
    private ProjectAdapter adapter;
    private RecyclerView recyclerViewProjects;
    private View emptyView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        recyclerViewProjects = view.findViewById(R.id.recyclerViewProjects);
        FloatingActionButton fabAddProject = view.findViewById(R.id.fabAddProject);
        
        // Ẩn nút thêm dự án ở tab Đội ngũ
        fabAddProject.setVisibility(View.GONE);

        // Thay đổi tiêu đề Header cho phù hợp với tab Đội ngũ
        TextView tvHeaderTitle = view.findViewById(R.id.tvHomeHeaderTitle);
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText("Đội ngũ");
        }
        TextView tvHeaderSubtitle = view.findViewById(R.id.tvHomeHeaderSubtitle);
        if (tvHeaderSubtitle != null) {
            tvHeaderSubtitle.setText("Quản lý thành viên trong dự án");
        }

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ProjectAdapter(projectWithRole -> {
            // Khi nhấn vào dự án, mở chi tiết thành viên
            ProjectDetailFragment fragment = ProjectDetailFragment.newInstance(
                    projectWithRole.projectId,
                    projectWithRole.title,
                    projectWithRole.role);
            
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.layoutTeam, fragment)
                        .addToBackStack(null) // Hỗ trợ nút Back quay lại danh sách
                        .commit();
            }
        });
        
        recyclerViewProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewProjects.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getUserProjects().observe(getViewLifecycleOwner(), projects -> {
            if (projects != null) {
                adapter.setProjects(projects);
                showEmptyState(projects.isEmpty());
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                showTimedToast(message);
            }
        });
    }

    private void showEmptyState(boolean show) {
        if (show) {
            recyclerViewProjects.setVisibility(View.GONE);
            if (emptyView == null) {
                LinearLayout parent = (LinearLayout) recyclerViewProjects.getParent();
                
                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setGravity(android.view.Gravity.CENTER);
                layout.setPadding(dpToPx(32), dpToPx(64), dpToPx(32), dpToPx(64));
                
                TextView tvIcon = new TextView(getContext());
                tvIcon.setText("📁");
                tvIcon.setTextSize(48);
                tvIcon.setGravity(android.view.Gravity.CENTER);
                layout.addView(tvIcon);
                
                TextView tvTitle = new TextView(getContext());
                tvTitle.setText("Bạn chưa tham gia dự án nào");
                tvTitle.setTextSize(16);
                tvTitle.setTextColor(Color.parseColor("#424242"));
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTitle.setPadding(0, dpToPx(16), 0, dpToPx(4));
                tvTitle.setGravity(android.view.Gravity.CENTER);
                layout.addView(tvTitle);
                
                emptyView = layout;
                parent.addView(emptyView, parent.indexOfChild(recyclerViewProjects));
            }
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewProjects.setVisibility(View.VISIBLE);
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private void showTimedToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
        new Handler(Looper.getMainLooper()).postDelayed(toast::cancel, Constants.TOAST_DURATION_MS);
    }

    private int dpToPx(int dp) {
        if (getContext() == null) return dp;
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }
}
