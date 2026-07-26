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

    private static class ProjectInputFields {
        EditText edtMembers;
        EditText edtStatus;
        EditText edtStart;
        EditText edtEnd;
    }

    private ProjectInputFields injectCustomProjectFields(LinearLayout rootContainer) {
        ProjectInputFields fields = new ProjectInputFields();

        com.google.android.material.textfield.TextInputLayout layoutMembers = new com.google.android.material.textfield.TextInputLayout(requireContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        layoutMembers.setHint("Số lượng thành viên dự kiến");
        LinearLayout.LayoutParams membersParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        membersParams.topMargin = dpToPx(requireContext(), 12);
        layoutMembers.setLayoutParams(membersParams);
        
        com.google.android.material.textfield.TextInputEditText edtMembers = new com.google.android.material.textfield.TextInputEditText(layoutMembers.getContext());
        edtMembers.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtMembers.setText("5");
        layoutMembers.addView(edtMembers);
        rootContainer.addView(layoutMembers, 3);
        fields.edtMembers = edtMembers;

        com.google.android.material.textfield.TextInputLayout layoutStatus = new com.google.android.material.textfield.TextInputLayout(requireContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        layoutStatus.setHint("Trạng thái ban đầu");
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusParams.topMargin = dpToPx(requireContext(), 12);
        layoutStatus.setLayoutParams(statusParams);
        
        com.google.android.material.textfield.TextInputEditText edtStatus = new com.google.android.material.textfield.TextInputEditText(layoutStatus.getContext());
        edtStatus.setFocusable(false);
        edtStatus.setClickable(true);
        edtStatus.setText("Đang thực hiện");
        edtStatus.setOnClickListener(v -> {
            String[] statuses = {"Kế hoạch", "Đang thực hiện", "Hoàn thành"};
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn trạng thái")
                    .setItems(statuses, (dialog, which) -> {
                        edtStatus.setText(statuses[which]);
                    })
                    .show();
        });
        layoutStatus.addView(edtStatus);
        rootContainer.addView(layoutStatus, 4);
        fields.edtStatus = edtStatus;

        LinearLayout dateContainer = new LinearLayout(requireContext());
        dateContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams dateContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateContainerParams.topMargin = dpToPx(requireContext(), 12);
        dateContainer.setLayoutParams(dateContainerParams);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String todayStr = sdf.format(new java.util.Date());

        com.google.android.material.textfield.TextInputLayout layoutStart = new com.google.android.material.textfield.TextInputLayout(requireContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        layoutStart.setHint("Ngày bắt đầu");
        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        startParams.rightMargin = dpToPx(requireContext(), 6);
        layoutStart.setLayoutParams(startParams);
        
        com.google.android.material.textfield.TextInputEditText edtStart = new com.google.android.material.textfield.TextInputEditText(layoutStart.getContext());
        edtStart.setFocusable(false);
        edtStart.setClickable(true);
        edtStart.setText(todayStr);
        edtStart.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(requireContext(), (view2, year, month, dayOfMonth) -> {
                cal.set(java.util.Calendar.YEAR, year);
                cal.set(java.util.Calendar.MONTH, month);
                cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
                edtStart.setText(sdf.format(cal.getTime()));
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
        layoutStart.addView(edtStart);
        dateContainer.addView(layoutStart);
        fields.edtStart = edtStart;

        com.google.android.material.textfield.TextInputLayout layoutEnd = new com.google.android.material.textfield.TextInputLayout(requireContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        layoutEnd.setHint("Ngày kết thúc");
        LinearLayout.LayoutParams endParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        endParams.leftMargin = dpToPx(requireContext(), 6);
        layoutEnd.setLayoutParams(endParams);
        
        com.google.android.material.textfield.TextInputEditText edtEnd = new com.google.android.material.textfield.TextInputEditText(layoutEnd.getContext());
        edtEnd.setFocusable(false);
        edtEnd.setClickable(true);
        edtEnd.setText(todayStr);
        edtEnd.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(requireContext(), (view2, year, month, dayOfMonth) -> {
                cal.set(java.util.Calendar.YEAR, year);
                cal.set(java.util.Calendar.MONTH, month);
                cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
                edtEnd.setText(sdf.format(cal.getTime()));
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
        layoutEnd.addView(edtEnd);
        dateContainer.addView(layoutEnd);
        fields.edtEnd = edtEnd;

        rootContainer.addView(dateContainer, 5);

        return fields;
    }

    private void showCreateProjectBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_setup_project, null);
        bottomSheetDialog.setContentView(dialogView);

        EditText inputTitle = dialogView.findViewById(R.id.edtProjectTitle);
        EditText inputDesc = dialogView.findViewById(R.id.edtProjectDesc);
        View btnCreate = dialogView.findViewById(R.id.btnCreateProject);

        if (btnCreate == null) {
            showCreateProjectDialog();
            return;
        }

        ProjectInputFields fields = injectCustomProjectFields((LinearLayout) dialogView);

        btnCreate.setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            String start = fields.edtStart.getText().toString().trim();
            String end = fields.edtEnd.getText().toString().trim();
            String status = fields.edtStatus.getText().toString().trim();
            String membersStr = fields.edtMembers.getText().toString().trim();
            int members = 1;
            try {
                members = Integer.parseInt(membersStr);
            } catch (Exception ignored) {}

            if (!title.isEmpty()) {
                viewModel.createProject(title, desc, start, end, status, members, () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            viewModel.loadProjects();
                            showTimedToast("Dự án đã được tạo và liên kết đội ngũ thành công!");
                            bottomSheetDialog.dismiss();
                        });
                    }
                });
            } else {
                showTimedToast("Vui lòng nhập tên dự án");
            }
        });

        bottomSheetDialog.show();
    }

    private void showCreateProjectDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_setup_project, null);
        EditText inputTitle = dialogView.findViewById(R.id.edtProjectTitle);
        EditText inputDesc = dialogView.findViewById(R.id.edtProjectDesc);

        ProjectInputFields fields = injectCustomProjectFields((LinearLayout) dialogView);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Tạo Dự Án", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    String desc = inputDesc.getText().toString().trim();
                    String start = fields.edtStart.getText().toString().trim();
                    String end = fields.edtEnd.getText().toString().trim();
                    String status = fields.edtStatus.getText().toString().trim();
                    String membersStr = fields.edtMembers.getText().toString().trim();
                    int members = 1;
                    try {
                        members = Integer.parseInt(membersStr);
                    } catch (Exception ignored) {}
                    
                    if (!title.isEmpty()) {
                        viewModel.createProject(title, desc, start, end, status, members, () -> {
                            if (getActivity() != null) {
                                  getActivity().runOnUiThread(() -> {
                                      viewModel.loadProjects();
                                      showTimedToast("Dự án đã được khởi tạo và liên kết đội ngũ!");
                                  });
                            }
                        });
                    } else {
                        showTimedToast("Vui lòng nhập tên dự án");
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
