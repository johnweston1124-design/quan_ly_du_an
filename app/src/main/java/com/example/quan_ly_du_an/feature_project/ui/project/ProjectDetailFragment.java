package com.example.quan_ly_du_an.feature_project.ui.project;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.feature_project.data.model.MemberWithRole;
import com.example.quan_ly_du_an.feature_project.ui.member.AddMemberDialog;
import com.example.quan_ly_du_an.utils.RoleUtils;
import com.example.quan_ly_du_an.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ProjectDetailFragment extends Fragment {
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_ROLE = "role";

    private long projectId;
    private String projectTitle;
    private String currentUserRole;
    private ProjectViewModel viewModel;

    private RecyclerView recyclerViewMembers;
    private MemberAdapter adapter;
    private LinearLayout layoutEmptyState;
    private List<MemberWithRole> memberList = new ArrayList<>();

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

        // Parent FrameLayout to hold Content & FAB
        FrameLayout rootLayout = new FrameLayout(requireContext());
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.setBackgroundColor(Color.parseColor("#F8F9FA"));

        // Content Container (Vertical LinearLayout)
        LinearLayout contentContainer = new LinearLayout(requireContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentContainer.setLayoutParams(contentParams);
        contentContainer.setPadding(dpToPx(requireContext(), 16), dpToPx(requireContext(), 60), dpToPx(requireContext(), 16), dpToPx(requireContext(), 16));
        rootLayout.addView(contentContainer);

        // Header Title
        TextView tvHeader = new TextView(requireContext());
        tvHeader.setText("Dự án: " + projectTitle);
        tvHeader.setTextSize(22);
        tvHeader.setTextColor(Color.parseColor("#1B5E20"));
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        contentContainer.addView(tvHeader);

        // User Role info
        TextView tvRoleNotification = new TextView(requireContext());
        tvRoleNotification.setText("Vai trò của bạn: " + currentUserRole);
        tvRoleNotification.setPadding(0, dpToPx(requireContext(), 4), 0, dpToPx(requireContext(), 16));
        tvRoleNotification.setTextColor(Color.parseColor("#757575"));
        tvRoleNotification.setTextSize(14);
        contentContainer.addView(tvRoleNotification);

        // FrameLayout for List & Empty State
        FrameLayout listContainer = new FrameLayout(requireContext());
        LinearLayout.LayoutParams listContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        listContainer.setLayoutParams(listContainerParams);
        contentContainer.addView(listContainer);

        // RecyclerView
        recyclerViewMembers = new RecyclerView(requireContext());
        recyclerViewMembers.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        listContainer.addView(recyclerViewMembers);

        // Programmatic Empty State Layout
        layoutEmptyState = new LinearLayout(requireContext());
        layoutEmptyState.setOrientation(LinearLayout.VERTICAL);
        layoutEmptyState.setGravity(android.view.Gravity.CENTER);
        layoutEmptyState.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutEmptyState.setVisibility(View.GONE);
        listContainer.addView(layoutEmptyState);

        TextView tvEmptyIcon = new TextView(requireContext());
        tvEmptyIcon.setText("👥");
        tvEmptyIcon.setTextSize(48);
        tvEmptyIcon.setGravity(android.view.Gravity.CENTER);
        layoutEmptyState.addView(tvEmptyIcon);

        TextView tvEmptyTitle = new TextView(requireContext());
        tvEmptyTitle.setText("Chưa có thành viên nào");
        tvEmptyTitle.setTextSize(16);
        tvEmptyTitle.setTextColor(Color.parseColor("#424242"));
        tvEmptyTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvEmptyTitle.setPadding(0, dpToPx(requireContext(), 16), 0, dpToPx(requireContext(), 4));
        tvEmptyTitle.setGravity(android.view.Gravity.CENTER);
        layoutEmptyState.addView(tvEmptyTitle);

        TextView tvEmptySub = new TextView(requireContext());
        tvEmptySub.setText(RoleUtils.canManageMembers(currentUserRole) 
                ? "Nhấn nút (+) bên dưới để thêm thành viên mới." 
                : "Liên hệ Admin hoặc Leader để thêm thành viên.");
        tvEmptySub.setTextSize(13);
        tvEmptySub.setTextColor(Color.parseColor("#757575"));
        tvEmptySub.setGravity(android.view.Gravity.CENTER);
        layoutEmptyState.addView(tvEmptySub);

        // Floating Action Button (+)
        FloatingActionButton fabAdd = new FloatingActionButton(requireContext());
        fabAdd.setImageResource(android.R.drawable.ic_input_add);
        fabAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
        fabAdd.setColorFilter(Color.parseColor("#2E7D32"));
        
        FrameLayout.LayoutParams fabParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fabParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        fabParams.setMargins(0, 0, dpToPx(requireContext(), 24), dpToPx(requireContext(), 24));
        fabAdd.setLayoutParams(fabParams);
        rootLayout.addView(fabAdd);

        // Setup role-based access
        if (!RoleUtils.canManageMembers(currentUserRole)) {
            fabAdd.setVisibility(View.GONE);
        } else {
            fabAdd.setOnClickListener(v -> {
                AddMemberDialog dialog = AddMemberDialog.newInstance(projectId);
                dialog.show(getChildFragmentManager(), "AddMemberDialog");
            });
        }

        // ViewModels & Observers
        viewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
        
        long myUserId = SessionManager.getCurrentUserId(requireContext());
        adapter = new MemberAdapter(memberList, currentUserRole, myUserId, selectedMember -> {
            showRemoveConfirmDialog(selectedMember.userId, selectedMember.name);
        });
        recyclerViewMembers.setAdapter(adapter);

        loadMembers();

        getChildFragmentManager().setFragmentResultListener("refresh_members", getViewLifecycleOwner(), (requestKey, bundle) -> {
            loadMembers();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });

        return rootLayout;
    }

    private void loadMembers() {
        viewModel.getProjectMembers(projectId).observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                memberList.clear();
                memberList.addAll(members);
                adapter.notifyDataSetChanged();
                
                if (members.isEmpty()) {
                    recyclerViewMembers.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewMembers.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showRemoveConfirmDialog(long userIdToRemove, String userName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa thành viên")
                .setMessage("Bạn có chắc chắn muốn xóa '" + userName + "' khỏi dự án này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.removeMember(projectId, userIdToRemove, () -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Đã xóa thành viên khỏi dự án!", Toast.LENGTH_SHORT).show();
                                loadMembers();
                            });
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    // --- RECYCLER VIEW ADAPTER CLASS ---
    private static class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
        private final List<MemberWithRole> members;
        private final String currentUserRole;
        private final long currentUserId;
        private final OnMemberDeleteClickListener deleteClickListener;

        interface OnMemberDeleteClickListener {
            void onDeleteClick(MemberWithRole member);
        }

        MemberAdapter(List<MemberWithRole> members, String currentUserRole, long currentUserId, OnMemberDeleteClickListener deleteClickListener) {
            this.members = members;
            this.currentUserRole = currentUserRole;
            this.currentUserId = currentUserId;
            this.deleteClickListener = deleteClickListener;
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            
            androidx.cardview.widget.CardView cardView = new androidx.cardview.widget.CardView(context);
            ViewGroup.MarginLayoutParams cardParams = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            int marginHorizontal = dpToPx(context, 4);
            int marginVertical = dpToPx(context, 6);
            cardParams.setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(dpToPx(context, 12));
            cardView.setCardElevation(dpToPx(context, 2));
            cardView.setCardBackgroundColor(Color.WHITE);
            
            LinearLayout horizontalLayout = new LinearLayout(context);
            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
            horizontalLayout.setPadding(dpToPx(context, 12), dpToPx(context, 12), dpToPx(context, 12), dpToPx(context, 12));
            horizontalLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            cardView.addView(horizontalLayout);
            
            // 1. Avatar circle Text
            TextView tvAvatar = new TextView(context);
            LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dpToPx(context, 44), dpToPx(context, 44));
            tvAvatar.setLayoutParams(avatarParams);
            tvAvatar.setGravity(android.view.Gravity.CENTER);
            tvAvatar.setTextColor(Color.WHITE);
            tvAvatar.setTextSize(14);
            tvAvatar.setTypeface(null, android.graphics.Typeface.BOLD);
            horizontalLayout.addView(tvAvatar);
            
            // 2. Member Info layout
            LinearLayout infoLayout = new LinearLayout(context);
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            infoParams.leftMargin = dpToPx(context, 12);
            infoParams.rightMargin = dpToPx(context, 12);
            infoLayout.setLayoutParams(infoParams);
            horizontalLayout.addView(infoLayout);
            
            TextView tvName = new TextView(context);
            tvName.setTextColor(Color.parseColor("#212121"));
            tvName.setTextSize(15);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            infoLayout.addView(tvName);
            
            TextView tvEmail = new TextView(context);
            tvEmail.setTextColor(Color.parseColor("#757575"));
            tvEmail.setTextSize(12);
            tvEmail.setPadding(0, 1, 0, 3);
            infoLayout.addView(tvEmail);
            
            TextView tvTaskInfo = new TextView(context);
            tvTaskInfo.setTextColor(Color.parseColor("#2E7D32"));
            tvTaskInfo.setTextSize(11);
            tvTaskInfo.setTypeface(null, android.graphics.Typeface.ITALIC);
            infoLayout.addView(tvTaskInfo);
            
            // 3. Right Badge & Delete Layout
            LinearLayout rightLayout = new LinearLayout(context);
            rightLayout.setOrientation(LinearLayout.VERTICAL);
            rightLayout.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
            rightLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            horizontalLayout.addView(rightLayout);
            
            TextView tvRole = new TextView(context);
            tvRole.setPadding(dpToPx(context, 6), dpToPx(context, 3), dpToPx(context, 6), dpToPx(context, 3));
            tvRole.setTextSize(10);
            tvRole.setTypeface(null, android.graphics.Typeface.BOLD);
            rightLayout.addView(tvRole);
            
            android.widget.ImageView ivDelete = new android.widget.ImageView(context);
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(dpToPx(context, 28), dpToPx(context, 28));
            deleteParams.topMargin = dpToPx(context, 6);
            ivDelete.setLayoutParams(deleteParams);
            ivDelete.setImageResource(android.R.drawable.ic_menu_delete);
            ivDelete.setColorFilter(Color.parseColor("#D32F2F"));
            ivDelete.setFocusable(true);
            ivDelete.setClickable(true);
            
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            ivDelete.setBackgroundResource(outValue.resourceId);
            rightLayout.addView(ivDelete);
            
            return new MemberViewHolder(cardView, tvAvatar, tvName, tvEmail, tvTaskInfo, tvRole, ivDelete);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            MemberWithRole item = members.get(position);
            
            holder.tvName.setText(item.name);
            holder.tvEmail.setText(item.email);
            
            // Set Avatar Circle
            String initials = getInitials(item.name);
            holder.tvAvatar.setText(initials);
            GradientDrawable gdAvatar = new GradientDrawable();
            gdAvatar.setShape(GradientDrawable.OVAL);
            gdAvatar.setColor(getAvatarBgColor(item.name));
            holder.tvAvatar.setBackground(gdAvatar);
            
            // Set Task Info text
            if (item.taskCount > 0) {
                holder.tvTaskInfo.setText("Đang phụ trách: " + item.taskCount + " công việc");
                holder.tvTaskInfo.setTextColor(Color.parseColor("#E65100")); // Orange warning
            } else {
                holder.tvTaskInfo.setText("Không có công việc");
                holder.tvTaskInfo.setTextColor(Color.parseColor("#757575")); // Gray
            }
            
            // Set Role badge
            holder.tvRole.setText(item.role);
            GradientDrawable gdRole = new GradientDrawable();
            gdRole.setCornerRadius(dpToPx(holder.itemView.getContext(), 6));
            
            if ("ADMIN".equalsIgnoreCase(item.role)) {
                holder.tvRole.setTextColor(Color.parseColor("#D32F2F"));
                gdRole.setColor(Color.parseColor("#FFEBEE"));
            } else if ("LEADER".equalsIgnoreCase(item.role)) {
                holder.tvRole.setTextColor(Color.parseColor("#F57C00"));
                gdRole.setColor(Color.parseColor("#FFF3E0"));
            } else {
                holder.tvRole.setTextColor(Color.parseColor("#388E3C"));
                gdRole.setColor(Color.parseColor("#E8F5E9"));
            }
            holder.tvRole.setBackground(gdRole);
            
            // Handle Delete accessibility
            boolean canManage = RoleUtils.canManageMembers(currentUserRole);
            boolean isSelf = (item.userId == currentUserId);
            
            if (canManage && !isSelf) {
                holder.ivDelete.setVisibility(View.VISIBLE);
                holder.ivDelete.setOnClickListener(v -> {
                    if (deleteClickListener != null) {
                        deleteClickListener.onDeleteClick(item);
                    }
                });
            } else {
                holder.ivDelete.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        private String getInitials(String name) {
            if (name == null || name.trim().isEmpty()) return "U";
            String[] parts = name.trim().split("\\s+");
            if (parts.length == 0) return "U";
            if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            String first = parts[0].substring(0, 1);
            String last = parts[parts.length - 1].substring(0, 1);
            return (first + last).toUpperCase();
        }

        private int getAvatarBgColor(String name) {
            int hash = name != null ? name.hashCode() : 0;
            String[] colors = {
                    "#3F51B5", "#009688", "#673AB7", "#E91E63", 
                    "#03A9F4", "#4CAF50", "#FF9800", "#795548"
            };
            int index = Math.abs(hash) % colors.length;
            return Color.parseColor(colors[index]);
        }

        static class MemberViewHolder extends RecyclerView.ViewHolder {
            final TextView tvAvatar;
            final TextView tvName;
            final TextView tvEmail;
            final TextView tvTaskInfo;
            final TextView tvRole;
            final android.widget.ImageView ivDelete;

            MemberViewHolder(View itemView, TextView tvAvatar, TextView tvName, TextView tvEmail, TextView tvTaskInfo, TextView tvRole, android.widget.ImageView ivDelete) {
                super(itemView);
                this.tvAvatar = tvAvatar;
                this.tvName = tvName;
                this.tvEmail = tvEmail;
                this.tvTaskInfo = tvTaskInfo;
                this.tvRole = tvRole;
                this.ivDelete = ivDelete;
            }
        }
    }
}