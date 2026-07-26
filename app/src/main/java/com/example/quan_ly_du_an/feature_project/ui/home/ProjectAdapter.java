package com.example.quan_ly_du_an.feature_project.ui.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.model.ProjectWithRole;
import com.example.quan_ly_du_an.utils.RoleEnum;
import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private List<ProjectWithRole> projectList = new ArrayList<>();
    private final OnProjectClickListener listener;
    private OnProjectLongClickListener longClickListener;

    public interface OnProjectClickListener {
        void onProjectClick(ProjectWithRole projectWithRole);
    }

    public interface OnProjectLongClickListener {
        void onProjectLongClick(ProjectWithRole projectWithRole);
    }

    public ProjectAdapter(OnProjectClickListener listener) {
        this.listener = listener;
    }

    public void setOnProjectLongClickListener(OnProjectLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setProjects(List<ProjectWithRole> projects) {
        this.projectList = projects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_card, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(projectList.get(position), listener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProjectTitle;
        private final TextView tvProjectDescription;
        private final TextView tvProjectStatus;
        private final TextView tvUserRoleBadge;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            tvProjectTitle = itemView.findViewById(R.id.tvProjectTitle);
            tvProjectDescription = itemView.findViewById(R.id.tvProjectDescription);
            tvProjectStatus = itemView.findViewById(R.id.tvProjectStatus);
            tvUserRoleBadge = itemView.findViewById(R.id.tvUserRoleBadge);
        }

        public void bind(ProjectWithRole item, OnProjectClickListener listener, OnProjectLongClickListener longClickListener) {
            tvProjectTitle.setText(item.title);
            tvProjectDescription.setText(item.description);
            tvProjectStatus.setText(item.status);
            tvUserRoleBadge.setText(item.role);

            if (RoleEnum.ADMIN.getRoleName().equalsIgnoreCase(item.role)) {
                tvUserRoleBadge.setTextColor(Color.parseColor("#B71C1C"));
                tvUserRoleBadge.setBackgroundColor(Color.parseColor("#FFEBEE"));
            } else if (RoleEnum.LEADER.getRoleName().equalsIgnoreCase(item.role)) {
                tvUserRoleBadge.setTextColor(Color.parseColor("#E65100"));
                tvUserRoleBadge.setBackgroundColor(Color.parseColor("#FFF3E0"));
            } else {
                tvUserRoleBadge.setTextColor(Color.parseColor("#1B5E20"));
                tvUserRoleBadge.setBackgroundColor(Color.parseColor("#E8F5E9"));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProjectClick(item);
            });
            
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onProjectLongClick(item);
                    return true;
                }
                return false;
            });
        }
    }
}
