package com.example.quan_ly_du_an.feature_project.ui.project;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.quan_ly_du_an.feature_project.data.model.MemberWithRole;
import com.example.quan_ly_du_an.feature_project.ui.member.AddMemberDialog;
import com.example.quan_ly_du_an.utils.RoleUtils;
import com.example.quan_ly_du_an.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProjectDetailFragment extends Fragment {
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_ROLE = "role";

    private long projectId;
    private String projectTitle;
    private String currentUserRole;
    private ProjectViewModel viewModel;

    public static ProjectDetailFragment newInstance(long projectId, String title, String role) {
        ProjectDetailFragment fragment = new ProjectDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PROJECT_ID, projectId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_ROLE, role);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            projectId = getArguments().getLong(ARG_PROJECT_ID);
            projectTitle = getArguments().getString(ARG_TITLE);
            currentUserRole = getArguments().getString(ARG_ROLE);
        }

        LinearLayout rootLayout = new LinearLayout(getContext());
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#F8F9FA"));
        rootLayout.setPadding(48, 160, 48, 48);

        TextView tvHeader = new TextView(getContext());
        tvHeader.setText("Dự án: " + projectTitle);
        tvHeader.setTextSize(22);
        tvHeader.setTextColor(Color.parseColor("#1B5E20"));
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        rootLayout.addView(tvHeader);

        TextView tvRoleNotification = new TextView(getContext());
        tvRoleNotification.setText("Quyền của bạn: " + currentUserRole);
        tvRoleNotification.setPadding(0, 8, 0, 24);
        tvRoleNotification.setTextColor(Color.parseColor("#757575"));
        rootLayout.addView(tvRoleNotification);

        ListView listViewMembers = new ListView(getContext());
        listViewMembers.setDividerHeight(16);
        rootLayout.addView(listViewMembers, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        FloatingActionButton fabAdd = new FloatingActionButton(requireContext());
        fabAdd.setImageResource(android.R.drawable.ic_input_add);
        fabAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9")));

        LinearLayout.LayoutParams fabParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fabParams.gravity = android.view.Gravity.END;
        fabParams.setMargins(0, 16, 16, 16);
        rootLayout.addView(fabAdd, fabParams);

        // PHÂN QUYỀN: Nếu là MEMBER -> Ẩn nút Thêm thành viên
        if (!RoleUtils.canManageMembers(currentUserRole)) {
            fabAdd.setVisibility(View.GONE);
        } else {
            fabAdd.setOnClickListener(v -> {
                AddMemberDialog dialog = AddMemberDialog.newInstance(projectId);
                dialog.show(getChildFragmentManager(), "AddMemberDialog");
            });
        }

        viewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
        viewModel.getProjectMembers(projectId).observe(getViewLifecycleOwner(), members -> {
            if (members != null && getContext() != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
                for (MemberWithRole m : members) {
                    adapter.add(m.name + " (" + m.email + ") - [" + m.role + "]");
                }
                listViewMembers.setAdapter(adapter);

                if (RoleUtils.canManageMembers(currentUserRole)) {
                    listViewMembers.setOnItemClickListener((parent, view1, position, id) -> {
                        MemberWithRole selectedMember = members.get(position);
                        if (selectedMember.userId == SessionManager.getCurrentUserId()) {
                            Toast.makeText(getContext(), "Bạn không thể tự xóa chính mình!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showRemoveConfirmDialog(selectedMember.userId, selectedMember.name);
                    });
                }
            }
        });

        return rootLayout;
    }

    private void showRemoveConfirmDialog(long userIdToRemove, String userName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa thành viên")
                .setMessage("Bạn có chắc chắn muốn xóa '" + userName + "' khỏi dự án này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.removeMember(projectId, userIdToRemove, () -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Đã xóa thành viên!", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}