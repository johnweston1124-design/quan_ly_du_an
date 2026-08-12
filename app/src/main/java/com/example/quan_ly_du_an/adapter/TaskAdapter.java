package com.example.quan_ly_du_an.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private List<Task> taskListFull;
    private final OnTaskClickListener listener;

    private String currentSearchText = "";
    private String currentStatusFilter = "Tất cả";
    private String currentPriorityFilter = "Tất cả ưu tiên";

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = new ArrayList<>(taskList);
        this.taskListFull = new ArrayList<>(taskList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.txtTitle.setText(task.getTitle());
        holder.txtDescription.setText(task.getDescription());
        holder.txtDeadline.setText(task.getDeadline() != null ? task.getDeadline() : "");

        holder.chipPriority.setText(task.getPriority() != null ? task.getPriority() : "Trung bình");
        holder.chipStatus.setText(task.getStatus() != null ? task.getStatus() : "Tự do");

        setPriorityColor(holder.chipPriority, task.getPriority());
        setStatusColor(holder.chipStatus, task.getStatus());

        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateData(List<Task> newList) {
        this.taskListFull = new ArrayList<>(newList);
        applyFilter();
    }

    public void filter(String query) {
        this.currentSearchText = query.toLowerCase().trim();
        applyFilter();
    }

    public void filterByStatus(String status) {
        this.currentStatusFilter = status;
        applyFilter();
    }

    public void filterByPriority(String priority) {
        this.currentPriorityFilter = priority;
        applyFilter();
    }

    private void applyFilter() {
        taskList.clear();
        for (Task task : taskListFull) {
            String title = task.getTitle() != null ? task.getTitle() : "";
            String desc = task.getDescription() != null ? task.getDescription() : "";
            String status = task.getStatus() != null ? task.getStatus() : "";
            String priority = task.getPriority() != null ? task.getPriority() : "";

            boolean matchesSearch = title.toLowerCase().contains(currentSearchText) 
                    || desc.toLowerCase().contains(currentSearchText);
            
            boolean matchesStatus = currentStatusFilter.equals("Tất cả") 
                    || status.equalsIgnoreCase(currentStatusFilter)
                    || (currentStatusFilter.equalsIgnoreCase("Đang làm") && status.equalsIgnoreCase("Doing"))
                    || (currentStatusFilter.equalsIgnoreCase("Hoàn thành") && status.equalsIgnoreCase("Done"));
            
            boolean matchesPriority = currentPriorityFilter.equals("Tất cả ưu tiên")
                    || priority.equalsIgnoreCase(currentPriorityFilter);

            if (matchesSearch && matchesStatus && matchesPriority) {
                taskList.add(task);
            }
        }
        notifyDataSetChanged();
    }

    private void setPriorityColor(TextView tv, String priority) {
        if (tv == null) return;
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(dpToPx(tv.getContext(), 8));

        String p = priority != null ? priority.trim() : "";
        if (p.equalsIgnoreCase("Cao") || p.equalsIgnoreCase("High")) {
            tv.setTextColor(Color.parseColor("#B71C1C"));
            shape.setColor(Color.parseColor("#FFEBEE"));
        } else if (p.equalsIgnoreCase("Trung bình") || p.equalsIgnoreCase("Medium")) {
            tv.setTextColor(Color.parseColor("#E65100"));
            shape.setColor(Color.parseColor("#FFF3E0"));
        } else {
            tv.setTextColor(Color.parseColor("#1B5E20"));
            shape.setColor(Color.parseColor("#E8F5E9"));
        }
        tv.setBackground(shape);
    }

    private void setStatusColor(TextView tv, String status) {
        if (tv == null) return;
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(dpToPx(tv.getContext(), 8));

        String s = status != null ? status.trim() : "";
        if (s.equalsIgnoreCase("Doing") || s.equalsIgnoreCase("Đang làm") || s.equalsIgnoreCase("In Progress")) {
            tv.setTextColor(Color.parseColor("#0D47A1"));
            shape.setColor(Color.parseColor("#E3F2FD"));
        } else if (s.equalsIgnoreCase("Done") || s.equalsIgnoreCase("Hoàn thành")) {
            tv.setTextColor(Color.parseColor("#1B5E20"));
            shape.setColor(Color.parseColor("#E8F5E9"));
        } else {
            tv.setTextColor(Color.parseColor("#616161"));
            shape.setColor(Color.parseColor("#F5F5F5"));
        }
        tv.setBackground(shape);
    }

    private static int dpToPx(android.content.Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtDeadline;
        TextView chipPriority, chipStatus;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTaskTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtDeadline = itemView.findViewById(R.id.txtDeadline);
            chipPriority = itemView.findViewById(R.id.chipPriority);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}
