package com.example.quan_ly_du_an.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.model.Task;
import com.google.android.material.chip.Chip;

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
        holder.txtDeadline.setText(task.getDeadline());

        holder.chipPriority.setText(task.getPriority());
        holder.chipStatus.setText(task.getStatus());

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
            boolean matchesSearch = task.getTitle().toLowerCase().contains(currentSearchText) 
                    || task.getDescription().toLowerCase().contains(currentSearchText);
            
            boolean matchesStatus = currentStatusFilter.equals("Tất cả") 
                    || task.getStatus().equalsIgnoreCase(currentStatusFilter);
            
            boolean matchesPriority = currentPriorityFilter.equals("Tất cả ưu tiên")
                    || task.getPriority().equalsIgnoreCase(currentPriorityFilter);

            if (matchesSearch && matchesStatus && matchesPriority) {
                taskList.add(task);
            }
        }
        notifyDataSetChanged();
    }

    private void setPriorityColor(Chip chip, String priority) {
        if (priority == null) return;
        switch (priority) {
            case "Cao":
                chip.setChipBackgroundColorResource(R.color.red);
                break;
            case "Trung bình":
                chip.setChipBackgroundColorResource(R.color.orange);
                break;
            default:
                chip.setChipBackgroundColorResource(R.color.green);
                break;
        }
    }

    private void setStatusColor(Chip chip, String status) {
        if (status == null) return;
        switch (status) {
            case "To Do":
                chip.setChipBackgroundColorResource(R.color.gray);
                break;
            case "In Progress":
            case "Đang làm":
                chip.setChipBackgroundColorResource(R.color.blue);
                break;
            case "Done":
            case "Hoàn thành":
                chip.setChipBackgroundColorResource(R.color.green);
                break;
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtDeadline;
        Chip chipPriority, chipStatus;

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
