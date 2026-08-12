package com.example.quan_ly_du_an.feature_project.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.feature_project.ui.ProjectSharedViewModel;
import com.example.quan_ly_du_an.feature_project.ui.base.BaseFragment;
import com.example.quan_ly_du_an.utils.Constants;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends BaseFragment {
    private HomeViewModel viewModel;
    private ProjectSharedViewModel sharedViewModel;
    private ProjectAdapter adapter;
    private RecyclerView recyclerViewProjects;
    private FloatingActionButton fabAddProject;
    private View emptyView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(ProjectSharedViewModel.class);

        recyclerViewProjects = view.findViewById(R.id.recyclerViewProjects);
        fabAddProject = view.findViewById(R.id.fabAddProject);

        setupRecyclerView();
        observeViewModel();

        fabAddProject.setOnClickListener(v -> showCreateProjectBottomSheet());
    }

    private void setupRecyclerView() {
        adapter = new ProjectAdapter(projectWithRole -> {
            sharedViewModel.selectProject(projectWithRole);
        });
        
        adapter.setOnProjectLongClickListener(projectWithRole -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa dự án")
                    .setMessage("Bạn có chắc chắn muốn xóa dự án '" + projectWithRole.title + "' không? Hành động này sẽ xóa toàn bộ thành viên và công việc thuộc dự án này.")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        viewModel.deleteProject(projectWithRole.projectId, () -> {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    showTimedToast("Xóa dự án thành công");
                                });
                            }
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
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
                layout.setPadding(dpToPx(requireContext(), 32), dpToPx(requireContext(), 64), dpToPx(requireContext(), 32), dpToPx(requireContext(), 64));
                
                TextView tvIcon = new TextView(getContext());
                tvIcon.setText("📁");
                tvIcon.setTextSize(48);
                tvIcon.setGravity(android.view.Gravity.CENTER);
                layout.addView(tvIcon);
                
                TextView tvTitle = new TextView(getContext());
                tvTitle.setText("Chưa có dự án nào");
                tvTitle.setTextSize(16);
                tvTitle.setTextColor(Color.parseColor("#424242"));
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTitle.setPadding(0, dpToPx(requireContext(), 16), 0, dpToPx(requireContext(), 4));
                tvTitle.setGravity(android.view.Gravity.CENTER);
                layout.addView(tvTitle);
                
                TextView tvSub = new TextView(getContext());
                tvSub.setText("Nhấn nút (+) bên dưới để tạo dự án mới.");
                tvSub.setTextSize(13);
                tvSub.setTextColor(Color.parseColor("#757575"));
                tvSub.setGravity(android.view.Gravity.CENTER);
                layout.addView(tvSub);
                
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

    public void showCreateProjectBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_setup_project, null);
        bottomSheetDialog.setContentView(dialogView);

        EditText inputTitle = dialogView.findViewById(R.id.edtProjectTitle);
        EditText inputDesc = dialogView.findViewById(R.id.edtProjectDesc);
        EditText edtMembers = dialogView.findViewById(R.id.edtMembersCount);
        EditText edtStatus = dialogView.findViewById(R.id.edtProjectStatus);
        EditText edtStart = dialogView.findViewById(R.id.edtStartDate);
        EditText edtEnd = dialogView.findViewById(R.id.edtEndDate);
        View btnCreate = dialogView.findViewById(R.id.btnCreateProject);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String todayStr = sdf.format(new java.util.Date());

        if (edtStart != null) edtStart.setText(todayStr);
        if (edtEnd != null) edtEnd.setText(todayStr);

        if (edtStatus != null) {
            edtStatus.setOnClickListener(v -> {
                String[] statuses = {"Kế hoạch", "Đang thực hiện", "Hoàn thành"};
                new AlertDialog.Builder(requireContext())
                        .setTitle("Chọn trạng thái")
                        .setItems(statuses, (dialog, which) -> edtStatus.setText(statuses[which]))
                        .show();
            });
        }

        View.OnClickListener datePickerListener = v -> {
            EditText target = (EditText) v;
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(requireContext(), (view2, year, month, dayOfMonth) -> {
                cal.set(java.util.Calendar.YEAR, year);
                cal.set(java.util.Calendar.MONTH, month);
                cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
                target.setText(sdf.format(cal.getTime()));
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        };

        if (edtStart != null) edtStart.setOnClickListener(datePickerListener);
        if (edtEnd != null) edtEnd.setOnClickListener(datePickerListener);

        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                String title = inputTitle != null ? inputTitle.getText().toString().trim() : "";
                String desc = inputDesc != null ? inputDesc.getText().toString().trim() : "";
                String start = edtStart != null ? edtStart.getText().toString().trim() : "";
                String end = edtEnd != null ? edtEnd.getText().toString().trim() : "";
                String status = edtStatus != null ? edtStatus.getText().toString().trim() : "";
                String membersStr = edtMembers != null ? edtMembers.getText().toString().trim() : "";
                int members = 5;
                try {
                    members = Integer.parseInt(membersStr);
                } catch (Exception ignored) {}

                if (!title.isEmpty()) {
                    viewModel.createProject(title, desc, start, end, status, members, () -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                viewModel.loadProjects();
                                showTimedToast("Dự án đã được tạo thành công!");
                                bottomSheetDialog.dismiss();
                            });
                        }
                    });
                } else {
                    showTimedToast("Vui lòng nhập tên dự án");
                }
            });
        }

        bottomSheetDialog.show();
    }

    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
